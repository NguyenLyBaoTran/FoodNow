package com.example.foodnow;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodnow.model.Cart;
import com.example.foodnow.model.CartItem;
import com.example.foodnow.model.Category;
import com.example.foodnow.network.ApiService;
import com.example.foodnow.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.bumptech.glide.Glide;
import com.example.foodnow.adapter.FoodAdapter;
import com.example.foodnow.model.Food;
import com.example.foodnow.model.Restaurant;
import com.example.foodnow.utils.ImageUtils;

public class RestaurantActivity extends AppCompatActivity implements FoodAdapter.OnFoodItemClickListener {
    TextView tvName, tvAddress, tvOpenHour, tvTotalItem, tvTotalPrice;
    ImageView ivCover;
    ProgressBar progressBar;
    RecyclerView rvFoods;
    CardView layoutViewBasket;
    FoodAdapter foodAdapter;
    Restaurant restaurant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant);

        initViews();
        getDataFromIntent();
        updateUI();

        if (restaurant != null) {
            fetchRestaurantDetail(restaurant.getId());
        }

        layoutViewBasket.setOnClickListener(v -> {
            Intent intent = new Intent(RestaurantActivity.this, CartActivity.class);
            startActivity(intent);
        });
        fetchCartSummary();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchCartSummary();
    }

    private void fetchRestaurantDetail(int id) {
        progressBar.setVisibility(View.VISIBLE);
        ApiService apiService = RetrofitClient.getApiService(this);
        apiService.getRestaurantDetail(id).enqueue(new Callback<Restaurant>() {
            @Override
            public void onResponse(Call<Restaurant> call, Response<Restaurant> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    restaurant = response.body();
                    // Merge foods from all categories into a single list for the existing adapter
                    ArrayList<Food> allFoods = new ArrayList<>();
                    if (restaurant.getCategories() != null) {
                        for (Category category : restaurant.getCategories()) {
                            if (category.getFoods() != null) {
                                allFoods.addAll(category.getFoods());
                            }
                        }
                    }
                    // Also check the 'menu' or 'foods' list if it exists directly
                    if (restaurant.getMenu() != null) {
                        // Avoid duplicates if both are present
                        for (Food f : restaurant.getMenu()) {
                            boolean exists = false;
                            for (Food ef : allFoods) {
                                if (ef.getId() == f.getId()) { exists = true; break; }
                            }
                            if (!exists) allFoods.add(f);
                        }
                    }
                    
                    restaurant.setMenu(allFoods);
                    updateUI();
                    setupRecyclerView();
                }
            }

            @Override
            public void onFailure(Call<Restaurant> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(RestaurantActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initViews() {
        tvName = findViewById(R.id.tvName);
        tvAddress = findViewById(R.id.tvAddress);
        tvOpenHour = findViewById(R.id.tvOpenHour);
        ivCover = findViewById(R.id.ivCover);
        progressBar = findViewById(R.id.progressBar);
        rvFoods = findViewById(R.id.rvFoods);
        layoutViewBasket = findViewById(R.id.layoutViewBasket);
        tvTotalItem = findViewById(R.id.tvTotalItem);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        
        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void getDataFromIntent() {
        if (getIntent().hasExtra("restaurant")) {
            restaurant = (Restaurant) getIntent().getSerializableExtra("restaurant");
        }
    }

    private void updateUI() {
        if (restaurant != null) {
            tvName.setText(restaurant.getName());
            tvAddress.setText(restaurant.getAddress());
            tvOpenHour.setText("Open Hours: " + restaurant.getOpenHours());
            
            Glide.with(this)
                    .load(ImageUtils.getDrawableId(this, restaurant.getCoverUrl()))
                    .placeholder(R.drawable.dish)
                    .into(ivCover);
        }
    }

    private void setupRecyclerView() {
        if (restaurant != null && restaurant.getMenu() != null) {
            foodAdapter = new FoodAdapter(restaurant.getMenu(), this);
            rvFoods.setLayoutManager(new LinearLayoutManager(this));
            rvFoods.setAdapter(foodAdapter);
        }
    }

    private void fetchCartSummary() {
        ApiService apiService = RetrofitClient.getApiService(this);
        apiService.getCart().enqueue(new Callback<Cart>() {
            @Override
            public void onResponse(Call<Cart> call, Response<Cart> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<CartItem> items = response.body().getItems() != null ? response.body().getItems() : new ArrayList<>();
                    int totalItems = 0;
                    int totalPrice = 0;

                    for (CartItem item : items) {
                        if (item.getFood() != null) {
                            totalItems += item.getQuantity();
                            totalPrice += item.getFood().getPrice() * item.getQuantity();
                        }
                    }

                    tvTotalItem.setText(totalItems == 1 ? "1 item" : totalItems + " items");
                    tvTotalPrice.setText(String.format("%,d VND", totalPrice));
                } else {
                    tvTotalItem.setText("0 items");
                    tvTotalPrice.setText("0 VND");
                }
            }

            @Override
            public void onFailure(Call<Cart> call, Throwable t) {
                tvTotalItem.setText("0 items");
                tvTotalPrice.setText("0 VND");
            }
        });
    }

    @Override
    public void onFoodItemClick(Food food) {
        Intent intent = new Intent(this, FoodDetailActivity.class);
        intent.putExtra("food", food);
        startActivity(intent);
    }
}