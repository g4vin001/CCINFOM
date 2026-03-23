package com.archersground.dbapp.service;

import com.archersground.dbapp.config.DatabaseConfig;
import com.archersground.dbapp.dao.CustomerDao;
import com.archersground.dbapp.dao.EmployeeDao;
import com.archersground.dbapp.dao.GateDao;
import com.archersground.dbapp.dao.MenuItemDao;
import com.archersground.dbapp.dao.OrderDao;
import com.archersground.dbapp.dao.OrderItemDao;
import com.archersground.dbapp.dao.OrderStatusLogDao;
import com.archersground.dbapp.dao.PaymentDao;
import com.archersground.dbapp.dao.RefundDao;
import com.archersground.dbapp.model.Customer;
import com.archersground.dbapp.model.Gate;
import com.archersground.dbapp.model.MenuItem;
import com.archersground.dbapp.model.OrderItemRequest;
import com.archersground.dbapp.model.OrderSnapshot;
import com.archersground.dbapp.model.OrderStatus;
import com.archersground.dbapp.model.OrderType;
import com.archersground.dbapp.model.OrderWorkflowView;
import com.archersground.dbapp.model.PaymentMethod;
import com.archersground.dbapp.model.PaymentRecord;
import com.archersground.dbapp.model.PlaceOrderRequest;
import com.archersground.dbapp.util.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OrderService {
    private final MenuItemDao menuItemDao = new MenuItemDao();
    private final CustomerDao customerDao = new CustomerDao();
    private final EmployeeDao employeeDao = new EmployeeDao();
    private final GateDao gateDao = new GateDao();
    private final OrderDao orderDao = new OrderDao();
    private final OrderItemDao orderItemDao = new OrderItemDao();
    private final PaymentDao paymentDao = new PaymentDao();
    private final OrderStatusLogDao orderStatusLogDao = new OrderStatusLogDao();
    private final RefundDao refundDao = new RefundDao();

    public int placeOrder(PlaceOrderRequest request) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                int processingEmployeeId = resolveProcessingEmployeeId(connection, request.getEmployeeId());
                Customer customer = customerDao.findById(connection, request.getCustomerId());
                if (customer == null) {
                    throw new IllegalArgumentException("Customer not found.");
                }

                if (request.getItems().isEmpty()) {
                    throw new IllegalArgumentException("At least one order item is required.");
                }

                BigDecimal subtotal = BigDecimal.ZERO;
                Map<Integer, MenuItem> menuItemsById = new LinkedHashMap<>();
                for (OrderItemRequest itemRequest : request.getItems()) {
                    if (itemRequest.getQuantity() <= 0) {
                        throw new IllegalArgumentException("Quantity must be greater than zero.");
                    }

                    MenuItem menuItem = menuItemDao.findById(connection, itemRequest.getMenuItemId());
                    if (menuItem == null || !menuItem.isAvailable()) {
                        throw new IllegalArgumentException("Menu item " + itemRequest.getMenuItemId() + " is unavailable.");
                    }
                    menuItemsById.put(itemRequest.getMenuItemId(), menuItem);
                    subtotal = subtotal.add(menuItem.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity())));
                }

                BigDecimal deliveryFee = BigDecimal.ZERO;
                if (request.getOrderType() == OrderType.CAMPUS_GATE_DELIVERY) {
                    if (!customer.isLasallian()) {
                        throw new IllegalArgumentException("Only Lasallians may use campus-gate delivery.");
                    }
                    if (request.getGateId() == null) {
                        throw new IllegalArgumentException("Gate is required for campus-gate delivery.");
                    }
                    deliveryFee = gateDao.findActiveDeliveryFee(connection, request.getGateId());
                    if (deliveryFee == null) {
                        throw new IllegalArgumentException("Selected gate is inactive or does not exist.");
                    }
                }

                BigDecimal totalAmount = subtotal.add(deliveryFee);
                boolean deferredCashPickup = request.getOrderType() == OrderType.PICK_UP
                    && request.getPaymentMethod() == PaymentMethod.CASH;
                int orderId = orderDao.insertOrder(
                    connection,
                    request.getCustomerId(),
                    processingEmployeeId,
                    request.getGateId(),
                    request.getOrderType(),
                    subtotal,
                    deliveryFee,
                    totalAmount
                );

                for (OrderItemRequest itemRequest : request.getItems()) {
                    MenuItem menuItem = menuItemsById.get(itemRequest.getMenuItemId());
                    BigDecimal lineSubtotal = menuItem.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
                    orderItemDao.insertOrderItem(
                        connection,
                        orderId,
                        itemRequest.getMenuItemId(),
                        itemRequest.getQuantity(),
                        menuItem.getPrice(),
                        lineSubtotal
                    );
                }

                paymentDao.insertPayment(
                    connection,
                    orderId,
                    request.getPaymentMethod(),
                    totalAmount,
                    deferredCashPickup ? "PENDING" : "PAID"
                );
                if (deferredCashPickup) {
                    orderDao.updateStatus(connection, orderId, OrderStatus.PREPARING);
                } else {
                    orderDao.updateStatus(connection, orderId, OrderStatus.PAID);
                    orderStatusLogDao.insertStatusUpdate(connection, orderId, OrderStatus.PAID, processingEmployeeId, "Payment recorded");
                }
                connection.commit();
                return orderId;
            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public void markOrderReady(int orderId, int updatedByEmployeeId) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                validateEmployee(connection, updatedByEmployeeId);
                OrderSnapshot order = requireOrder(connection, orderId);
                if (order.getOrderStatus() != OrderStatus.PAID && order.getOrderStatus() != OrderStatus.PREPARING) {
                    throw new IllegalArgumentException("Only PAID or PREPARING orders can be marked READY.");
                }

                orderDao.markPrepared(connection, orderId, LocalDateTime.now());
                orderStatusLogDao.insertStatusUpdate(
                    connection,
                    orderId,
                    OrderStatus.READY,
                    updatedByEmployeeId,
                    "Order marked ready"
                );
                connection.commit();
            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public void updateDeliveryStatus(int orderId, OrderStatus newStatus, int updatedByEmployeeId, String notes) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                validateEmployee(connection, updatedByEmployeeId);
                OrderSnapshot order = requireOrder(connection, orderId);
                validateDeliveryTransition(order, newStatus);
                orderDao.updateStatus(connection, orderId, newStatus);
                orderStatusLogDao.insertStatusUpdate(connection, orderId, newStatus, updatedByEmployeeId, notes);
                if (newStatus == OrderStatus.DELIVERED || newStatus == OrderStatus.FAILED_DELIVERY) {
                    orderDao.markCompleted(connection, orderId, newStatus, LocalDateTime.now());
                }
                connection.commit();
            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public void cancelOrRefundOrder(int orderId, int updatedByEmployeeId, BigDecimal refundAmount, String reason) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                validateEmployee(connection, updatedByEmployeeId);
                OrderSnapshot order = requireOrder(connection, orderId);
                validateCancellationReason(reason);
                validateRefundAmount(refundAmount);
                validateCancellationState(order);

                if (refundAmount.compareTo(BigDecimal.ZERO) > 0) {
                    PaymentRecord payment = paymentDao.findByOrderId(connection, orderId);
                    if (payment == null) {
                        throw new IllegalArgumentException("Order does not have a payment record.");
                    }
                    if ("REFUNDED".equalsIgnoreCase(payment.getStatus())) {
                        throw new IllegalArgumentException("Order payment has already been refunded.");
                    }
                    if (refundAmount.compareTo(payment.getAmount()) > 0) {
                        throw new IllegalArgumentException("Refund amount cannot exceed the paid amount.");
                    }

                    orderDao.updateStatus(connection, orderId, OrderStatus.REFUNDED);
                    paymentDao.markRefunded(connection, orderId);
                    refundDao.insertRefund(connection, orderId, payment.getPaymentId(), refundAmount, reason);
                    orderStatusLogDao.insertStatusUpdate(
                        connection,
                        orderId,
                        OrderStatus.REFUNDED,
                        updatedByEmployeeId,
                        reason
                    );
                } else {
                    orderDao.updateStatus(connection, orderId, OrderStatus.CANCELLED);
                    orderStatusLogDao.insertStatusUpdate(
                        connection,
                        orderId,
                        OrderStatus.CANCELLED,
                        updatedByEmployeeId,
                        reason
                    );
                }
                connection.commit();
            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public void requestOrderCancellation(int orderId, String reason) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int defaultEmployeeId = resolveProcessingEmployeeId(connection, null);
                OrderSnapshot order = requireOrder(connection, orderId);
                PaymentRecord payment = paymentDao.findByOrderId(connection, orderId);
                String cancellationReason = prefixCustomerReason(reason, "Customer cancellation");

                validateCancellationReason(cancellationReason);
                validateCancellationState(order);

                orderDao.updateStatus(connection, orderId, OrderStatus.CANCELLED);
                orderStatusLogDao.insertStatusUpdate(
                    connection,
                    orderId,
                    OrderStatus.CANCELLED,
                    defaultEmployeeId,
                    cancellationReason
                );

                if (shouldAutoRefund(order, payment)) {
                    paymentDao.markRefunded(connection, orderId);
                    refundDao.insertRefund(connection, orderId, payment.getPaymentId(), payment.getAmount(), cancellationReason);
                }

                connection.commit();
            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public List<Gate> getActiveGates() throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            return gateDao.findActiveGates(connection);
        }
    }

    public List<OrderWorkflowView> getPreparationQueue() throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            return orderDao.findPreparationQueue(connection);
        }
    }

    public List<OrderWorkflowView> getDeliveryQueue() throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            return orderDao.findDeliveryQueue(connection);
        }
    }

    public List<OrderWorkflowView> getCollectionQueue() throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            return orderDao.findCollectionQueue(connection);
        }
    }

    public void completeCollectionOrder(int orderId, int updatedByEmployeeId) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                validateEmployee(connection, updatedByEmployeeId);
                OrderSnapshot order = requireOrder(connection, orderId);
                if (order.getOrderType() == OrderType.CAMPUS_GATE_DELIVERY) {
                    throw new IllegalArgumentException("Campus-gate delivery orders must use delivery fulfillment.");
                }
                if (order.getOrderStatus() != OrderStatus.READY) {
                    throw new IllegalArgumentException("Only READY dine-in or pick-up orders can be completed.");
                }

                PaymentRecord payment = paymentDao.findByOrderId(connection, orderId);
                if (payment != null && "PENDING".equalsIgnoreCase(payment.getStatus())) {
                    paymentDao.markPaid(connection, orderId);
                    orderStatusLogDao.insertStatusUpdate(
                        connection,
                        orderId,
                        OrderStatus.PAID,
                        updatedByEmployeeId,
                        "Cash payment received upon collection"
                    );
                }

                orderDao.markCompleted(connection, orderId, OrderStatus.COMPLETED, LocalDateTime.now());
                orderStatusLogDao.insertStatusUpdate(
                    connection,
                    orderId,
                    OrderStatus.COMPLETED,
                    updatedByEmployeeId,
                    "Order claimed / served"
                );
                connection.commit();
            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public List<OrderWorkflowView> getCancellationQueue() throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            return orderDao.findCancellationQueue(connection);
        }
    }

    private void validateEmployee(Connection connection, int employeeId) throws SQLException {
        if (!employeeDao.isActiveEmployee(connection, employeeId)) {
            throw new IllegalArgumentException("Employee not found or inactive.");
        }
    }

    private int resolveProcessingEmployeeId(Connection connection, Integer employeeId) throws SQLException {
        int resolvedEmployeeId = employeeId != null ? employeeId : DatabaseConfig.getDefaultProcessingEmployeeId();
        validateEmployee(connection, resolvedEmployeeId);
        return resolvedEmployeeId;
    }

    private OrderSnapshot requireOrder(Connection connection, int orderId) throws SQLException {
        OrderSnapshot order = orderDao.findById(connection, orderId);
        if (order == null) {
            throw new IllegalArgumentException("Order not found.");
        }
        return order;
    }

    private void validateDeliveryTransition(OrderSnapshot order, OrderStatus newStatus) {
        if (order.getOrderType() != OrderType.CAMPUS_GATE_DELIVERY) {
            throw new IllegalArgumentException("Only campus-gate delivery orders can use delivery statuses.");
        }

        OrderStatus currentStatus = order.getOrderStatus();
        if (newStatus == OrderStatus.OUT_FOR_DELIVERY && currentStatus != OrderStatus.READY) {
            throw new IllegalArgumentException("Only READY orders can be marked OUT_FOR_DELIVERY.");
        }
        if ((newStatus == OrderStatus.DELIVERED || newStatus == OrderStatus.FAILED_DELIVERY)
            && currentStatus != OrderStatus.OUT_FOR_DELIVERY) {
            throw new IllegalArgumentException("Only OUT_FOR_DELIVERY orders can be completed.");
        }
    }

    private void validateCancellationState(OrderSnapshot order) {
        OrderStatus currentStatus = order.getOrderStatus();
        if (currentStatus == OrderStatus.CANCELLED || currentStatus == OrderStatus.REFUNDED) {
            throw new IllegalArgumentException("Order has already been closed.");
        }
        if (currentStatus == OrderStatus.DELIVERED || currentStatus == OrderStatus.COMPLETED) {
            throw new IllegalArgumentException("Completed orders cannot be cancelled or refunded.");
        }
    }

    private void validateRefundAmount(BigDecimal refundAmount) {
        if (refundAmount == null) {
            throw new IllegalArgumentException("Refund amount is required.");
        }
        if (refundAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Refund amount cannot be negative.");
        }
    }

    private void validateCancellationReason(String reason) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Reason is required.");
        }
    }

    private boolean shouldAutoRefund(OrderSnapshot order, PaymentRecord payment) {
        if (payment == null) {
            return false;
        }
        if (!"PAID".equalsIgnoreCase(payment.getStatus())) {
            return false;
        }
        if ("REFUNDED".equalsIgnoreCase(payment.getStatus())) {
            return false;
        }
        return switch (order.getOrderStatus()) {
            case CREATED -> false;
            default -> payment.getAmount().compareTo(BigDecimal.ZERO) > 0;
        };
    }

    private String prefixCustomerReason(String reason, String prefix) {
        String trimmedReason = reason == null ? "" : reason.trim();
        if (trimmedReason.isEmpty()) {
            return prefix;
        }
        return prefix + ": " + trimmedReason;
    }
}
