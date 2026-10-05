package com.example.foodnow.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.foodnow.R;
import com.example.foodnow.model.Food;
import com.example.foodnow.model.Restaurant;
import com.example.foodnow.utils.ImageUtils;

import java.util.List;

public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ViewHolder> {

    public interface OnSearchResultClickListener {
        void onRestaurantClick(Restaurant restaurant);
        void onFoodClick(Food food);
    }

    public static class SearchResultItem {
        public enum Type { RESTAURANT, FOOD }

        private final Type type;
        private final Restaurant restaurant;
        private final Food food;
        private final Restaurant foodRestaurant;

        public SearchResultItem(Type type, Restaurant restaurant, Food food) {
            this(type, restaurant, food, null);
        }

        public SearchResultItem(Type type, Restaurant restaurant, Food food, Restaurant foodRestaurant) {
            this.type = type;
            this.restaurant = restaurant;
            this.food = food;
            this.foodRestaurant = foodRestaurant;
        }

        public Type getType() {
            return type;
        }

        public Restaurant getRestaurant() {
            return restaurant;
        }

        public Food getFood() {
            return food;
        }

        public Restaurant getFoodRestaurant() {
            return foodRestaurant;
        }
    }

    private final List<SearchResultItem> items;
    private final OnSearchResultClickListener listener;

    public SearchResultAdapter(List<SearchResultItem> items, OnSearchResultClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_restaurant, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SearchResultItem item = items.get(position);

        if (item.getType() == SearchResultItem.Type.RESTAURANT) {
            Restaurant restaurant = item.getRestaurant();
            if (restaurant == null) return;

            holder.tvName.setText(restaurant.getName());
            holder.tvAddress.setText(restaurant.getAddress());
            
            if (restaurant.getCategories() != null && !restaurant.getCategories().isEmpty()) {
                holder.tvCategory.setText(restaurant.getCategories().get(0).getName());
                holder.tvCategory.setVisibility(View.VISIBLE);
            } else {
                holder.tvCategory.setVisibility(View.GONE);
            }

            Glide.with(holder.itemView.getContext())
                    .load(ImageUtils.getDrawableId(holder.itemView.getContext(), restaurant.getLogoUrl()))
                    .placeholder(R.drawable.dish)
                    .into(holder.ivCover);

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRestaurantClick(restaurant);
                }
            });
            return;
        }

        Food food = item.getFood();
        if (food == null) return;

        holder.tvName.setText(food.getName());
        String restaurantName = item.getFoodRestaurant() != null ? item.getFoodRestaurant().getName() : "Restaurant";
        holder.tvAddress.setText(String.format("%s • %,d VND", restaurantName, food.getPrice()));
        
        holder.tvCategory.setText("Food Result");
        holder.tvCategory.setVisibility(View.VISIBLE);

        String imageKey = food.getImageUrl();
        // Fallback to restaurant logo if food image key is missing
        if ((imageKey == null || imageKey.isEmpty()) && item.getFoodRestaurant() != null) {
            imageKey = item.getFoodRestaurant().getLogoUrl();
        }

        Glide.with(holder.itemView.getContext())
                .load(ImageUtils.getDrawableId(holder.itemView.getContext(), imageKey))
                .placeholder(R.drawable.dish)
                .into(holder.ivCover);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFoodClick(food);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCover;
        TextView tvName, tvAddress, tvCategory;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.ivCover);
            tvName = itemView.findViewById(R.id.tvName);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvCategory = itemView.findViewById(R.id.tvCategory);
        }
    }
}
