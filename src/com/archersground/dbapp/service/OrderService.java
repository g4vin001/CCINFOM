package com.archersground.dbapp.service;

import com.archersground.dbapp.dao.CustomerDao;
import com.archersground.dbapp.dao.GateDao;
import com.archersground.dbapp.dao.MenuItemDao;
import com.archersground.dbapp.dao.OrderDao;
import com.archersground.dbapp.dao.OrderItemDao;
import com.archersground.dbapp.dao.OrderStatusLogDao;
import com.archersground.dbapp.dao.PaymentDao;
import com.archersground.dbapp.dao.RefundDao;
import com.archersground.dbapp.model.Customer;
import com.archersground.dbapp.model.MenuItem;
import com.archersground.dbapp.model.OrderItemRequest;
import com.archersground.dbapp.model.OrderStatus;
import com.archersground.dbapp.model.OrderType;
import com.archersground.dbapp.model.PlaceOrderRequest;
import com.archersground.dbapp.util.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class OrderService {
    private final MenuItemDao menuItemDao = new MenuItemDao();
    private final CustomerDao customerDao = new CustomerDao();
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
                Customer customer = customerDao.findById(connection, request.getCustomerId());
                if (customer == null) {
                    throw new IllegalArgumentException("Customer not found.");
                }

                BigDecimal subtotal = BigDecimal.ZERO;
                for (OrderItemRequest itemRequest : request.getItems()) {
                    MenuItem menuItem = menuItemDao.findById(connection, itemRequest.getMenuItemId());
                    if (menuItem == null || !menuItem.isAvailable()) {
                        throw new IllegalArgumentException("Menu item " + itemRequest.getMenuItemId() + " is unavailable.");
                    }
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
                int orderId = orderDao.insertOrder(
                    connection,
                    request.getCustomerId(),
                    request.getEmployeeId(),
                    request.getGateId(),
                    request.getOrderType(),
                    subtotal,
                    deliveryFee,
                    totalAmount
                );

                for (OrderItemRequest itemRequest : request.getItems()) {
                    MenuItem menuItem = menuItemDao.findById(connection, itemRequest.getMenuItemId());
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

                paymentDao.insertPayment(connection, orderId, request.getPaymentMethod(), totalAmount);
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
            orderDao.markPrepared(connection, orderId, LocalDateTime.now());
            orderStatusLogDao.insertStatusUpdate(
                connection,
                orderId,
                OrderStatus.READY,
                updatedByEmployeeId,
                "Order marked ready"
            );
        }
    }

    public void updateDeliveryStatus(int orderId, OrderStatus newStatus, int updatedByEmployeeId, String notes) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                orderDao.updateStatus(connection, orderId, newStatus);
                orderStatusLogDao.insertStatusUpdate(connection, orderId, newStatus, updatedByEmployeeId, notes);
                if (newStatus == OrderStatus.DELIVERED || newStatus == OrderStatus.FAILED_DELIVERY) {
                    orderDao.markCompleted(connection, orderId, newStatus, LocalDateTime.now());
                }
                connection.commit();
            } catch (SQLException exception) {
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
                if (refundAmount.compareTo(BigDecimal.ZERO) > 0) {
                    orderDao.updateStatus(connection, orderId, OrderStatus.REFUNDED);
                    paymentDao.markRefunded(connection, orderId);
                    refundDao.insertRefund(connection, orderId, refundAmount, reason);
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
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }
}
