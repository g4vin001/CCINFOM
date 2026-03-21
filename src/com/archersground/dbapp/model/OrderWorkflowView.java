package com.archersground.dbapp.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderWorkflowView {
    private final int orderId;
    private final LocalDateTime orderDateTime;
    private final OrderType orderType;
    private final OrderStatus orderStatus;
    private final BigDecimal totalAmount;
    private final String customerName;
    private final String gateName;

    public OrderWorkflowView(
        int orderId,
        LocalDateTime orderDateTime,
        OrderType orderType,
        OrderStatus orderStatus,
        BigDecimal totalAmount,
        String customerName,
        String gateName
    ) {
        this.orderId = orderId;
        this.orderDateTime = orderDateTime;
        this.orderType = orderType;
        this.orderStatus = orderStatus;
        this.totalAmount = totalAmount;
        this.customerName = customerName;
        this.gateName = gateName;
    }

    public int getOrderId() {
        return orderId;
    }

    public LocalDateTime getOrderDateTime() {
        return orderDateTime;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getGateName() {
        return gateName;
    }
}
