package com.archersground.dbapp.model;

public enum OrderStatus {
    CREATED,
    PAID,
    PREPARING,
    READY,
    OUT_FOR_DELIVERY,
    DELIVERED,
    COMPLETED,
    CANCELLED,
    REFUNDED,
    FAILED_DELIVERY
}
