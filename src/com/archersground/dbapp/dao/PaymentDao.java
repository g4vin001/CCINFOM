package com.archersground.dbapp.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PaymentDao {
    public void insertPayment(Connection connection, int orderId, String paymentMethod, BigDecimal amount) throws SQLException {
        String sql = """
            INSERT INTO payments (order_id, payment_method, payment_amount, payment_status)
            VALUES (?, ?, ?, 'PAID')
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            statement.setString(2, paymentMethod);
            statement.setBigDecimal(3, amount);
            statement.executeUpdate();
        }
    }

    public void markRefunded(Connection connection, int orderId) throws SQLException {
        String sql = "UPDATE payments SET payment_status = 'REFUNDED' WHERE order_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            statement.executeUpdate();
        }
    }
}
