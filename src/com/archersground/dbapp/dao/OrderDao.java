package com.archersground.dbapp.dao;

import com.archersground.dbapp.model.OrderSnapshot;
import com.archersground.dbapp.model.OrderStatus;
import com.archersground.dbapp.model.OrderType;
import com.archersground.dbapp.model.OrderWorkflowView;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderDao {
    public int insertOrder(
        Connection connection,
        int customerId,
        int employeeId,
        Integer gateId,
        OrderType orderType,
        BigDecimal subtotal,
        BigDecimal deliveryFee,
        BigDecimal totalAmount
    ) throws SQLException {
        String sql = """
            INSERT INTO orders (
                customer_id,
                processed_by_employee_id,
                gate_id,
                order_type,
                order_status,
                subtotal_amount,
                delivery_fee,
                total_amount
            ) VALUES (?, ?, ?, ?, 'CREATED', ?, ?, ?)
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, customerId);
            statement.setInt(2, employeeId);
            if (gateId == null) {
                statement.setNull(3, java.sql.Types.INTEGER);
            } else {
                statement.setInt(3, gateId);
            }
            statement.setString(4, orderType.name());
            statement.setBigDecimal(5, subtotal);
            statement.setBigDecimal(6, deliveryFee);
            statement.setBigDecimal(7, totalAmount);
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        }

        throw new SQLException("Unable to create order.");
    }

    public void updateStatus(Connection connection, int orderId, OrderStatus status) throws SQLException {
        String sql = "UPDATE orders SET order_status = ? WHERE order_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status.name());
            statement.setInt(2, orderId);
            statement.executeUpdate();
        }
    }

    public void markPrepared(Connection connection, int orderId, LocalDateTime preparedAt) throws SQLException {
        String sql = """
            UPDATE orders
            SET order_status = 'READY', prepared_at = ?
            WHERE order_id = ?
            """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setTimestamp(1, Timestamp.valueOf(preparedAt));
            statement.setInt(2, orderId);
            statement.executeUpdate();
        }
    }

    public void markCompleted(Connection connection, int orderId, OrderStatus status, LocalDateTime completedAt) throws SQLException {
        String sql = """
            UPDATE orders
            SET order_status = ?, completed_at = ?
            WHERE order_id = ?
            """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status.name());
            statement.setTimestamp(2, Timestamp.valueOf(completedAt));
            statement.setInt(3, orderId);
            statement.executeUpdate();
        }
    }

    public String findOrderType(Connection connection, int orderId) throws SQLException {
        String sql = "SELECT order_type FROM orders WHERE order_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("order_type");
                }
            }
        }
        return null;
    }

    public OrderSnapshot findById(Connection connection, int orderId) throws SQLException {
        String sql = """
            SELECT order_id, order_type, order_status, total_amount
            FROM orders
            WHERE order_id = ?
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new OrderSnapshot(
                        resultSet.getInt("order_id"),
                        OrderType.valueOf(resultSet.getString("order_type")),
                        OrderStatus.valueOf(resultSet.getString("order_status")),
                        resultSet.getBigDecimal("total_amount")
                    );
                }
            }
        }
        return null;
    }

    public List<OrderWorkflowView> findPreparationQueue(Connection connection) throws SQLException {
        String sql = """
            SELECT
                o.order_id,
                o.order_datetime,
                o.order_type,
                o.order_status,
                o.total_amount,
                CONCAT(c.first_name, ' ', c.last_name) AS customer_name,
                g.gate_name
            FROM orders o
            INNER JOIN customers c ON c.customer_id = o.customer_id
            LEFT JOIN gates g ON g.gate_id = o.gate_id
            WHERE o.order_status IN ('PAID', 'PREPARING')
            ORDER BY o.order_datetime DESC
            """;
        return findWorkflowOrders(connection, sql);
    }

    public List<OrderWorkflowView> findDeliveryQueue(Connection connection) throws SQLException {
        String sql = """
            SELECT
                o.order_id,
                o.order_datetime,
                o.order_type,
                o.order_status,
                o.total_amount,
                CONCAT(c.first_name, ' ', c.last_name) AS customer_name,
                g.gate_name
            FROM orders o
            INNER JOIN customers c ON c.customer_id = o.customer_id
            LEFT JOIN gates g ON g.gate_id = o.gate_id
            WHERE o.order_type = 'CAMPUS_GATE_DELIVERY'
              AND o.order_status IN ('READY', 'OUT_FOR_DELIVERY')
            ORDER BY o.order_datetime DESC
        """;
        return findWorkflowOrders(connection, sql);
    }

    public List<OrderWorkflowView> findCollectionQueue(Connection connection) throws SQLException {
        String sql = """
            SELECT
                o.order_id,
                o.order_datetime,
                o.order_type,
                o.order_status,
                o.total_amount,
                CONCAT(c.first_name, ' ', c.last_name) AS customer_name,
                g.gate_name
            FROM orders o
            INNER JOIN customers c ON c.customer_id = o.customer_id
            LEFT JOIN gates g ON g.gate_id = o.gate_id
            WHERE o.order_type IN ('DINE_IN', 'PICK_UP')
              AND o.order_status = 'READY'
            ORDER BY o.order_datetime DESC
            """;
        return findWorkflowOrders(connection, sql);
    }

    public List<OrderWorkflowView> findCancellationQueue(Connection connection) throws SQLException {
        String sql = """
            SELECT
                o.order_id,
                o.order_datetime,
                o.order_type,
                o.order_status,
                o.total_amount,
                CONCAT(c.first_name, ' ', c.last_name) AS customer_name,
                g.gate_name
            FROM orders o
            INNER JOIN customers c ON c.customer_id = o.customer_id
            LEFT JOIN gates g ON g.gate_id = o.gate_id
            WHERE o.order_status NOT IN ('CANCELLED', 'REFUNDED', 'DELIVERED', 'COMPLETED')
            ORDER BY o.order_datetime DESC
            """;
        return findWorkflowOrders(connection, sql);
    }

    private List<OrderWorkflowView> findWorkflowOrders(Connection connection, String sql) throws SQLException {
        List<OrderWorkflowView> orders = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                orders.add(new OrderWorkflowView(
                    resultSet.getInt("order_id"),
                    resultSet.getTimestamp("order_datetime").toLocalDateTime(),
                    OrderType.valueOf(resultSet.getString("order_type")),
                    OrderStatus.valueOf(resultSet.getString("order_status")),
                    resultSet.getBigDecimal("total_amount"),
                    resultSet.getString("customer_name"),
                    resultSet.getString("gate_name")
                ));
            }
        }
        return orders;
    }
}
