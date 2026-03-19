package com.archersground.dbapp.dao;

import com.archersground.dbapp.model.MenuItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MenuItemDao {
    public List<MenuItem> findAvailableItems(Connection connection) throws SQLException {
        String sql = """
            SELECT menu_item_id, item_name, category, is_available, price
            FROM menu_items
            WHERE is_available = TRUE
            ORDER BY item_name
            """;

        List<MenuItem> items = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                items.add(new MenuItem(
                    resultSet.getInt("menu_item_id"),
                    resultSet.getString("item_name"),
                    resultSet.getString("category"),
                    resultSet.getBoolean("is_available"),
                    resultSet.getBigDecimal("price")
                ));
            }
        }
        return items;
    }

    public MenuItem findById(Connection connection, int menuItemId) throws SQLException {
        String sql = """
            SELECT menu_item_id, item_name, category, is_available, price
            FROM menu_items
            WHERE menu_item_id = ?
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, menuItemId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new MenuItem(
                        resultSet.getInt("menu_item_id"),
                        resultSet.getString("item_name"),
                        resultSet.getString("category"),
                        resultSet.getBoolean("is_available"),
                        resultSet.getBigDecimal("price")
                    );
                }
            }
        }
        return null;
    }
}
