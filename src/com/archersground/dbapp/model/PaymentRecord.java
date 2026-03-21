package com.archersground.dbapp.model;

import java.math.BigDecimal;

public class PaymentRecord {
    private final BigDecimal amount;
    private final String status;

    public PaymentRecord(BigDecimal amount, String status) {
        this.amount = amount;
        this.status = status;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getStatus() {
        return status;
    }
}
