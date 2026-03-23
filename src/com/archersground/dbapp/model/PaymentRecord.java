package com.archersground.dbapp.model;

import java.math.BigDecimal;

public class PaymentRecord {
    private final int paymentId;
    private final BigDecimal amount;
    private final String status;

    public PaymentRecord(int paymentId, BigDecimal amount, String status) {
        this.paymentId = paymentId;
        this.amount = amount;
        this.status = status;
    }

    public int getPaymentId() {
        return paymentId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getStatus() {
        return status;
    }
}
