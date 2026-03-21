package com.archersground.dbapp.ui;

import com.archersground.dbapp.dao.MenuItemDao;
import com.archersground.dbapp.model.Gate;
import com.archersground.dbapp.model.MenuItem;
import com.archersground.dbapp.model.OrderItemRequest;
import com.archersground.dbapp.model.OrderStatus;
import com.archersground.dbapp.model.OrderType;
import com.archersground.dbapp.model.PaymentMethod;
import com.archersground.dbapp.model.PlaceOrderRequest;
import com.archersground.dbapp.service.OrderService;
import com.archersground.dbapp.service.ReportService;
import com.archersground.dbapp.util.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ConsoleApp {
    private final Scanner scanner = new Scanner(System.in);
    private final OrderService orderService = new OrderService();
    private final ReportService reportService = new ReportService();
    private final MenuItemDao menuItemDao = new MenuItemDao();

    public void start() {
        boolean running = true;
        while (running) {
            printMainMenu();
            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1" -> showAvailableMenuItems();
                    case "2" -> placeOrder();
                    case "3" -> markOrderReady();
                    case "4" -> updateDeliveryStatus();
                    case "5" -> cancelOrRefundOrder();
                    case "6" -> generateReports();
                    case "0" -> running = false;
                    default -> System.out.println("Invalid option.");
                }
            } catch (SQLException | IllegalArgumentException exception) {
                System.out.println("Operation failed: " + exception.getMessage());
            }
        }
    }

    private void printMainMenu() {
        System.out.println();
        System.out.println("Archer's Ground DB App");
        System.out.println("1. View available menu items");
        System.out.println("2. Place online order and process payment");
        System.out.println("3. Update order preparation status");
        System.out.println("4. Campus-gate delivery fulfillment");
        System.out.println("5. Cancel or refund order");
        System.out.println("6. Generate reports");
        System.out.println("0. Exit");
        System.out.print("Select option: ");
    }

    private void showAvailableMenuItems() throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            List<MenuItem> items = menuItemDao.findAvailableItems(connection);
            System.out.println("Available menu items:");
            for (MenuItem item : items) {
                System.out.println(item);
            }
        }
    }

    private void placeOrder() throws SQLException {
        System.out.print("Customer ID: ");
        int customerId = Integer.parseInt(scanner.nextLine().trim());

        System.out.print("Order type (1=Dine-in, 2=Pick-up, 3=Campus-gate delivery): ");
        String orderTypeChoice = scanner.nextLine().trim();
        OrderType orderType = switch (orderTypeChoice) {
            case "1" -> OrderType.DINE_IN;
            case "2" -> OrderType.PICK_UP;
            case "3" -> OrderType.CAMPUS_GATE_DELIVERY;
            default -> throw new IllegalArgumentException("Invalid order type.");
        };

        Integer gateId = null;
        if (orderType == OrderType.CAMPUS_GATE_DELIVERY) {
            gateId = chooseActiveGate();
        }

        showAvailableMenuItems();
        List<OrderItemRequest> items = new ArrayList<>();
        boolean addingItems = true;
        while (addingItems) {
            System.out.print("Menu item ID: ");
            int menuItemId = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Quantity: ");
            int quantity = Integer.parseInt(scanner.nextLine().trim());
            items.add(new OrderItemRequest(menuItemId, quantity));

            System.out.print("Add another item? (Y/N): ");
            addingItems = "Y".equalsIgnoreCase(scanner.nextLine().trim());
        }

        System.out.print("Payment method (CASH, GCASH, CARD): ");
        String paymentMethod = scanner.nextLine().trim().toUpperCase();

        PlaceOrderRequest request = new PlaceOrderRequest(
            customerId,
            null,
            gateId,
            orderType,
            PaymentMethod.fromString(paymentMethod),
            items
        );

        int orderId = orderService.placeOrder(request);
        System.out.println("Order created successfully. Order ID: " + orderId);
    }

    private void markOrderReady() throws SQLException {
        System.out.print("Order ID to mark as ready: ");
        int orderId = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Employee ID updating the order: ");
        int employeeId = Integer.parseInt(scanner.nextLine().trim());
        orderService.markOrderReady(orderId, employeeId);
        System.out.println("Order updated to READY.");
    }

    private void updateDeliveryStatus() throws SQLException {
        System.out.print("Order ID: ");
        int orderId = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Employee ID updating the delivery status: ");
        int employeeId = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("New delivery status (1=OUT_FOR_DELIVERY, 2=DELIVERED, 3=FAILED_DELIVERY): ");
        String choice = scanner.nextLine().trim();
        OrderStatus status = switch (choice) {
            case "1" -> OrderStatus.OUT_FOR_DELIVERY;
            case "2" -> OrderStatus.DELIVERED;
            case "3" -> OrderStatus.FAILED_DELIVERY;
            default -> throw new IllegalArgumentException("Invalid delivery status.");
        };
        System.out.print("Notes: ");
        String notes = scanner.nextLine().trim();
        orderService.updateDeliveryStatus(orderId, status, employeeId, notes);
        System.out.println("Delivery status updated.");
    }

    private void cancelOrRefundOrder() throws SQLException {
        System.out.print("Order ID: ");
        int orderId = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Employee ID updating the order: ");
        int employeeId = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Refund amount (0 for cancellation only): ");
        BigDecimal refundAmount = new BigDecimal(scanner.nextLine().trim());
        System.out.print("Reason: ");
        String reason = scanner.nextLine().trim();
        orderService.cancelOrRefundOrder(orderId, employeeId, refundAmount, reason);
        if (refundAmount.compareTo(BigDecimal.ZERO) > 0) {
            System.out.println("Order refunded.");
        } else {
            System.out.println("Order cancelled.");
        }
    }

    private void generateReports() throws SQLException {
        YearMonth period = readYearMonth();
        System.out.println("1. Monthly sales summary");
        System.out.println("2. Campus-gate delivery report");
        System.out.println("3. Top-selling menu items");
        System.out.println("4. Order volume by time of day");
        System.out.print("Select report: ");
        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1" -> System.out.print(reportService.getMonthlySalesSummary(period));
            case "2" -> System.out.print(reportService.getCampusGateDeliveryReport(period));
            case "3" -> System.out.print(reportService.getTopSellingItemsReport(period));
            case "4" -> System.out.print(reportService.getOrderVolumeByTimeOfDayReport(period));
            default -> System.out.println("Invalid option.");
        }
    }

    private YearMonth readYearMonth() {
        System.out.print("Year (YYYY): ");
        int year = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Month (1-12): ");
        int month = Integer.parseInt(scanner.nextLine().trim());
        return YearMonth.of(year, month);
    }

    private Integer chooseActiveGate() throws SQLException {
        List<Gate> gates = orderService.getActiveGates();
        if (gates.isEmpty()) {
            throw new IllegalArgumentException("No active gates are available.");
        }

        System.out.println("Active gates:");
        for (Gate gate : gates) {
            System.out.println(gate.getGateId() + ". " + gate);
        }

        System.out.print("Gate ID: ");
        return Integer.parseInt(scanner.nextLine().trim());
    }
}
