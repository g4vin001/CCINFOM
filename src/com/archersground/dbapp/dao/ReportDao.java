package com.archersground.dbapp.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.YearMonth;

public class ReportDao {
    public void printMonthlySalesSummary(Connection connection, YearMonth period) throws SQLException {
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
                    System.out.println("Monthly Sales Summary");
                    System.out.println("Total sales: Php " + resultSet.getBigDecimal("total_sales"));
                    System.out.println("Total orders: " + resultSet.getInt("total_orders"));
                    System.out.println("Average order value: Php " + resultSet.getBigDecimal("average_order_value"));
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
                System.out.println("Breakdown by order type:");
                while (resultSet.next()) {
                    System.out.println(resultSet.getString("order_type") + ": " + resultSet.getInt("order_count"));
                }
            }
        }
    }

    public void printCampusGateDeliveryReport(Connection connection, YearMonth period) throws SQLException {
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
                System.out.println("Campus-Gate Delivery Report");
                while (resultSet.next()) {
                    System.out.printf(
                        "%s | deliveries=%d | fees=Php %s | completed=%d | failed=%d%n",
                        resultSet.getString("gate_name"),
                        resultSet.getInt("delivery_count"),
                        resultSet.getBigDecimal("total_delivery_fees"),
                        resultSet.getInt("completed_deliveries"),
                        resultSet.getInt("failed_deliveries")
                    );
                }
            }
        }
    }

    public void printTopSellingItemsReport(Connection connection, YearMonth period) throws SQLException {
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
                System.out.println("Top-Selling Menu Items Report");
                while (resultSet.next()) {
                    System.out.printf(
                        "%s | quantity=%d | revenue=Php %s%n",
                        resultSet.getString("item_name"),
                        resultSet.getInt("total_quantity_sold"),
                        resultSet.getBigDecimal("total_revenue")
                    );
                }
            }
        }
    }

    public void printOrderVolumeByTimeOfDayReport(Connection connection, YearMonth period) throws SQLException {
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
                System.out.println("Order Volume by Time of Day Report");
                while (resultSet.next()) {
                    System.out.printf(
                        "%s | %s | %s | count=%d%n",
                        resultSet.getString("time_period"),
                        resultSet.getString("customer_type"),
                        resultSet.getString("order_type"),
                        resultSet.getInt("order_count")
                    );
                }
            }
        }
    }
}
