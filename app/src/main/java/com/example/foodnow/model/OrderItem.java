package com.example.foodnow.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class OrderItem implements Serializable {
    private int id;
    private int quantity;
    @SerializedName("price_snapshot")
    private double priceSnapshot;
    private Food food;

    public int getId() { return id; }
    public int getQuantity() { return quantity; }
    public double getPriceSnapshot() { return priceSnapshot; }
    public Food getFood() { return food; }
}