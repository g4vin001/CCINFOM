package com.archersground.dbapp.dao;

import com.archersground.dbapp.model.PaymentMethod;
import com.archersground.dbapp.model.PaymentRecord;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PaymentDao {
    public void insertPayment(Connection connection, int orderId, PaymentMethod paymentMethod, BigDecimal amount) throws SQLException {
        String sql = """
            INSERT INTO payments (order_id, payment_method, payment_amount, payment_status)
            VALUES (?, ?, ?, 'PAID')
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            statement.setString(2, paymentMethod.name());
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

    public PaymentRecord findByOrderId(Connection connection, int orderId) throws SQLException {
        String sql = """
            SELECT payment_amount, payment_status
            FROM payments
            WHERE order_id = ?
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new PaymentRecord(
                        resultSet.getBigDecimal("payment_amount"),
                        resultSet.getString("payment_status")
                    );
                }
            }
        }
        return null;
    }
}
