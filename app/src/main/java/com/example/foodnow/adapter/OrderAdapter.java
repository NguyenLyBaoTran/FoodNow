package com.example.foodnow.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodnow.R;
import com.example.foodnow.model.Order;
import com.example.foodnow.model.OrderItem;

import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {

    public interface OnOrderClickListener {
        void onOrderClick(Order order);
    }

    private List<Order> orderList;
    private OnOrderClickListener listener;

    public OrderAdapter(List<Order> orderList, OnOrderClickListener listener) {
        this.orderList = orderList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = orderList.get(position);
        holder.tvOrderId.setText("Order #" + order.getId());
        
        String status = order.getStatus();
        holder.tvStatus.setText(formatStatus(status));
        
        // Apply color based on status
        if (status.equals("CANCELLED")) {
            holder.tvStatus.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.error));
            holder.tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0x22E74C3C));
        } else if (status.equals("DELIVERED") || status.equals("COMPLETED")) {
            holder.tvStatus.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.success));
            holder.tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0x2227AE60));
        } else {
            holder.tvStatus.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.colorPrimary));
            holder.tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0x2227AD60));
        }

        holder.tvDate.setText(order.getCreatedAt());
        holder.tvTotal.setText(String.format("Total: %,.0f VND", order.getTotalAmount()));

        // Display items summary
        StringBuilder summary = new StringBuilder();
        if (order.getItems() != null) {
            for (int i = 0; i < order.getItems().size(); i++) {
                OrderItem item = order.getItems().get(i);
                if (item.getFood() != null) {
                    summary.append(item.getFood().getName()).append(" x").append(item.getQuantity());
                    if (i < order.getItems().size() - 1) summary.append(", ");
                }
            }
        }
        holder.tvItemsSummary.setText(summary.toString());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onOrderClick(order);
            }
        });
    }

    private String formatStatus(String status) {
        if (status == null) return "Unknown";
        String formatted = status.replace("_", " ").toLowerCase();
        return formatted.substring(0, 1).toUpperCase() + formatted.substring(1);
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvStatus, tvDate, tvTotal, tvItemsSummary;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTotal = itemView.findViewById(R.id.tvTotal);
            tvItemsSummary = itemView.findViewById(R.id.tvItemsSummary);
        }
    }
}
