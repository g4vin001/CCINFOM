package com.archersground.dbapp.model;

import java.math.BigDecimal;

public class MenuItem {
    private final int menuItemId;
    private final String itemName;
    private final String category;
    private final boolean available;
    private final BigDecimal price;

    public MenuItem(int menuItemId, String itemName, String category, boolean available, BigDecimal price) {
        this.menuItemId = menuItemId;
        this.itemName = itemName;
        this.category = category;
        this.available = available;
        this.price = price;
    }

    public int getMenuItemId() {
        return menuItemId;
    }

    public String getItemName() {
        return itemName;
    }

    public String getCategory() {
        return category;
    }

    public boolean isAvailable() {
        return available;
    }

    public BigDecimal getPrice() {
        return price;
    }

    @Override
    public String toString() {
        return menuItemId + " - " + itemName + " (" + category + ") Php " + price;
    }
}
