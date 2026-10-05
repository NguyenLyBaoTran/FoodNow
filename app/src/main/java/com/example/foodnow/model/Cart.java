package com.example.foodnow.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class Cart implements Serializable {
    private int id;
    @SerializedName("user_id")
    private int userId;
    private List<CartItem> items;

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public List<CartItem> getItems() { return items; }
}
