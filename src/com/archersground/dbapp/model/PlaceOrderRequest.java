package com.archersground.dbapp.model;

import java.util.List;

public class PlaceOrderRequest {
    private final int customerId;
    private final int employeeId;
    private final Integer gateId;
    private final OrderType orderType;
    private final String paymentMethod;
    private final List<OrderItemRequest> items;

    public PlaceOrderRequest(
        int customerId,
        int employeeId,
        Integer gateId,
        OrderType orderType,
        String paymentMethod,
        List<OrderItemRequest> items
    ) {
        this.customerId = customerId;
        this.employeeId = employeeId;
        this.gateId = gateId;
        this.orderType = orderType;
        this.paymentMethod = paymentMethod;
        this.items = items;
    }

    public int getCustomerId() {
        return customerId;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public Integer getGateId() {
        return gateId;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public List<OrderItemRequest> getItems() {
        return items;
    }
}
