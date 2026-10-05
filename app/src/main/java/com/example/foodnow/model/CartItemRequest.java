package com.example.foodnow.model;

import com.google.gson.annotations.SerializedName;

public class CartItemRequest {
    @SerializedName("food_id")
    private int foodId;
    private int quantity;

    public CartItemRequest(int foodId, int quantity) {
        this.foodId = foodId;
        this.quantity = quantity;
    }
}
