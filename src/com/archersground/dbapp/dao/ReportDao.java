package com.archersground.dbapp.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.YearMonth;

public class ReportDao {
    public void printMonthlySalesSummary(Connection connection, YearMonth period) throws SQLException {
        System.out.print(getMonthlySalesSummary(connection, period));
    }

    public String getMonthlySalesSummary(Connection connection, YearMonth period) throws SQLException {
        StringBuilder builder = new StringBuilder();
        String sql = """
            SELECT
                COALESCE(SUM(total_amount), 0) AS total_sales,
                COUNT(*) AS total_orders,
                COALESCE(AVG(total_amount), 0) AS average_order_value
            FROM orders
            WHERE YEAR(order_datetime) = ? AND MONTH(order_datetime) = ?
              AND order_status NOT IN ('CANCELLED')
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, period.getYear());
            statement.setInt(2, period.getMonthValue());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    builder.append("Monthly Sales Summary\n");
                    builder.append("Total sales: Php ").append(resultSet.getBigDecimal("total_sales")).append('\n');
                    builder.append("Total orders: ").append(resultSet.getInt("total_orders")).append('\n');
                    builder.append("Average order value: Php ").append(resultSet.getBigDecimal("average_order_value")).append('\n');
                }
            }
        }

        String breakdownSql = """
            SELECT order_type, COUNT(*) AS order_count
            FROM orders
            WHERE YEAR(order_datetime) = ? AND MONTH(order_datetime) = ?
              AND order_status NOT IN ('CANCELLED')
            GROUP BY order_type
            ORDER BY order_type
            """;

        try (PreparedStatement statement = connection.prepareStatement(breakdownSql)) {
            statement.setInt(1, period.getYear());
            statement.setInt(2, period.getMonthValue());
            try (ResultSet resultSet = statement.executeQuery()) {
                builder.append('\n').append("Breakdown by order type:\n");
                while (resultSet.next()) {
                    builder.append(resultSet.getString("order_type"))
                        .append(": ")
                        .append(resultSet.getInt("order_count"))
                        .append('\n');
                }
            }
        }

        return builder.toString();
    }

    public void printCampusGateDeliveryReport(Connection connection, YearMonth period) throws SQLException {
        System.out.print(getCampusGateDeliveryReport(connection, period));
    }

    public String getCampusGateDeliveryReport(Connection connection, YearMonth period) throws SQLException {
        StringBuilder builder = new StringBuilder("Campus-Gate Delivery Report\n");
        String sql = """
            SELECT
                g.gate_name,
                COUNT(o.order_id) AS delivery_count,
                COALESCE(SUM(o.delivery_fee), 0) AS total_delivery_fees,
                SUM(CASE WHEN o.order_status = 'DELIVERED' THEN 1 ELSE 0 END) AS completed_deliveries,
                SUM(CASE WHEN o.order_status = 'FAILED_DELIVERY' THEN 1 ELSE 0 END) AS failed_deliveries
            FROM gates g
            LEFT JOIN orders o ON g.gate_id = o.gate_id
                AND YEAR(o.order_datetime) = ? AND MONTH(o.order_datetime) = ?
                AND o.order_type = 'CAMPUS_GATE_DELIVERY'
            GROUP BY g.gate_id, g.gate_name
            ORDER BY g.gate_name
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, period.getYear());
            statement.setInt(2, period.getMonthValue());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    builder.append(resultSet.getString("gate_name"))
                        .append(" | deliveries=")
                        .append(resultSet.getInt("delivery_count"))
                        .append(" | fees=Php ")
                        .append(resultSet.getBigDecimal("total_delivery_fees"))
                        .append(" | completed=")
                        .append(resultSet.getInt("completed_deliveries"))
                        .append(" | failed=")
                        .append(resultSet.getInt("failed_deliveries"))
                        .append('\n');
                }
            }
        }

        return builder.toString();
    }

    public void printTopSellingItemsReport(Connection connection, YearMonth period) throws SQLException {
        System.out.print(getTopSellingItemsReport(connection, period));
    }

    public String getTopSellingItemsReport(Connection connection, YearMonth period) throws SQLException {
        StringBuilder builder = new StringBuilder("Top-Selling Menu Items Report\n");
        String sql = """
            SELECT
                mi.item_name,
                SUM(oi.quantity) AS total_quantity_sold,
                SUM(oi.line_subtotal) AS total_revenue
            FROM order_items oi
            INNER JOIN orders o ON oi.order_id = o.order_id
            INNER JOIN menu_items mi ON oi.menu_item_id = mi.menu_item_id
            WHERE YEAR(o.order_datetime) = ? AND MONTH(o.order_datetime) = ?
              AND o.order_status NOT IN ('CANCELLED', 'REFUNDED')
            GROUP BY mi.menu_item_id, mi.item_name
            ORDER BY total_quantity_sold DESC, total_revenue DESC
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, period.getYear());
            statement.setInt(2, period.getMonthValue());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    builder.append(resultSet.getString("item_name"))
                        .append(" | quantity=")
                        .append(resultSet.getInt("total_quantity_sold"))
                        .append(" | revenue=Php ")
                        .append(resultSet.getBigDecimal("total_revenue"))
                        .append('\n');
                }
            }
        }

        return builder.toString();
    }

    public void printOrderVolumeByTimeOfDayReport(Connection connection, YearMonth period) throws SQLException {
        System.out.print(getOrderVolumeByTimeOfDayReport(connection, period));
    }

    public String getOrderVolumeByTimeOfDayReport(Connection connection, YearMonth period) throws SQLException {
        StringBuilder builder = new StringBuilder("Order Volume by Time of Day Report\n");
        String sql = """
            SELECT
                CASE
                    WHEN HOUR(o.order_datetime) BETWEEN 6 AND 11 THEN 'Morning'
                    WHEN HOUR(o.order_datetime) BETWEEN 12 AND 17 THEN 'Afternoon'
                    ELSE 'Evening'
                END AS time_period,
                c.customer_type,
                o.order_type,
                COUNT(*) AS order_count
            FROM orders o
            INNER JOIN customers c ON o.customer_id = c.customer_id
            WHERE YEAR(o.order_datetime) = ? AND MONTH(o.order_datetime) = ?
            GROUP BY time_period, c.customer_type, o.order_type
            ORDER BY time_period, c.customer_type, o.order_type
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, period.getYear());
            statement.setInt(2, period.getMonthValue());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    builder.append(resultSet.getString("time_period"))
                        .append(" | ")
                        .append(resultSet.getString("customer_type"))
                        .append(" | ")
                        .append(resultSet.getString("order_type"))
                        .append(" | count=")
                        .append(resultSet.getInt("order_count"))
                        .append('\n');
                }
            }
        }

        return builder.toString();
    }
}
