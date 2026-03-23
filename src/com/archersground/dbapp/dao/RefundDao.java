package com.archersground.dbapp.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RefundDao {
    public void insertRefund(Connection connection, int orderId, int paymentId, BigDecimal amount, String reason) throws SQLException {
        String sql = """
            INSERT INTO refunds (order_id, payment_id, refund_amount, refund_reason)
            VALUES (?, ?, ?, ?)
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            statement.setInt(2, paymentId);
            statement.setBigDecimal(3, amount);
            statement.setString(4, reason);
            statement.executeUpdate();
        }
    }
}
