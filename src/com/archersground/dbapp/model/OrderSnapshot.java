package com.archersground.dbapp.model;

import java.math.BigDecimal;

public class OrderSnapshot {
    private final int orderId;
    private final OrderType orderType;
    private final OrderStatus orderStatus;
    private final BigDecimal totalAmount;

    public OrderSnapshot(int orderId, OrderType orderType, OrderStatus orderStatus, BigDecimal totalAmount) {
        this.orderId = orderId;
        this.orderType = orderType;
        this.orderStatus = orderStatus;
        this.totalAmount = totalAmount;
    }

    public int getOrderId() {
        return orderId;
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
}
