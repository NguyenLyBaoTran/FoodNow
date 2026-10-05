package com.example.foodnow.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodnow.R;
import com.example.foodnow.model.Restaurant;
import com.example.foodnow.utils.ImageUtils;

import java.util.List;

public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.ViewHolder> {

    public interface OnRestaurantClickListener {
        void onRestaurantClick(Restaurant restaurant);
    }

    private List<Restaurant> restaurantList;
    private OnRestaurantClickListener listener;

    public RestaurantAdapter(
            List<Restaurant> restaurantList,
            OnRestaurantClickListener listener
    ) {
        this.restaurantList = restaurantList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_restaurant, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {
        Restaurant restaurant = restaurantList.get(position);
        Context context = holder.itemView.getContext();

        // Restaurant information
        holder.tvName.setText(restaurant.getName());
        holder.tvAddress.setText(restaurant.getAddress());

        // Category
        if (restaurant.getCategories() != null
                && !restaurant.getCategories().isEmpty()) {

            holder.tvCategory.setText(
                    restaurant.getCategories().get(0).getName()
            );

            holder.tvCategory.setVisibility(View.VISIBLE);

        } else {
            holder.tvCategory.setVisibility(View.GONE);
        }

        // Get the exact drawable from logo_url
        int drawableId = ImageUtils.getDrawableId(
                context,
                restaurant.getLogoUrl()
        );

        // Decode the image using a smaller bitmap size.
        // This avoids loading an unnecessarily large original image.
        Bitmap bitmap = decodeSampledBitmap(
                context,
                drawableId,
                300,
                300
        );

        if (bitmap != null) {
            holder.ivCover.setImageBitmap(bitmap);
        } else {
            holder.ivCover.setImageResource(R.drawable.dish);
        }

        // Restaurant click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRestaurantClick(restaurant);
            }
        });
    }

    private Bitmap decodeSampledBitmap(
            Context context,
            int resourceId,
            int reqWidth,
            int reqHeight
    ) {

        if (resourceId == 0) {
            return null;
        }

        // Read only the dimensions first.
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeResource(
                context.getResources(),
                resourceId,
                options
        );

        int imageWidth = options.outWidth;
        int imageHeight = options.outHeight;

        if (imageWidth <= 0 || imageHeight <= 0) {
            return null;
        }

        // Calculate the sample size.
        options.inSampleSize = calculateInSampleSize(
                imageWidth,
                imageHeight,
                reqWidth,
                reqHeight
        );

        // Decode the actual bitmap.
        options.inJustDecodeBounds = false;

        // Use less memory for normal restaurant logo images.
        options.inPreferredConfig = Bitmap.Config.RGB_565;

        return BitmapFactory.decodeResource(
                context.getResources(),
                resourceId,
                options
        );
    }

    private int calculateInSampleSize(
            int imageWidth,
            int imageHeight,
            int reqWidth,
            int reqHeight
    ) {

        int inSampleSize = 1;

        if (imageHeight > reqHeight
                || imageWidth > reqWidth) {

            int halfHeight = imageHeight / 2;
            int halfWidth = imageWidth / 2;

            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {

                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    @Override
    public int getItemCount() {
        if (restaurantList != null) {
            return restaurantList.size();
        }

        return 0;
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
