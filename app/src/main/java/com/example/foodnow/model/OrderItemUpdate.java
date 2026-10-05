package com.example.foodnow.model;

public class OrderItemUpdate {
    private int quantity;

    public OrderItemUpdate(int quantity) {
        this.quantity = quantity;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
