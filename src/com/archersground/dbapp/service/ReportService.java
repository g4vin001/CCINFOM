package com.archersground.dbapp.service;

import com.archersground.dbapp.dao.ReportDao;
import com.archersground.dbapp.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.YearMonth;

public class ReportService {
    private final ReportDao reportDao = new ReportDao();

    public String getMonthlySalesSummary(YearMonth period) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            return reportDao.getMonthlySalesSummary(connection, period);
        }
    }

    public String getCampusGateDeliveryReport(YearMonth period) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            return reportDao.getCampusGateDeliveryReport(connection, period);
        }
    }

    public String getTopSellingItemsReport(YearMonth period) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            return reportDao.getTopSellingItemsReport(connection, period);
        }
    }

    public String getOrderVolumeByTimeOfDayReport(YearMonth period) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            return reportDao.getOrderVolumeByTimeOfDayReport(connection, period);
        }
    }
}
