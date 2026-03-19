package com.archersground.dbapp.dao;

import com.archersground.dbapp.model.Customer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CustomerDao {
    public Customer findById(Connection connection, int customerId) throws SQLException {
        String sql = """
            SELECT customer_id, last_name, first_name, customer_type, dlsu_id_number
            FROM customers
            WHERE customer_id = ?
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, customerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new Customer(
                        resultSet.getInt("customer_id"),
                        resultSet.getString("last_name"),
                        resultSet.getString("first_name"),
                        resultSet.getString("customer_type"),
                        resultSet.getString("dlsu_id_number")
                    );
                }
            }
        }
        return null;
    }
}
