package com.example.foodnow.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class CartItem implements Serializable {
    private int id;
    @SerializedName("cart_id")
    private int cartId;
    @SerializedName("food_id")
    private int foodId;
    private int quantity;
    private Food food;

    public int getId() { return id; }
    public int getCartId() { return cartId; }
    public int getFoodId() { return foodId; }
    public int getQuantity() { return quantity; }
    public Food getFood() { return food; }

    public void setQuantity(int quantity) { this.quantity = quantity; }
}
