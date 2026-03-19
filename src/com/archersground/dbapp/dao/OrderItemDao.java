package com.archersground.dbapp.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class OrderItemDao {
    public void insertOrderItem(
        Connection connection,
        int orderId,
        int menuItemId,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal lineSubtotal
    ) throws SQLException {
        String sql = """
            INSERT INTO order_items (order_id, menu_item_id, quantity, unit_price, line_subtotal)
            VALUES (?, ?, ?, ?, ?)
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            statement.setInt(2, menuItemId);
            statement.setInt(3, quantity);
            statement.setBigDecimal(4, unitPrice);
            statement.setBigDecimal(5, lineSubtotal);
            statement.executeUpdate();
        }
    }
}
