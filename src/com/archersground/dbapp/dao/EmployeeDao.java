package com.archersground.dbapp.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EmployeeDao {
    public boolean isActiveEmployee(Connection connection, int employeeId) throws SQLException {
        String sql = """
            SELECT 1
            FROM employees
            WHERE employee_id = ? AND status = 'ACTIVE'
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, employeeId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }
}
