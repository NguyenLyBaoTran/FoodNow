package com.example.foodnow.model;

import com.google.gson.annotations.SerializedName;

public class ReviewEligibility {
    @SerializedName("can_review")
    private boolean canReview;
    @SerializedName("already_reviewed")
    private boolean alreadyReviewed;
    private String message;

    public boolean canReview() { return canReview; }
    public boolean isAlreadyReviewed() { return alreadyReviewed; }
    public String getMessage() { return message; }
}