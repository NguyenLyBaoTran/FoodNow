package com.example.foodnow.model;

import com.google.gson.annotations.SerializedName;

public class ReviewRequest {
    @SerializedName("food_id")
    private int foodId;

    @SerializedName("rating")
    private float rating;

    @SerializedName("comment")
    private String comment;

    public ReviewRequest(int foodId, float rating, String comment) {
        this.foodId = foodId;
        this.rating = rating;
        this.comment = comment;
    }

    public int getFoodId() {
        return foodId;
    }

    public void setFoodId(int foodId) {
        this.foodId = foodId;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
