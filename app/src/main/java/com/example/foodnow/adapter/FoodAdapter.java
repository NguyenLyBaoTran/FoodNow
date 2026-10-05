package com.example.foodnow.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodnow.R;
import com.example.foodnow.model.Food;
import com.example.foodnow.utils.ImageUtils;

import java.util.List;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.ViewHolder> {

    public interface OnFoodItemClickListener {
        void onFoodItemClick(Food food);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice;
        ImageView ivImageFood;

        public ViewHolder(View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tvName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            ivImageFood = itemView.findViewById(R.id.ivImageFood);
        }
    }

    private List<Food> mFoods;
    private OnFoodItemClickListener mListener;

    public FoodAdapter(List<Food> foods, OnFoodItemClickListener listener) {
        mFoods = foods;
        mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();

        View view = LayoutInflater.from(context)
                .inflate(R.layout.row_food, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {

        final Food food = mFoods.get(position);
        Context context = viewHolder.itemView.getContext();

        // Set food information
        viewHolder.tvName.setText(food.getName());
        viewHolder.tvPrice.setText(
                String.format("%,d VND", food.getPrice())
        );

        // Get drawable ID from image key
        int drawableId = ImageUtils.getDrawableId(
                context,
                food.getImageUrl()
        );

        // Load the correct drawable directly.
        // Bitmap is decoded with a smaller sample size
        // to avoid crashing when the original image is very large.
        Bitmap bitmap = decodeSampledBitmap(
                context,
                drawableId,
                300,
                300
        );

        if (bitmap != null) {
            viewHolder.ivImageFood.setImageBitmap(bitmap);
        } else {
            viewHolder.ivImageFood.setImageResource(R.drawable.dish);
        }

        // Food item click
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onFoodItemClick(food);
                }
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

        // First decode only the image dimensions.
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

        // Calculate a suitable sample size.
        options.inSampleSize = calculateInSampleSize(
                imageWidth,
                imageHeight,
                reqWidth,
                reqHeight
        );

        // Decode the actual bitmap using the smaller size.
        options.inJustDecodeBounds = false;
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

        if (imageHeight > reqHeight || imageWidth > reqWidth) {

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
        if (mFoods != null) {
            return mFoods.size();
        }

        return 0;
    }
}
