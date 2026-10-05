package com.example.foodnow.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodnow.CartActivity;
import com.example.foodnow.FoodDetailActivity;
import com.example.foodnow.R;
import com.example.foodnow.RestaurantActivity;
import com.example.foodnow.adapter.RestaurantAdapter;
import com.example.foodnow.adapter.SearchResultAdapter;
import com.example.foodnow.model.Food;
import com.example.foodnow.model.Restaurant;
import com.example.foodnow.network.ApiService;
import com.example.foodnow.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment implements RestaurantAdapter.OnRestaurantClickListener {

    private RecyclerView rvRestaurants;
    private ProgressBar progressBar;
    private EditText etSearch;
    private TextView tvNoResults;
    private RestaurantAdapter adapter;
    private SearchResultAdapter searchAdapter;
    private final List<Restaurant> restaurantList = new ArrayList<>();
    private final List<Food> foodList = new ArrayList<>();
    private final List<SearchResultAdapter.SearchResultItem> searchResults = new ArrayList<>();
    private boolean isSearching = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        rvRestaurants = view.findViewById(R.id.rvRestaurants);
        progressBar = view.findViewById(R.id.progressBar);
        etSearch = view.findViewById(R.id.etSearch);
        tvNoResults = view.findViewById(R.id.tvNoResults);

        view.findViewById(R.id.btnViewCart).setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), CartActivity.class);
            startActivity(intent);
        });

        rvRestaurants.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new RestaurantAdapter(restaurantList, this);
        searchAdapter = new SearchResultAdapter(searchResults, new SearchResultAdapter.OnSearchResultClickListener() {
            @Override
            public void onRestaurantClick(Restaurant restaurant) {
                openRestaurantDetail(restaurant);
            }

            @Override
            public void onFoodClick(Food food) {
                openFoodDetail(food);
            }
        });
        rvRestaurants.setAdapter(adapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applySearch(s == null ? "" : s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        fetchRestaurants();
        return view;
    }

    private void fetchRestaurants() {
        progressBar.setVisibility(View.VISIBLE);
        ApiService apiService = RetrofitClient.getApiService(requireContext());
        apiService.getRestaurants().enqueue(new Callback<List<Restaurant>>() {
            @Override
            public void onResponse(Call<List<Restaurant>> call, Response<List<Restaurant>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    restaurantList.clear();
                    restaurantList.addAll(response.body());
                    loadRestaurantFoodsForSearch();
                    applySearch(etSearch.getText() == null ? "" : etSearch.getText().toString());
                } else {
                    Toast.makeText(requireContext(), "Failed to fetch restaurants", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Restaurant>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadRestaurantFoodsForSearch() {
        foodList.clear();
        if (restaurantList.isEmpty()) {
            return;
        }

        final int[] pendingRequests = {restaurantList.size()};
        ApiService apiService = RetrofitClient.getApiService(requireContext());

        for (Restaurant restaurant : restaurantList) {
            apiService.getRestaurantDetail(restaurant.getId()).enqueue(new Callback<Restaurant>() {
                @Override
                public void onResponse(Call<Restaurant> call, Response<Restaurant> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Restaurant detail = response.body();
                        ArrayList<Food> foods = new ArrayList<>();

                        if (detail.getCategories() != null) {
                            for (com.example.foodnow.model.Category category : detail.getCategories()) {
                                if (category != null && category.getFoods() != null) {
                                    foods.addAll(category.getFoods());
                                }
                            }
                        }

                        if (detail.getMenu() != null) {
                            for (Food food : detail.getMenu()) {
                                if (food != null) {
                                    boolean exists = false;
                                    for (Food existing : foods) {
                                        if (existing != null && existing.getId() == food.getId()) {
                                            exists = true;
                                            break;
                                        }
                                    }
                                    if (!exists) {
                                        foods.add(food);
                                    }
                                }
                            }
                        }

                        foodList.addAll(foods);
                    }

                    pendingRequests[0]--;
                    if (pendingRequests[0] == 0) {
                        applySearch(etSearch.getText() == null ? "" : etSearch.getText().toString());
                    }
                }

                @Override
                public void onFailure(Call<Restaurant> call, Throwable t) {
                    pendingRequests[0]--;
                    if (pendingRequests[0] == 0) {
                        applySearch(etSearch.getText() == null ? "" : etSearch.getText().toString());
                    }
                }
            });
        }
    }

    private void applySearch(String keyword) {
        String trimmed = keyword == null ? "" : keyword.trim();
        isSearching = !trimmed.isEmpty();

        if (!isSearching) {
            rvRestaurants.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            tvNoResults.setVisibility(View.GONE);
            rvRestaurants.setVisibility(View.VISIBLE);
            return;
        }

        String query = trimmed.toLowerCase();
        List<SearchResultAdapter.SearchResultItem> matchedResults = new ArrayList<>();

        for (Restaurant restaurant : restaurantList) {
            if (restaurant == null) continue;

            if (restaurant.getName() != null && restaurant.getName().toLowerCase().contains(query)) {
                matchedResults.add(new SearchResultAdapter.SearchResultItem(
                        SearchResultAdapter.SearchResultItem.Type.RESTAURANT,
                        restaurant,
                        null
                ));
            }
        }

        for (Food food : foodList) {
            if (food == null) continue;
            if (food.getName() != null && food.getName().toLowerCase().contains(query)) {
                matchedResults.add(new SearchResultAdapter.SearchResultItem(
                        SearchResultAdapter.SearchResultItem.Type.FOOD,
                        null,
                    food,
                    findRestaurantForFood(food)
                ));
            }
        }

        searchResults.clear();
        searchResults.addAll(matchedResults);

        if (searchResults.isEmpty()) {
            rvRestaurants.setAdapter(adapter);
            rvRestaurants.setVisibility(View.GONE);
            tvNoResults.setVisibility(View.VISIBLE);
        } else {
            rvRestaurants.setAdapter(searchAdapter);
            rvRestaurants.setVisibility(View.VISIBLE);
            tvNoResults.setVisibility(View.GONE);
            searchAdapter.notifyDataSetChanged();
        }
    }

    private List<Restaurant> getAllRestaurantsSnapshot() {
        return new ArrayList<>(restaurantList);
    }

    private Restaurant findRestaurantForFood(Food food) {
        for (Restaurant restaurant : restaurantList) {
            if (restaurant != null && restaurant.getId() == food.getRestaurantId()) {
                return restaurant;
            }
        }
        return null;
    }

    @Override
    public void onRestaurantClick(Restaurant restaurant) {
        openRestaurantDetail(restaurant);
    }

    private void openRestaurantDetail(Restaurant restaurant) {
        Intent intent = new Intent(requireContext(), RestaurantActivity.class);
        intent.putExtra("restaurant", restaurant);
        startActivity(intent);
    }

    public void openFoodDetail(Food food) {
        Intent intent = new Intent(requireContext(), FoodDetailActivity.class);
        intent.putExtra("food", food);
        startActivity(intent);
    }
}
