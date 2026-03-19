package com.archersground.dbapp.dao;

import com.archersground.dbapp.model.OrderStatus;
import com.archersground.dbapp.model.OrderType;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;

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
            ) VALUES (?, ?, ?, ?, 'PAID', ?, ?, ?)
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
}
