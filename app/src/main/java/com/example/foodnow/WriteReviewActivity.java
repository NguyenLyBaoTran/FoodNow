package com.example.foodnow;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;
import java.io.IOException;

import androidx.appcompat.app.AppCompatActivity;

import com.example.foodnow.model.Review;
import com.example.foodnow.model.ReviewRequest;
import com.example.foodnow.network.ApiService;
import com.example.foodnow.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WriteReviewActivity extends AppCompatActivity {
    private RatingBar ratingBar;
    private EditText edtComment;
    private Button btnSubmitReview;
    private int foodId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_review);

        foodId = getIntent().getIntExtra("food_id", -1);

        ratingBar = findViewById(R.id.ratingBar);
        edtComment = findViewById(R.id.edtComment);
        btnSubmitReview = findViewById(R.id.btnSubmitReview);

        btnSubmitReview.setOnClickListener(v -> submitReview());
    }

    private void submitReview() {
        String comment = edtComment.getText().toString().trim();
        float rating = ratingBar.getRating();

        if (foodId <= 0) {
            Toast.makeText(this, "Food information is missing", Toast.LENGTH_SHORT).show();
            return;
        }

        if (rating <= 0) {
            Toast.makeText(this, "Please select a rating.", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSubmitReview.setEnabled(false);
        btnSubmitReview.setText("Submitting...");

        ReviewRequest request = new ReviewRequest(foodId, rating, comment);
        ApiService apiService = RetrofitClient.getApiService(this);
        apiService.createReview(request).enqueue(new Callback<Review>() {
            @Override
            public void onResponse(Call<Review> call, Response<Review> response) {
                btnSubmitReview.setEnabled(true);
                btnSubmitReview.setText("Submit Review");

                if (response.isSuccessful()) {
                    Toast.makeText(WriteReviewActivity.this, "Review submitted", Toast.LENGTH_SHORT).show();
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("review_submitted", true);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                } else {
                    String message = "Unable to submit review";
                    if (response.code() == 400 && response.errorBody() != null) {
                        try {
                            String error = response.errorBody().string();
                            if (error.contains("already reviewed")) {
                                message = "You already reviewed this food.";
                            } else if (error.contains("after completing")) {
                                message = "You can review this food after completing an order.";
                            }
                        } catch (IOException ignored) {
                            // Keep the generic fallback message.
                        }
                    }
                    Toast.makeText(WriteReviewActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Review> call, Throwable t) {
                btnSubmitReview.setEnabled(true);
                btnSubmitReview.setText("Submit Review");
                Toast.makeText(WriteReviewActivity.this, "Unable to connect. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
