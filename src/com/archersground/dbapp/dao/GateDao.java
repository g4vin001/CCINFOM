package com.archersground.dbapp.dao;

import com.archersground.dbapp.model.Gate;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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

    public List<Gate> findActiveGates(Connection connection) throws SQLException {
        String sql = """
            SELECT gate_id, gate_name, delivery_fee, status
            FROM gates
            WHERE status = 'ACTIVE'
            ORDER BY gate_name
            """;

        List<Gate> gates = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                gates.add(new Gate(
                    resultSet.getInt("gate_id"),
                    resultSet.getString("gate_name"),
                    resultSet.getBigDecimal("delivery_fee"),
                    resultSet.getString("status")
                ));
            }
        }
        return gates;
    }
}
