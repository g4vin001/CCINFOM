package com.archersground.dbapp.model;

public enum PaymentMethod {
    CASH,
    GCASH,
    CARD;

    public static PaymentMethod fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Payment method is required.");
        }

        try {
            return PaymentMethod.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Payment method must be CASH, GCASH, or CARD.");
        }
    }
}
