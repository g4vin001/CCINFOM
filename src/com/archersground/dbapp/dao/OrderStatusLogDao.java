package com.archersground.dbapp.dao;

import com.archersground.dbapp.model.OrderStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class OrderStatusLogDao {
    public void insertStatusUpdate(
        Connection connection,
        int orderId,
        OrderStatus status,
        int updatedByEmployeeId,
        String notes
    ) throws SQLException {
        String sql = """
            INSERT INTO order_status_log (order_id, status, updated_by_employee_id, notes)
            VALUES (?, ?, ?, ?)
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            statement.setString(2, status.name());
            statement.setInt(3, updatedByEmployeeId);
            statement.setString(4, notes);
            statement.executeUpdate();
        }
    }
}
