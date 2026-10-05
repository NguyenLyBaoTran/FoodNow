package com.example.foodnow.driver.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodnow.R;
import com.example.foodnow.model.Order;

import java.util.List;

public class DriverOrderAdapter extends RecyclerView.Adapter<DriverOrderAdapter.ViewHolder> {

    public interface OnOrderClickListener {
        void onOrderClick(Order order);
    }

    private List<Order> orderList;
    private OnOrderClickListener listener;

    public DriverOrderAdapter(List<Order> orderList, OnOrderClickListener listener) {
        this.orderList = orderList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_driver_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = orderList.get(position);
        holder.tvOrderId.setText("Order #" + order.getId());
        holder.tvStatus.setText(order.getStatus());
        holder.tvTotal.setText(String.format("Total: %,.0f VND", order.getTotalAmount()));
        holder.tvPaymentMethod.setText(order.getPaymentMethod());

        if (order.getItems() != null && !order.getItems().isEmpty() && order.getItems().get(0).getFood() != null) {
             holder.tvRestaurantName.setText("Food order");
        }
        
        if (order.getAddress() != null) {
            holder.tvAddress.setText(order.getAddress().getAddressLine());
        } else {
            holder.tvAddress.setText("Address ID: " + order.getAddressId());
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onOrderClick(order);
            }
        });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvStatus, tvRestaurantName, tvAddress, tvTotal, tvPaymentMethod;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvRestaurantName = itemView.findViewById(R.id.tvRestaurantName);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvTotal = itemView.findViewById(R.id.tvTotal);
            tvPaymentMethod = itemView.findViewById(R.id.tvPaymentMethod);
        }
    }
}
