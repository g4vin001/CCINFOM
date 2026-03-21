package com.archersground.dbapp.dao;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.YearMonth;

public class ReportDao {
    private static String php(BigDecimal value) {
        return "Php " + (value == null ? "0.00" : value.setScale(2, RoundingMode.HALF_UP).toPlainString());
    }

    private static String sep() {
        return "─".repeat(44) + "\n";
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
              AND order_status NOT IN ('CANCELLED', 'REFUNDED')
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, period.getYear());
            statement.setInt(2, period.getMonthValue());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    builder.append("Monthly Sales Summary — ").append(period).append('\n');
                    builder.append(sep());
                    builder.append(String.format("%-24s %s%n", "Total Sales:", php(resultSet.getBigDecimal("total_sales"))));
                    builder.append(String.format("%-24s %d%n", "Total Orders:", resultSet.getInt("total_orders")));
                    builder.append(String.format("%-24s %s%n", "Average Order Value:", php(resultSet.getBigDecimal("average_order_value"))));
                }
            }
        }

        String breakdownSql = """
            SELECT order_type, COUNT(*) AS order_count
            FROM orders
            WHERE YEAR(order_datetime) = ? AND MONTH(order_datetime) = ?
              AND order_status NOT IN ('CANCELLED', 'REFUNDED')
            GROUP BY order_type
            ORDER BY order_type
            """;

        try (PreparedStatement statement = connection.prepareStatement(breakdownSql)) {
            statement.setInt(1, period.getYear());
            statement.setInt(2, period.getMonthValue());
            try (ResultSet resultSet = statement.executeQuery()) {
                builder.append('\n').append("Breakdown by Order Type:\n");
                while (resultSet.next()) {
                    builder.append(String.format("  %-24s %d%n",
                        resultSet.getString("order_type") + ":",
                        resultSet.getInt("order_count")));
                }
            }
        }

        return builder.toString();
    }

    public String getCampusGateDeliveryReport(Connection connection, YearMonth period) throws SQLException {
        StringBuilder builder = new StringBuilder();
        builder.append("Campus-Gate Delivery Report — ").append(period).append('\n');
        builder.append(sep());
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
                    builder.append(String.format("%-20s | deliveries=%-3d | completed=%-3d | failed=%-3d | fees=%s%n",
                        resultSet.getString("gate_name"),
                        resultSet.getInt("delivery_count"),
                        resultSet.getInt("completed_deliveries"),
                        resultSet.getInt("failed_deliveries"),
                        php(resultSet.getBigDecimal("total_delivery_fees"))));
                }
            }
        }

        return builder.toString();
    }

    public String getTopSellingItemsReport(Connection connection, YearMonth period) throws SQLException {
        StringBuilder builder = new StringBuilder();
        builder.append("Top-Selling Menu Items — ").append(period).append('\n');
        builder.append(sep());
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
                int rank = 1;
                while (resultSet.next()) {
                    builder.append(String.format("%2d. %-28s | qty=%-4d | revenue=%s%n",
                        rank++,
                        resultSet.getString("item_name"),
                        resultSet.getInt("total_quantity_sold"),
                        php(resultSet.getBigDecimal("total_revenue"))));
                }
            }
        }

        return builder.toString();
    }

    public String getOrderVolumeByTimeOfDayReport(Connection connection, YearMonth period) throws SQLException {
        StringBuilder builder = new StringBuilder();
        builder.append("Order Volume by Time of Day — ").append(period).append('\n');
        builder.append(sep());
        String sql = """
            SELECT
                CASE
                    WHEN HOUR(o.order_datetime) BETWEEN 6 AND 11 THEN 'Morning'
                    WHEN HOUR(o.order_datetime) BETWEEN 12 AND 17 THEN 'Afternoon'
                    ELSE 'Evening'
                END AS time_period,
                CASE
                    WHEN HOUR(o.order_datetime) BETWEEN 6 AND 11 THEN 1
                    WHEN HOUR(o.order_datetime) BETWEEN 12 AND 17 THEN 2
                    ELSE 3
                END AS time_order,
                c.customer_type,
                o.order_type,
                COUNT(*) AS order_count
            FROM orders o
            INNER JOIN customers c ON o.customer_id = c.customer_id
            WHERE YEAR(o.order_datetime) = ? AND MONTH(o.order_datetime) = ?
              AND o.order_status NOT IN ('CANCELLED', 'REFUNDED')
            GROUP BY time_period, time_order, c.customer_type, o.order_type
            ORDER BY time_order, c.customer_type, o.order_type
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, period.getYear());
            statement.setInt(2, period.getMonthValue());
            try (ResultSet resultSet = statement.executeQuery()) {
                String lastPeriod = null;
                while (resultSet.next()) {
                    String timePeriod = resultSet.getString("time_period");
                    if (!timePeriod.equals(lastPeriod)) {
                        builder.append('\n').append(timePeriod).append('\n');
                        lastPeriod = timePeriod;
                    }
                    builder.append(String.format("  %-14s | %-22s | count=%d%n",
                        resultSet.getString("customer_type"),
                        resultSet.getString("order_type"),
                        resultSet.getInt("order_count")));
                }
            }
        }

        return builder.toString();
    }
}
