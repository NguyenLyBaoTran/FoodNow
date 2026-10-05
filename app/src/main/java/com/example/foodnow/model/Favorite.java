package com.example.foodnow.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Favorite implements Serializable {
    private int id;
    @SerializedName("user_id")
    private int userId;
    @SerializedName("food_id")
    private int foodId;
    private Food food;

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public int getFoodId() { return foodId; }
    public Food getFood() { return food; }
}