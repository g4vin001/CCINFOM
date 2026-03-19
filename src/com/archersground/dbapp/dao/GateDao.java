package com.archersground.dbapp.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GateDao {
    public BigDecimal findActiveDeliveryFee(Connection connection, int gateId) throws SQLException {
        String sql = """
            SELECT delivery_fee
            FROM gates
            WHERE gate_id = ? AND status = 'ACTIVE'
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, gateId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getBigDecimal("delivery_fee");
                }
            }
        }
        return null;
    }
}
