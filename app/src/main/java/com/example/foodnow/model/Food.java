package com.example.foodnow.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Food implements Serializable {
    private int id;
    private String name;
    private String description;
    private int price;
    @SerializedName("image_url")
    private String imageUrl;
    @SerializedName("restaurant_id")
    private int restaurantId;
    @SerializedName("category_id")
    private int categoryId;

    public Food(String name, int price, String imageUrl) {
        this.name = name;
        this.price = price;
        this.imageUrl = imageUrl;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getPrice() { return price; }
    public String getImageUrl() { return imageUrl; }
    public int getRestaurantId() { return restaurantId; }
    public int getCategoryId() { return categoryId; }
}
