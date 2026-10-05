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
import com.example.foodnow.model.OrderItem;
import com.example.foodnow.utils.ImageUtils;

import java.util.List;

public class OrderDetailAdapter extends RecyclerView.Adapter<OrderDetailAdapter.ViewHolder> {

    public interface OrderItemActionListener {
        void onIncrease(OrderItem item);
        void onDecrease(OrderItem item);
        void onDelete(OrderItem item);
    }

    private final List<OrderItem> itemList;
    private final boolean isEditable;
    private final OrderItemActionListener listener;

    public OrderDetailAdapter(List<OrderItem> itemList) {
        this(itemList, false, null);
    }

    public OrderDetailAdapter(List<OrderItem> itemList, boolean isEditable, OrderItemActionListener listener) {
        this.itemList = itemList;
        this.isEditable = isEditable;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_order_detail_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderItem item = itemList.get(position);
        String foodName = item.getFood() != null ? item.getFood().getName() : "Food";
        double unitPrice = item.getPriceSnapshot();
        int quantity = item.getQuantity();
        double total = unitPrice * quantity;

        holder.tvItemName.setText(foodName);
        holder.tvItemMeta.setText(quantity + " x " + String.format("%,.0f VND", unitPrice));
        holder.tvItemTotal.setText(String.format("%,.0f VND", total));

        if (item.getFood() != null) {
            Glide.with(holder.itemView.getContext())
                    .load(ImageUtils.getDrawableId(holder.itemView.getContext(), item.getFood().getImageUrl()))
                    .placeholder(R.drawable.dish)
                    .into(holder.ivItemFood);
        } else {
            holder.ivItemFood.setImageResource(R.drawable.dish);
        }

        if (isEditable && listener != null) {
            holder.layoutEdit.setVisibility(View.VISIBLE);
            holder.tvQuantity.setText(String.valueOf(quantity));
            holder.btnDecrease.setOnClickListener(v -> listener.onDecrease(item));
            holder.btnIncrease.setOnClickListener(v -> listener.onIncrease(item));
            holder.btnDelete.setOnClickListener(v -> listener.onDelete(item));
        } else {
            holder.layoutEdit.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvItemName, tvItemMeta, tvItemTotal, tvQuantity;
        ImageView ivItemFood;
        View layoutEdit, btnDecrease, btnIncrease, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvItemMeta = itemView.findViewById(R.id.tvItemMeta);
            tvItemTotal = itemView.findViewById(R.id.tvItemTotal);
            ivItemFood = itemView.findViewById(R.id.ivItemFood);
            layoutEdit = itemView.findViewById(R.id.layoutEdit);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            btnDecrease = itemView.findViewById(R.id.btnDecrease);
            btnIncrease = itemView.findViewById(R.id.btnIncrease);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
