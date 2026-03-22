package com.archersground.dbapp.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RefundDao {
    public void insertRefund(Connection connection, int orderId, BigDecimal amount, String reason) throws SQLException {
        String sql = """
            INSERT INTO refunds (order_id, refund_amount, refund_reason)
            VALUES (?, ?, ?)
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            statement.setBigDecimal(2, amount);
            statement.setString(3, reason);
            statement.executeUpdate();
        }
    }
}
