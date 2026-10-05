package com.example.foodnow;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.foodnow.adapter.ReviewAdapter;
import com.example.foodnow.model.CartItem;
import com.example.foodnow.model.CartItemRequest;
import com.example.foodnow.model.Favorite;
import com.example.foodnow.model.Food;
import com.example.foodnow.model.Review;
import com.example.foodnow.network.ApiService;
import com.example.foodnow.network.RetrofitClient;
import com.example.foodnow.utils.ImageUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FoodDetailActivity extends AppCompatActivity {

    private ImageView ivFood, ivFavorite;
    private TextView tvName, tvPrice, tvDescription, tvReviewCount, tvReviewSummary, tvReviewEligibility;
    private Button btnAddToCart, btnWriteReview;
    private RecyclerView rvReviews;
    private ReviewAdapter reviewAdapter;
    private List<Review> reviewList = new ArrayList<>();
    private Food food;
    private boolean isFavorite = false;

    private final ActivityResultLauncher<Intent> writeReviewLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getBooleanExtra("review_submitted", false)) {
                    fetchReviews();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_detail);

        if (getIntent().hasExtra("food")) {
            food = (Food) getIntent().getSerializableExtra("food");
        }

        initViews();
        if (food != null) {
            updateUI();
            fetchReviews();
            checkReviewEligibility();
            checkIfFavorite();
        }

        btnAddToCart.setOnClickListener(v -> addToCart());
        btnWriteReview.setOnClickListener(v -> openWriteReview());
        ivFavorite.setOnClickListener(v -> toggleFavorite());
    }

    private void initViews() {
        ivFood = findViewById(R.id.ivFood);
        ivFavorite = findViewById(R.id.ivFavorite);
        tvName = findViewById(R.id.tvFoodName);
        tvPrice = findViewById(R.id.tvPrice);
        tvDescription = findViewById(R.id.tvDescription);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        btnWriteReview = findViewById(R.id.btnWriteReview);
        tvReviewCount = findViewById(R.id.tvReviewCount);
        tvReviewSummary = findViewById(R.id.tvReviewSummary);
        tvReviewEligibility = findViewById(R.id.tvReviewEligibility);
        rvReviews = findViewById(R.id.rvReviews);
        
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        reviewAdapter = new ReviewAdapter(reviewList);
        rvReviews.setLayoutManager(new LinearLayoutManager(this));
        rvReviews.setAdapter(reviewAdapter);
    }

    private void updateUI() {
        tvName.setText(food.getName());
        tvPrice.setText(String.format("%,d VND", food.getPrice()));
        tvDescription.setText(food.getDescription());
        Glide.with(this)
                .load(ImageUtils.getDrawableId(this, food.getImageUrl()))
                .placeholder(R.drawable.dish)
                .into(ivFood);
    }

    private void fetchReviews() {
        ApiService apiService = RetrofitClient.getApiService(this);
        apiService.getReviews(food.getId()).enqueue(new Callback<List<Review>>() {
            @Override
            public void onResponse(Call<List<Review>> call, Response<List<Review>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    reviewList.clear();
                    reviewList.addAll(response.body());
                    reviewAdapter.notifyDataSetChanged();
                    tvReviewCount.setText("Reviews (" + reviewList.size() + ")");
                    float totalRating = 0;
                    for (Review review : reviewList) totalRating += review.getRating();
                    float average = reviewList.isEmpty() ? 0 : totalRating / reviewList.size();
                    tvReviewSummary.setText(String.format("%.1f/5  |  Based on %d reviews", average, reviewList.size()));
                } else {
                    tvReviewCount.setText("Reviews (0)");
                    tvReviewSummary.setText("No reviews yet");
                }
            }

            @Override
            public void onFailure(Call<List<Review>> call, Throwable t) {
                tvReviewCount.setText("Reviews (0)");
                tvReviewSummary.setText("Reviews unavailable");
            }
        });
    }

    private void checkReviewEligibility() {
        ApiService apiService = RetrofitClient.getApiService(this);
        apiService.getReviewEligibility(food.getId()).enqueue(new Callback<com.example.foodnow.model.ReviewEligibility>() {
            @Override
            public void onResponse(Call<com.example.foodnow.model.ReviewEligibility> call, Response<com.example.foodnow.model.ReviewEligibility> response) {
                if (response.isSuccessful() && response.body() != null) {
                    com.example.foodnow.model.ReviewEligibility eligibility = response.body();
                    btnWriteReview.setVisibility(View.VISIBLE);
                    if (eligibility.canReview()) {
                        btnWriteReview.setText("Write Review");
                        btnWriteReview.setEnabled(true);
                        tvReviewEligibility.setText("");
                    } else {
                        btnWriteReview.setText(eligibility.isAlreadyReviewed() ? "Already Reviewed" : "Not Eligible");
                        btnWriteReview.setEnabled(false);
                        tvReviewEligibility.setText(eligibility.getMessage());
                    }
                } else {
                    btnWriteReview.setText("Login to Review");
                    btnWriteReview.setEnabled(false);
                    tvReviewEligibility.setText("Please log in to write a review.");
                }
            }

            @Override
            public void onFailure(Call<com.example.foodnow.model.ReviewEligibility> call, Throwable t) {
                btnWriteReview.setText("Login to Review");
                btnWriteReview.setEnabled(false);
                tvReviewEligibility.setText("Please log in to write a review.");
            }
        });
    }

    private void checkIfFavorite() {
        // Simple mock for now, or check from getFavorites
    }

    private void openWriteReview() {
        Intent intent = new Intent(this, WriteReviewActivity.class);
        intent.putExtra("food_id", food.getId());
        writeReviewLauncher.launch(intent);
    }

    private void toggleFavorite() {
        ApiService apiService = RetrofitClient.getApiService(this);
        if (!isFavorite) {
            apiService.addFavorite(food.getId()).enqueue(new Callback<Favorite>() {
                @Override
                public void onResponse(Call<Favorite> call, Response<Favorite> response) {
                    if (response.isSuccessful()) {
                        isFavorite = true;
                        ivFavorite.setColorFilter(getResources().getColor(R.color.colorPrimary));
                        Toast.makeText(FoodDetailActivity.this, "Added to Favorites", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Favorite> call, Throwable t) {}
            });
        } else {
            apiService.removeFavorite(food.getId()).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        isFavorite = false;
                        ivFavorite.setColorFilter(getResources().getColor(R.color.colorLightBlack));
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {}
            });
        }
    }

    private void addToCart() {
        ApiService apiService = RetrofitClient.getApiService(this);
        CartItemRequest request = new CartItemRequest(food.getId(), 1);
        apiService.addToCart(request).enqueue(new Callback<CartItem>() {
            @Override
            public void onResponse(Call<CartItem> call, Response<CartItem> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(FoodDetailActivity.this, "Added to cart", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CartItem> call, Throwable t) {}
        });
    }
}