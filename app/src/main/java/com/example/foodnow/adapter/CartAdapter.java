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
import com.example.foodnow.model.CartItem;
import com.example.foodnow.utils.ImageUtils;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    public interface CartActionListener {
        void onIncrease(CartItem item);
        void onDecrease(CartItem item);
        void onDelete(CartItem item);
    }

    private final List<CartItem> cartItems;
    private final CartActionListener listener;

    public CartAdapter(List<CartItem> cartItems) {
        this(cartItems, null);
    }

    public CartAdapter(List<CartItem> cartItems, CartActionListener listener) {
        this.cartItems = cartItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_cart_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {
        CartItem item = cartItems.get(position);
        Context context = holder.itemView.getContext();

        if (item.getFood() != null) {

            // Food information
            holder.tvName.setText(item.getFood().getName());

            holder.tvPrice.setText(
                    String.format(
                            "%,d VND",
                            item.getFood().getPrice()
                    )
            );

            // Get the exact drawable from image_url
            int drawableId = ImageUtils.getDrawableId(
                    context,
                    item.getFood().getImageUrl()
            );

            // Decode image using a smaller bitmap size
            // to prevent large bitmap crashes.
            Bitmap bitmap = decodeSampledBitmap(
                    context,
                    drawableId,
                    300,
                    300
            );

            if (bitmap != null) {
                holder.ivFood.setImageBitmap(bitmap);
            } else {
                holder.ivFood.setImageResource(R.drawable.dish);
            }

        } else {

            holder.tvName.setText("Unknown item");
            holder.tvPrice.setText("0 VND");
            holder.ivFood.setImageResource(R.drawable.dish);
        }

        // Quantity
        holder.tvQuantity.setText(
                String.valueOf(item.getQuantity())
        );

        // Decrease
        holder.btnDecrease.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDecrease(item);
            }
        });

        // Increase
        holder.btnIncrease.setOnClickListener(v -> {
            if (listener != null) {
                listener.onIncrease(item);
            }
        });

        // Delete
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDelete(item);
            }
        });
    }

    /**
     * Decode a drawable resource with a smaller size.
     * This prevents Android from loading an unnecessarily
     * large bitmap into memory.
     */
    private Bitmap decodeSampledBitmap(
            Context context,
            int resourceId,
            int reqWidth,
            int reqHeight
    ) {

        if (resourceId == 0) {
            return null;
        }

        // Read image dimensions first.
        // The actual bitmap is not loaded yet.
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

        // Calculate the appropriate sample size.
        options.inSampleSize = calculateInSampleSize(
                imageWidth,
                imageHeight,
                reqWidth,
                reqHeight
        );

        // Decode the bitmap using the calculated sample size.
        options.inJustDecodeBounds = false;

        // RGB_565 uses less memory than ARGB_8888.
        // This is enough for normal food images.
        options.inPreferredConfig = Bitmap.Config.RGB_565;

        return BitmapFactory.decodeResource(
                context.getResources(),
                resourceId,
                options
        );
    }

    /**
     * Calculate how much the original image should be reduced.
     */
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
        return cartItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView ivFood;
        TextView tvName, tvPrice, tvQuantity;
        View btnDecrease, btnIncrease, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            ivFood = itemView.findViewById(R.id.ivFood);
            tvName = itemView.findViewById(R.id.tvFoodName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);

            btnDecrease = itemView.findViewById(R.id.btnDecrease);
            btnIncrease = itemView.findViewById(R.id.btnIncrease);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
