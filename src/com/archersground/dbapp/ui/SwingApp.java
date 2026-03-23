package com.archersground.dbapp.ui;

import com.archersground.dbapp.dao.MenuItemDao;
import com.archersground.dbapp.model.Gate;
import com.archersground.dbapp.model.MenuItem;
import com.archersground.dbapp.model.OrderItemRequest;
import com.archersground.dbapp.model.OrderStatus;
import com.archersground.dbapp.model.OrderType;
import com.archersground.dbapp.model.OrderWorkflowView;
import com.archersground.dbapp.model.PaymentMethod;
import com.archersground.dbapp.model.PlaceOrderRequest;
import com.archersground.dbapp.service.OrderService;
import com.archersground.dbapp.service.ReportService;
import com.archersground.dbapp.util.DatabaseConnection;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
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
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
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
    private static final Color COFFEE = new Color(133, 81, 42);
    private static final Color LATTE = new Color(224, 196, 139);

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
    private JComboBox<OrderType> orderTypeComboBox;
    private JComboBox<Gate> gateComboBox;
    private JTextArea orderItemsArea;
    private JComboBox<String> paymentMethodComboBox;
    private JTextField cancelRequestOrderIdField;
    private JTextField cancelRequestReasonField;

    private JTextArea preparationQueueArea;
    private JTextField readyOrderIdField;
    private JTextField readyEmployeeIdField;

    private JTextArea deliveryQueueArea;
    private JTextField deliveryOrderIdField;
    private JTextField deliveryEmployeeIdField;
    private JComboBox<OrderStatus> deliveryStatusComboBox;
    private JTextField deliveryNotesField;
    private JTextArea collectionQueueArea;
    private JTextField collectionOrderIdField;
    private JTextField collectionEmployeeIdField;

    private JTextArea cancellationQueueArea;
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
            refreshActiveGates();
            refreshWorkflowQueues();
        });
    }

    private JPanel buildContent() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        root.setBackground(CREAM);

        root.add(buildHeader(), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Customer Portal", buildCustomerPortal());
        tabs.addTab("Staff Portal", buildStaffPortal());
        tabs.setBackground(Color.WHITE);
        tabs.setForeground(FOREST);
        root.add(tabs, BorderLayout.CENTER);

        return root;
    }

    private JTabbedPane buildCustomerPortal() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Menu Items", buildMenuPanel());
        tabs.addTab("Place Order", buildPlaceOrderPanel());
        tabs.addTab("Cancel Order", buildCancelRequestPanel());
        return tabs;
    }

    private JTabbedPane buildStaffPortal() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Order Status", buildStatusPanel());
        tabs.addTab("Refunds", buildRefundPanel());
        tabs.addTab("Reports", buildReportsPanel());
        return tabs;
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(FOREST);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(GOLD, 2),
            BorderFactory.createEmptyBorder(18, 20, 18, 20)
        ));

        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        brandPanel.setOpaque(false);

        JLabel logoLabel = buildLogoLabel();
        if (logoLabel != null) {
            brandPanel.add(logoLabel);
        }

        JLabel heading = new JLabel("Archer's Ground");
        heading.setForeground(CREAM);
        heading.setFont(new Font("Serif", Font.BOLD, 28));

        JLabel subheading = new JLabel("Where Coffee Keeps You Grounded.");
        subheading.setForeground(new Color(220, 228, 218));
        subheading.setFont(new Font("SansSerif", Font.PLAIN, 14));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        textPanel.add(heading);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(subheading);
        brandPanel.add(textPanel);

        JPanel accent = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        accent.setOpaque(false);
        accent.add(createAccentBlock(GOLD));
        accent.add(createAccentBlock(MOSS));
        accent.add(createAccentBlock(CREAM));

        header.add(brandPanel, BorderLayout.WEST);
        header.add(accent, BorderLayout.EAST);
        return header;
    }

    private JLabel buildLogoLabel() {
        JLabel label = new JLabel(new ArcherLogoIcon(96, 96));
        label.setOpaque(false);
        return label;
    }

    private JPanel createAccentBlock(Color color) {
        JPanel block = new JPanel();
        block.setBackground(color);
        block.setPreferredSize(new Dimension(18, 18));
        return block;
    }

    private static final class ArcherLogoIcon implements Icon {
        private final int width;
        private final int height;

        private ArcherLogoIcon(int width, int height) {
            this.width = width;
            this.height = height;
        }

        @Override
        public int getIconWidth() {
            return width;
        }

        @Override
        public int getIconHeight() {
            return height;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.translate(x, y);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.scale(width / 100.0, height / 100.0);

            drawArrow(g2);
            drawCup(g2);
            drawSteam(g2);
            drawArrowCut(g2);

            g2.dispose();
        }

        private void drawArrow(Graphics2D g2) {
            Path2D shaft = new Path2D.Double();
            shaft.moveTo(7, 55);
            shaft.lineTo(80, 54);
            shaft.lineTo(80, 58);
            shaft.lineTo(7, 59);
            shaft.closePath();

            Path2D tail = new Path2D.Double();
            tail.moveTo(1, 56);
            tail.lineTo(8, 51);
            tail.lineTo(17, 52);
            tail.lineTo(13, 57);
            tail.lineTo(17, 62);
            tail.lineTo(8, 63);
            tail.closePath();

            Path2D head = new Path2D.Double();
            head.moveTo(80, 49);
            head.lineTo(98, 56);
            head.lineTo(80, 63);
            head.lineTo(83, 58);
            head.lineTo(76, 58);
            head.lineTo(76, 54);
            head.lineTo(83, 54);
            head.closePath();

            g2.setColor(CREAM);
            g2.fill(shaft);
            g2.fill(tail);
            g2.fill(head);
        }

        private void drawCup(Graphics2D g2) {
            Path2D body = new Path2D.Double();
            body.moveTo(21, 41);
            body.curveTo(18, 56, 23, 72, 36, 79);
            body.curveTo(49, 84, 62, 81, 70, 71);
            body.curveTo(74, 64, 75, 52, 72, 41);
            body.closePath();

            g2.setColor(CREAM);
            g2.fill(body);

            Ellipse2D rim = new Ellipse2D.Double(15, 32, 62, 15);
            g2.fill(rim);

            g2.setColor(COFFEE);
            g2.fill(new Ellipse2D.Double(20, 35.5, 53, 9.5));

            Path2D innerShade = new Path2D.Double();
            innerShade.moveTo(29, 50);
            innerShade.curveTo(27, 60, 29, 72, 35, 78);
            innerShade.curveTo(31, 76, 26, 72, 23, 66);
            innerShade.curveTo(21, 60, 21, 54, 22, 48);
            innerShade.closePath();
            g2.setColor(LATTE);
            g2.fill(innerShade);

            g2.setColor(CREAM);
            g2.setStroke(new BasicStroke(6.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(new Arc2D.Double(59, 46, 18, 24, -95, 250, Arc2D.OPEN));
        }

        private void drawSteam(Graphics2D g2) {
            g2.setColor(CREAM);
            g2.setStroke(new BasicStroke(5.1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            Path2D left = new Path2D.Double();
            left.moveTo(39, 31);
            left.curveTo(33, 24, 34, 16, 39, 10);
            left.curveTo(42, 6, 43, 3, 42, 0);
            g2.draw(left);

            Path2D right = new Path2D.Double();
            right.moveTo(50, 31);
            right.curveTo(46, 23, 48, 14, 54, 7);
            right.curveTo(57, 3, 58, 0, 57, -3);
            g2.draw(right);

            Path2D center = new Path2D.Double();
            center.moveTo(45, 31);
            center.curveTo(41, 22, 43, 13, 48, 6);
            center.curveTo(51, 2, 52, -1, 51, -4);
            g2.draw(center);
        }

        private void drawArrowCut(Graphics2D g2) {
            g2.setColor(FOREST);

            Path2D cut = new Path2D.Double();
            cut.moveTo(14, 60);
            cut.lineTo(51, 55);
            cut.lineTo(52, 57.7);
            cut.lineTo(16, 62.5);
            cut.closePath();
            g2.fill(cut);
        }
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
        orderTypeComboBox = new JComboBox<>(OrderType.values());
        gateComboBox = new JComboBox<>(new DefaultComboBoxModel<>());
        orderItemsArea = new JTextArea(7, 20);
        paymentMethodComboBox = new JComboBox<>(new String[]{"CASH", "GCASH", "CARD"});

        orderTypeComboBox.addActionListener(event -> updateGateFieldState());

        JPanel form = createFormPanel();
        addField(form, 0, "Customer ID", customerIdField);
        addField(form, 1, "Order Type", orderTypeComboBox);
        addField(form, 2, "Gate", gateComboBox);

        JScrollPane itemsScrollPane = new JScrollPane(orderItemsArea);
        itemsScrollPane.setPreferredSize(new Dimension(320, 130));
        addField(form, 3, "Items", itemsScrollPane);
        addField(form, 4, "Payment Method", paymentMethodComboBox);

        JTextArea helpText = new JTextArea(
            "Customer mode places the order directly.\n" +
            "Staff processing is handled in the Staff Portal.\n\n" +
            "Enter one order item per line using: menuItemId,quantity\n" +
            "Example:\n3,1\n17,2"
        );
        helpText.setEditable(false);
        helpText.setOpaque(false);
        helpText.setLineWrap(true);
        helpText.setWrapStyleWord(true);
        addField(form, 5, "Format", helpText);

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

    private JPanel buildCancelRequestPanel() {
        cancelRequestOrderIdField = new JTextField();
        cancelRequestReasonField = new JTextField();

        JPanel form = createFormPanel();
        addField(form, 0, "Order ID", cancelRequestOrderIdField);
        addField(form, 1, "Reason", cancelRequestReasonField);

        JTextArea helpText = new JTextArea(
            "Customer cancellation requests are submitted here.\n" +
            "If payment already exists, the system will automatically refund the paid amount when allowed."
        );
        helpText.setEditable(false);
        helpText.setOpaque(false);
        helpText.setLineWrap(true);
        helpText.setWrapStyleWord(true);
        addField(form, 2, "Notes", helpText);

        JButton submitButton = new JButton("Request Cancellation");
        submitButton.addActionListener(event -> requestCancellation());
        styleButton(submitButton);

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(CREAM);
        panel.add(form, BorderLayout.NORTH);
        panel.add(submitButton, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildStatusPanel() {
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setBackground(CREAM);

        preparationQueueArea = createReadOnlyTextArea(6);
        JPanel queuePanel = createInfoPanel("Paid / Preparing Orders", preparationQueueArea);

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

        deliveryQueueArea = createReadOnlyTextArea(6);
        JPanel deliveryQueuePanel = createInfoPanel("Ready / Out-for-Delivery Orders", deliveryQueueArea);

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

        collectionQueueArea = createReadOnlyTextArea(6);
        JPanel collectionQueuePanel = createInfoPanel("Ready Dine-In / Pick-Up Orders", collectionQueueArea);

        collectionOrderIdField = new JTextField();
        collectionEmployeeIdField = new JTextField();
        JPanel collectionPanel = createFormPanel();
        collectionPanel.setBorder(BorderFactory.createTitledBorder("Complete Dine-In / Pick-Up Order"));
        addField(collectionPanel, 0, "Order ID", collectionOrderIdField);
        addField(collectionPanel, 1, "Employee ID", collectionEmployeeIdField);
        JButton collectionButton = new JButton("Mark Completed");
        collectionButton.addActionListener(event -> completeCollectionOrder());
        styleButton(collectionButton);
        addField(collectionPanel, 2, "", collectionButton);

        wrapper.add(queuePanel);
        wrapper.add(Box.createVerticalStrut(12));
        wrapper.add(readyPanel);
        wrapper.add(Box.createVerticalStrut(12));
        wrapper.add(deliveryQueuePanel);
        wrapper.add(Box.createVerticalStrut(12));
        wrapper.add(deliveryPanel);
        wrapper.add(Box.createVerticalStrut(12));
        wrapper.add(collectionQueuePanel);
        wrapper.add(Box.createVerticalStrut(12));
        wrapper.add(collectionPanel);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CREAM);
        JScrollPane scrollPane = new JScrollPane(wrapper);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildRefundPanel() {
        cancellationQueueArea = createReadOnlyTextArea(7);
        refundOrderIdField = new JTextField();
        refundEmployeeIdField = new JTextField();
        refundAmountField = new JTextField("0.00");
        refundReasonField = new JTextField();

        JPanel queuePanel = createInfoPanel("Active Orders Eligible for Cancel / Refund", cancellationQueueArea);
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
        wrapper.add(queuePanel, BorderLayout.CENTER);
        wrapper.add(panel, BorderLayout.SOUTH);
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

    private JPanel createInfoPanel(String title, JTextArea textArea) {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(CREAM);
        panel.setBorder(BorderFactory.createTitledBorder(title));
        panel.add(new JScrollPane(textArea), BorderLayout.CENTER);
        return panel;
    }

    private JTextArea createReadOnlyTextArea(int rows) {
        JTextArea textArea = new JTextArea(rows, 20);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        return textArea;
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
        executeInBackground(
            () -> {
                try (Connection connection = DatabaseConnection.getConnection()) {
                    return menuItemDao.findAvailableItems(connection);
                }
            },
            items -> {
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
            }
        );
    }

    private void placeOrder() {
        try {
            PlaceOrderRequest request = new PlaceOrderRequest(
                parseInt(customerIdField.getText(), "Customer ID"),
                null,
                parseOptionalGateId(),
                (OrderType) orderTypeComboBox.getSelectedItem(),
                PaymentMethod.fromString((String) paymentMethodComboBox.getSelectedItem()),
                parseOrderItems()
            );

            executeInBackground(
                () -> orderService.placeOrder(request),
                orderId -> {
                    showMessage("Order created successfully. Order ID: " + orderId);
                    orderItemsArea.setText("");
                    gateComboBox.setSelectedItem(null);
                    refreshMenuItems();
                    refreshWorkflowQueues();
                }
            );
        } catch (IllegalArgumentException exception) {
            showError(exception);
        }
    }

    private void markOrderReady() {
        try {
            int orderId = parseInt(readyOrderIdField.getText(), "Order ID");
            int employeeId = parseInt(readyEmployeeIdField.getText(), "Employee ID");
            executeInBackground(
                () -> {
                    orderService.markOrderReady(orderId, employeeId);
                    return null;
                },
                ignored -> {
                    showMessage("Order updated to READY.");
                    refreshWorkflowQueues();
                }
            );
        } catch (IllegalArgumentException exception) {
            showError(exception);
        }
    }

    private void updateDeliveryStatus() {
        try {
            int orderId = parseInt(deliveryOrderIdField.getText(), "Order ID");
            OrderStatus status = (OrderStatus) deliveryStatusComboBox.getSelectedItem();
            int employeeId = parseInt(deliveryEmployeeIdField.getText(), "Employee ID");
            String notes = deliveryNotesField.getText().trim();
            executeInBackground(
                () -> {
                    orderService.updateDeliveryStatus(orderId, status, employeeId, notes);
                    return null;
                },
                ignored -> {
                    showMessage("Delivery status updated.");
                    refreshWorkflowQueues();
                }
            );
        } catch (IllegalArgumentException exception) {
            showError(exception);
        }
    }

    private void completeCollectionOrder() {
        try {
            int orderId = parseInt(collectionOrderIdField.getText(), "Order ID");
            int employeeId = parseInt(collectionEmployeeIdField.getText(), "Employee ID");
            executeInBackground(
                () -> {
                    orderService.completeCollectionOrder(orderId, employeeId);
                    return null;
                },
                ignored -> {
                    showMessage("Order updated to COMPLETED.");
                    refreshWorkflowQueues();
                }
            );
        } catch (IllegalArgumentException exception) {
            showError(exception);
        }
    }

    private void cancelOrRefundOrder() {
        try {
            BigDecimal refundAmount = new BigDecimal(refundAmountField.getText().trim());
            int orderId = parseInt(refundOrderIdField.getText(), "Order ID");
            int employeeId = parseInt(refundEmployeeIdField.getText(), "Employee ID");
            String reason = refundReasonField.getText().trim();
            executeInBackground(
                () -> {
                    orderService.cancelOrRefundOrder(orderId, employeeId, refundAmount, reason);
                    return null;
                },
                ignored -> {
                    if (refundAmount.compareTo(BigDecimal.ZERO) > 0) {
                        showMessage("Order refunded.");
                    } else {
                        showMessage("Order cancelled.");
                    }
                    refreshWorkflowQueues();
                }
            );
        } catch (IllegalArgumentException exception) {
            showError(exception);
        }
    }

    private void requestCancellation() {
        try {
            int orderId = parseInt(cancelRequestOrderIdField.getText(), "Order ID");
            String reason = cancelRequestReasonField.getText().trim();
            executeInBackground(
                () -> {
                    orderService.requestOrderCancellation(orderId, reason);
                    return null;
                },
                ignored -> {
                    showMessage("Customer cancellation processed.");
                    refreshWorkflowQueues();
                }
            );
        } catch (IllegalArgumentException exception) {
            showError(exception);
        }
    }

    private void generateReport() {
        try {
            YearMonth period = YearMonth.parse(reportYearMonthField.getText().trim());
            String selectedReport = (String) reportTypeComboBox.getSelectedItem();
            executeInBackground(
                () -> switch (selectedReport) {
                    case "Monthly Sales Summary" -> reportService.getMonthlySalesSummary(period);
                    case "Campus-Gate Delivery Report" -> reportService.getCampusGateDeliveryReport(period);
                    case "Top-Selling Menu Items" -> reportService.getTopSellingItemsReport(period);
                    case "Order Volume by Time of Day" -> reportService.getOrderVolumeByTimeOfDayReport(period);
                    default -> throw new IllegalArgumentException("Unsupported report type.");
                },
                reportText -> {
                    reportOutputArea.setText(reportText);
                    reportOutputArea.setCaretPosition(0);
                }
            );
        } catch (DateTimeParseException exception) {
            showError(new IllegalArgumentException("Year-Month must use the format YYYY-MM."));
        } catch (IllegalArgumentException exception) {
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

        Gate selectedGate = (Gate) gateComboBox.getSelectedItem();
        if (selectedGate == null) {
            throw new IllegalArgumentException("An active gate is required for campus-gate delivery.");
        }
        return selectedGate.getGateId();
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
        gateComboBox.setEnabled(enabled);
        if (!enabled) {
            gateComboBox.setSelectedItem(null);
        }
    }

    private void refreshActiveGates() {
        executeInBackground(
            orderService::getActiveGates,
            gates -> {
                DefaultComboBoxModel<Gate> model = new DefaultComboBoxModel<>();
                for (Gate gate : gates) {
                    model.addElement(gate);
                }
                gateComboBox.setModel(model);
                updateGateFieldState();
            }
        );
    }

    private void refreshWorkflowQueues() {
        refreshPreparationQueue();
        refreshDeliveryQueue();
        refreshCollectionQueue();
        refreshCancellationQueue();
    }

    private void refreshPreparationQueue() {
        executeInBackground(
            orderService::getPreparationQueue,
            orders -> preparationQueueArea.setText(formatWorkflowOrders(orders, false))
        );
    }

    private void refreshDeliveryQueue() {
        executeInBackground(
            orderService::getDeliveryQueue,
            orders -> deliveryQueueArea.setText(formatWorkflowOrders(orders, true))
        );
    }

    private void refreshCollectionQueue() {
        executeInBackground(
            orderService::getCollectionQueue,
            orders -> collectionQueueArea.setText(formatWorkflowOrders(orders, false))
        );
    }

    private void refreshCancellationQueue() {
        executeInBackground(
            orderService::getCancellationQueue,
            orders -> cancellationQueueArea.setText(formatWorkflowOrders(orders, true))
        );
    }

    private String formatWorkflowOrders(List<OrderWorkflowView> orders, boolean includeGate) {
        if (orders.isEmpty()) {
            return "No matching orders.";
        }

        StringBuilder builder = new StringBuilder();
        for (OrderWorkflowView order : orders) {
            builder.append("Order #").append(order.getOrderId())
                .append(" | ").append(order.getOrderDateTime())
                .append(" | ").append(order.getCustomerName())
                .append(" | ").append(order.getOrderType())
                .append(" | ").append(order.getOrderStatus())
                .append(" | Php ").append(order.getTotalAmount());
            if (includeGate && order.getGateName() != null) {
                builder.append(" | ").append(order.getGateName());
            }
            builder.append('\n');
        }
        return builder.toString();
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

    private <T> void executeInBackground(BackgroundTask<T> task, UiSuccessHandler<T> onSuccess) {
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return task.run();
            }

            @Override
            protected void done() {
                try {
                    onSuccess.accept(get());
                } catch (Exception exception) {
                    Throwable cause = exception.getCause() != null ? exception.getCause() : exception;
                    if (cause instanceof Exception appException) {
                        showError(appException);
                    } else {
                        showError(new RuntimeException(cause));
                    }
                }
            }
        }.execute();
    }

    @FunctionalInterface
    private interface BackgroundTask<T> {
        T run() throws Exception;
    }

    @FunctionalInterface
    private interface UiSuccessHandler<T> {
        void accept(T value);
    }
}
