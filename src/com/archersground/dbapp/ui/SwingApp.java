package com.archersground.dbapp.ui;

import com.archersground.dbapp.dao.MenuItemDao;
import com.archersground.dbapp.model.MenuItem;
import com.archersground.dbapp.model.OrderItemRequest;
import com.archersground.dbapp.model.OrderStatus;
import com.archersground.dbapp.model.OrderType;
import com.archersground.dbapp.model.PlaceOrderRequest;
import com.archersground.dbapp.service.OrderService;
import com.archersground.dbapp.service.ReportService;
import com.archersground.dbapp.util.DatabaseConnection;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class SwingApp {
    private static final Color CREAM = new Color(247, 240, 229);
    private static final Color FOREST = new Color(29, 78, 52);
    private static final Color MOSS = new Color(120, 148, 96);
    private static final Color GOLD = new Color(198, 152, 73);

    private final OrderService orderService = new OrderService();
    private final ReportService reportService = new ReportService();
    private final MenuItemDao menuItemDao = new MenuItemDao();

    private final DefaultTableModel menuTableModel = new DefaultTableModel(
        new Object[]{"ID", "Name", "Category", "Available", "Price"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private JTextField customerIdField;
    private JTextField employeeIdField;
    private JComboBox<OrderType> orderTypeComboBox;
    private JTextField gateIdField;
    private JTextArea orderItemsArea;
    private JComboBox<String> paymentMethodComboBox;

    private JTextField readyOrderIdField;
    private JTextField readyEmployeeIdField;

    private JTextField deliveryOrderIdField;
    private JTextField deliveryEmployeeIdField;
    private JComboBox<OrderStatus> deliveryStatusComboBox;
    private JTextField deliveryNotesField;

    private JTextField refundOrderIdField;
    private JTextField refundEmployeeIdField;
    private JTextField refundAmountField;
    private JTextField refundReasonField;

    private JTextField reportYearMonthField;
    private JComboBox<String> reportTypeComboBox;
    private JTextArea reportOutputArea;

    public void start() {
        SwingUtilities.invokeLater(() -> {
            installLookAndFeel();

            JFrame frame = new JFrame("Archer's Ground");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(980, 700);
            frame.setLocationRelativeTo(null);
            frame.setContentPane(buildContent());
            frame.setVisible(true);

            refreshMenuItems();
        });
    }

    private JPanel buildContent() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        root.setBackground(CREAM);

        root.add(buildHeader(), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Menu Items", buildMenuPanel());
        tabs.addTab("Place Order", buildPlaceOrderPanel());
        tabs.addTab("Order Status", buildStatusPanel());
        tabs.addTab("Refunds", buildRefundPanel());
        tabs.addTab("Reports", buildReportsPanel());
        tabs.setBackground(Color.WHITE);
        tabs.setForeground(FOREST);
        root.add(tabs, BorderLayout.CENTER);

        return root;
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(FOREST);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(GOLD, 2),
            BorderFactory.createEmptyBorder(18, 20, 18, 20)
        ));

        JLabel heading = new JLabel("Archer's Ground");
        heading.setForeground(CREAM);
        heading.setFont(new Font("Serif", Font.BOLD, 28));

        JLabel subheading = new JLabel("Cafe Ordering and Reports");
        subheading.setForeground(new Color(220, 228, 218));
        subheading.setFont(new Font("SansSerif", Font.PLAIN, 14));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        textPanel.add(heading);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(subheading);

        JPanel accent = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        accent.setOpaque(false);
        accent.add(createAccentBlock(GOLD));
        accent.add(createAccentBlock(MOSS));
        accent.add(createAccentBlock(CREAM));

        header.add(textPanel, BorderLayout.WEST);
        header.add(accent, BorderLayout.EAST);
        return header;
    }

    private JPanel createAccentBlock(Color color) {
        JPanel block = new JPanel();
        block.setBackground(color);
        block.setPreferredSize(new Dimension(18, 18));
        return block;
    }

    private JPanel buildMenuPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(CREAM);
        JTable table = new JTable(menuTableModel);
        table.setRowHeight(24);
        table.setGridColor(new Color(220, 220, 220));
        table.setSelectionBackground(MOSS);
        table.setSelectionForeground(Color.WHITE);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JButton refreshButton = new JButton("Refresh Menu");
        refreshButton.addActionListener(event -> refreshMenuItems());
        styleButton(refreshButton);
        panel.add(refreshButton, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel buildPlaceOrderPanel() {
        customerIdField = new JTextField();
        employeeIdField = new JTextField();
        orderTypeComboBox = new JComboBox<>(OrderType.values());
        gateIdField = new JTextField();
        orderItemsArea = new JTextArea(7, 20);
        paymentMethodComboBox = new JComboBox<>(new String[]{"CASH", "GCASH", "CARD"});

        orderTypeComboBox.addActionListener(event -> updateGateFieldState());

        JPanel form = createFormPanel();
        addField(form, 0, "Customer ID", customerIdField);
        addField(form, 1, "Employee ID", employeeIdField);
        addField(form, 2, "Order Type", orderTypeComboBox);
        addField(form, 3, "Gate ID", gateIdField);

        JScrollPane itemsScrollPane = new JScrollPane(orderItemsArea);
        itemsScrollPane.setPreferredSize(new Dimension(320, 130));
        addField(form, 4, "Items", itemsScrollPane);
        addField(form, 5, "Payment Method", paymentMethodComboBox);

        JTextArea helpText = new JTextArea(
            "Enter one order item per line using: menuItemId,quantity\n" +
            "Example:\n3,1\n17,2"
        );
        helpText.setEditable(false);
        helpText.setOpaque(false);
        helpText.setLineWrap(true);
        helpText.setWrapStyleWord(true);
        addField(form, 6, "Format", helpText);

        JButton submitButton = new JButton("Place Order");
        submitButton.addActionListener(event -> placeOrder());
        styleButton(submitButton);

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(CREAM);
        panel.add(form, BorderLayout.NORTH);
        panel.add(submitButton, BorderLayout.SOUTH);

        updateGateFieldState();
        return panel;
    }

    private JPanel buildStatusPanel() {
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));

        readyOrderIdField = new JTextField();
        readyEmployeeIdField = new JTextField();
        JPanel readyPanel = createFormPanel();
        readyPanel.setBorder(BorderFactory.createTitledBorder("Mark Order Ready"));
        addField(readyPanel, 0, "Order ID", readyOrderIdField);
        addField(readyPanel, 1, "Employee ID", readyEmployeeIdField);
        JButton readyButton = new JButton("Mark Ready");
        readyButton.addActionListener(event -> markOrderReady());
        styleButton(readyButton);
        addField(readyPanel, 2, "", readyButton);

        deliveryOrderIdField = new JTextField();
        deliveryEmployeeIdField = new JTextField();
        deliveryStatusComboBox = new JComboBox<>(new OrderStatus[]{
            OrderStatus.OUT_FOR_DELIVERY,
            OrderStatus.DELIVERED,
            OrderStatus.FAILED_DELIVERY
        });
        deliveryNotesField = new JTextField();
        JPanel deliveryPanel = createFormPanel();
        deliveryPanel.setBorder(BorderFactory.createTitledBorder("Update Delivery Status"));
        addField(deliveryPanel, 0, "Order ID", deliveryOrderIdField);
        addField(deliveryPanel, 1, "Employee ID", deliveryEmployeeIdField);
        addField(deliveryPanel, 2, "New Status", deliveryStatusComboBox);
        addField(deliveryPanel, 3, "Notes", deliveryNotesField);
        JButton deliveryButton = new JButton("Update Delivery");
        deliveryButton.addActionListener(event -> updateDeliveryStatus());
        styleButton(deliveryButton);
        addField(deliveryPanel, 4, "", deliveryButton);

        wrapper.add(readyPanel);
        wrapper.add(Box.createVerticalStrut(12));
        wrapper.add(deliveryPanel);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CREAM);
        panel.add(wrapper, BorderLayout.NORTH);
        return panel;
    }

    private JPanel buildRefundPanel() {
        refundOrderIdField = new JTextField();
        refundEmployeeIdField = new JTextField();
        refundAmountField = new JTextField("0.00");
        refundReasonField = new JTextField();

        JPanel panel = createFormPanel();
        addField(panel, 0, "Order ID", refundOrderIdField);
        addField(panel, 1, "Employee ID", refundEmployeeIdField);
        addField(panel, 2, "Refund Amount", refundAmountField);
        addField(panel, 3, "Reason", refundReasonField);

        JButton submitButton = new JButton("Submit Cancel / Refund");
        submitButton.addActionListener(event -> cancelOrRefundOrder());
        styleButton(submitButton);
        addField(panel, 4, "", submitButton);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(CREAM);
        wrapper.add(panel, BorderLayout.NORTH);
        return wrapper;
    }

    private JPanel buildReportsPanel() {
        reportYearMonthField = new JTextField("2026-02");
        reportTypeComboBox = new JComboBox<>(new String[]{
            "Monthly Sales Summary",
            "Campus-Gate Delivery Report",
            "Top-Selling Menu Items",
            "Order Volume by Time of Day"
        });
        reportOutputArea = new JTextArea();
        reportOutputArea.setEditable(false);

        JPanel controls = createFormPanel();
        addField(controls, 0, "Year-Month", reportYearMonthField);
        addField(controls, 1, "Report", reportTypeComboBox);

        JButton generateButton = new JButton("Generate Report");
        generateButton.addActionListener(event -> generateReport());
        styleButton(generateButton);
        addField(controls, 2, "", generateButton);

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(CREAM);
        panel.add(controls, BorderLayout.NORTH);
        panel.add(new JScrollPane(reportOutputArea), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        panel.setBackground(CREAM);
        return panel;
    }

    private void addField(JPanel panel, int row, String label, java.awt.Component component) {
        GridBagConstraints left = new GridBagConstraints();
        left.gridx = 0;
        left.gridy = row;
        left.insets = new Insets(4, 4, 4, 8);
        left.anchor = GridBagConstraints.NORTHWEST;
        if (!label.isBlank()) {
            panel.add(new JLabel(label), left);
        }

        GridBagConstraints right = new GridBagConstraints();
        right.gridx = 1;
        right.gridy = row;
        right.weightx = 1.0;
        right.fill = GridBagConstraints.HORIZONTAL;
        right.insets = new Insets(4, 4, 4, 4);
        if (component instanceof JScrollPane) {
            right.fill = GridBagConstraints.BOTH;
            right.weighty = 1.0;
        }
        panel.add(component, right);
    }

    private void refreshMenuItems() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            List<MenuItem> items = menuItemDao.findAvailableItems(connection);
            menuTableModel.setRowCount(0);
            for (MenuItem item : items) {
                menuTableModel.addRow(new Object[]{
                    item.getMenuItemId(),
                    item.getItemName(),
                    item.getCategory(),
                    item.isAvailable() ? "Yes" : "No",
                    item.getPrice()
                });
            }
        } catch (SQLException exception) {
            showError(exception);
        }
    }

    private void placeOrder() {
        try {
            PlaceOrderRequest request = new PlaceOrderRequest(
                parseInt(customerIdField.getText(), "Customer ID"),
                parseInt(employeeIdField.getText(), "Employee ID"),
                parseOptionalGateId(),
                (OrderType) orderTypeComboBox.getSelectedItem(),
                (String) paymentMethodComboBox.getSelectedItem(),
                parseOrderItems()
            );

            int orderId = orderService.placeOrder(request);
            showMessage("Order created successfully. Order ID: " + orderId);
            orderItemsArea.setText("");
            gateIdField.setText("");
            refreshMenuItems();
        } catch (IllegalArgumentException | SQLException exception) {
            showError(exception);
        }
    }

    private void markOrderReady() {
        try {
            orderService.markOrderReady(
                parseInt(readyOrderIdField.getText(), "Order ID"),
                parseInt(readyEmployeeIdField.getText(), "Employee ID")
            );
            showMessage("Order updated to READY.");
        } catch (IllegalArgumentException | SQLException exception) {
            showError(exception);
        }
    }

    private void updateDeliveryStatus() {
        try {
            orderService.updateDeliveryStatus(
                parseInt(deliveryOrderIdField.getText(), "Order ID"),
                (OrderStatus) deliveryStatusComboBox.getSelectedItem(),
                parseInt(deliveryEmployeeIdField.getText(), "Employee ID"),
                deliveryNotesField.getText().trim()
            );
            showMessage("Delivery status updated.");
        } catch (IllegalArgumentException | SQLException exception) {
            showError(exception);
        }
    }

    private void cancelOrRefundOrder() {
        try {
            BigDecimal refundAmount = new BigDecimal(refundAmountField.getText().trim());
            orderService.cancelOrRefundOrder(
                parseInt(refundOrderIdField.getText(), "Order ID"),
                parseInt(refundEmployeeIdField.getText(), "Employee ID"),
                refundAmount,
                refundReasonField.getText().trim()
            );
            if (refundAmount.compareTo(BigDecimal.ZERO) > 0) {
                showMessage("Order refunded.");
            } else {
                showMessage("Order cancelled.");
            }
        } catch (IllegalArgumentException | SQLException exception) {
            showError(exception);
        }
    }

    private void generateReport() {
        try {
            YearMonth period = YearMonth.parse(reportYearMonthField.getText().trim());
            String selectedReport = (String) reportTypeComboBox.getSelectedItem();
            String reportText = switch (selectedReport) {
                case "Monthly Sales Summary" -> reportService.getMonthlySalesSummary(period);
                case "Campus-Gate Delivery Report" -> reportService.getCampusGateDeliveryReport(period);
                case "Top-Selling Menu Items" -> reportService.getTopSellingItemsReport(period);
                case "Order Volume by Time of Day" -> reportService.getOrderVolumeByTimeOfDayReport(period);
                default -> throw new IllegalArgumentException("Unsupported report type.");
            };
            reportOutputArea.setText(reportText);
            reportOutputArea.setCaretPosition(0);
        } catch (DateTimeParseException exception) {
            showError(new IllegalArgumentException("Year-Month must use the format YYYY-MM."));
        } catch (IllegalArgumentException | SQLException exception) {
            showError(exception);
        }
    }

    private List<OrderItemRequest> parseOrderItems() {
        String text = orderItemsArea.getText().trim();
        if (text.isEmpty()) {
            throw new IllegalArgumentException("At least one order item is required.");
        }

        List<OrderItemRequest> items = new ArrayList<>();
        String[] lines = text.split("\\R");
        for (String line : lines) {
            String[] parts = line.split(",");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Each item line must follow menuItemId,quantity.");
            }

            int menuItemId = parseInt(parts[0], "Menu item ID");
            int quantity = parseInt(parts[1], "Quantity");
            if (quantity <= 0) {
                throw new IllegalArgumentException("Quantity must be greater than zero.");
            }
            items.add(new OrderItemRequest(menuItemId, quantity));
        }
        return items;
    }

    private Integer parseOptionalGateId() {
        OrderType orderType = (OrderType) orderTypeComboBox.getSelectedItem();
        if (orderType != OrderType.CAMPUS_GATE_DELIVERY) {
            return null;
        }

        String gateText = gateIdField.getText().trim();
        if (gateText.isEmpty()) {
            throw new IllegalArgumentException("Gate ID is required for campus-gate delivery.");
        }
        return parseInt(gateText, "Gate ID");
    }

    private int parseInt(String value, String fieldName) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(fieldName + " must be a whole number.");
        }
    }

    private void updateGateFieldState() {
        OrderType orderType = (OrderType) orderTypeComboBox.getSelectedItem();
        boolean enabled = orderType == OrderType.CAMPUS_GATE_DELIVERY;
        gateIdField.setEnabled(enabled);
        if (!enabled) {
            gateIdField.setText("");
        }
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(null, message, "Archer's Ground DB App", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(Exception exception) {
        JOptionPane.showMessageDialog(
            null,
            exception.getMessage(),
            "Operation Failed",
            JOptionPane.ERROR_MESSAGE
        );
    }

    private void installLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
    }

    private void styleButton(JButton button) {
        button.setBackground(FOREST);
        button.setForeground(CREAM);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(GOLD, 1),
            BorderFactory.createEmptyBorder(8, 14, 8, 14)
        ));
    }
}
