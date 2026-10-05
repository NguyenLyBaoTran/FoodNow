package com.example.foodnow;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodnow.adapter.FoodAdapter;
import com.example.foodnow.model.Favorite;
import com.example.foodnow.model.Food;
import com.example.foodnow.network.ApiService;
import com.example.foodnow.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FavoritesActivity extends AppCompatActivity implements FoodAdapter.OnFoodItemClickListener {

    private RecyclerView rvFavorites;
    private FoodAdapter adapter;
    private List<Food> favoriteFoods = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        rvFavorites = findViewById(R.id.rvFavorites);
        adapter = new FoodAdapter(favoriteFoods, this);
        rvFavorites.setLayoutManager(new LinearLayoutManager(this));
        rvFavorites.setAdapter(adapter);

        fetchFavorites();
    }

    private void fetchFavorites() {
        ApiService apiService = RetrofitClient.getApiService(this);
        apiService.getFavorites().enqueue(new Callback<List<Favorite>>() {
            @Override
            public void onResponse(Call<List<Favorite>> call, Response<List<Favorite>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    favoriteFoods.clear();
                    for (Favorite f : response.body()) {
                        favoriteFoods.add(f.getFood());
                    }
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<Favorite>> call, Throwable t) {
                Toast.makeText(FavoritesActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onFoodItemClick(Food food) {
        // Handle food click
    }
}