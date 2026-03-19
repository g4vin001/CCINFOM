package com.archersground.dbapp.model;

public class OrderItemRequest {
    private final int menuItemId;
    private final int quantity;

    public OrderItemRequest(int menuItemId, int quantity) {
        this.menuItemId = menuItemId;
        this.quantity = quantity;
    }

    public int getMenuItemId() {
        return menuItemId;
    }

    public int getQuantity() {
        return quantity;
    }
}
