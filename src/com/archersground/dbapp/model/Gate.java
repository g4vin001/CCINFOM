package com.archersground.dbapp.model;

import java.math.BigDecimal;

public class Gate {
    private final int gateId;
    private final String gateName;
    private final BigDecimal deliveryFee;
    private final String status;

    public Gate(int gateId, String gateName, BigDecimal deliveryFee, String status) {
        this.gateId = gateId;
        this.gateName = gateName;
        this.deliveryFee = deliveryFee;
        this.status = status;
    }

    public int getGateId() {
        return gateId;
    }

    public String getGateName() {
        return gateName;
    }

    public BigDecimal getDeliveryFee() {
        return deliveryFee;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return gateName + " (Php " + deliveryFee + ")";
    }
}
