package com.archersground.dbapp.service;

import com.archersground.dbapp.dao.ReportDao;
import com.archersground.dbapp.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.YearMonth;

public class ReportService {
    private final ReportDao reportDao = new ReportDao();

    public void printMonthlySalesSummary(YearMonth period) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            reportDao.printMonthlySalesSummary(connection, period);
        }
    }

    public void printCampusGateDeliveryReport(YearMonth period) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            reportDao.printCampusGateDeliveryReport(connection, period);
        }
    }

    public void printTopSellingItemsReport(YearMonth period) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            reportDao.printTopSellingItemsReport(connection, period);
        }
    }

    public void printOrderVolumeByTimeOfDayReport(YearMonth period) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            reportDao.printOrderVolumeByTimeOfDayReport(connection, period);
        }
    }
}
