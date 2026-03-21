package com.archersground.dbapp.model;

import java.util.List;

public class PlaceOrderRequest {
    private final int customerId;
    private final Integer employeeId;
    private final Integer gateId;
    private final OrderType orderType;
    private final PaymentMethod paymentMethod;
    private final List<OrderItemRequest> items;

    public PlaceOrderRequest(
        int customerId,
        Integer employeeId,
        Integer gateId,
        OrderType orderType,
        PaymentMethod paymentMethod,
        List<OrderItemRequest> items
    ) {
        this.customerId = customerId;
        this.employeeId = employeeId;
        this.gateId = gateId;
        this.orderType = orderType;
        this.paymentMethod = paymentMethod;
        this.items = List.copyOf(items);
    }

    public int getCustomerId() {
        return customerId;
    }

    public Integer getEmployeeId() {
        return employeeId;
    }

    public Integer getGateId() {
        return gateId;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public List<OrderItemRequest> getItems() {
        return items;
    }
}
