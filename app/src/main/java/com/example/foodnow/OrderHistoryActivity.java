package com.example.foodnow;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodnow.adapter.OrderAdapter;
import com.example.foodnow.model.Order;
import com.example.foodnow.network.ApiService;
import com.example.foodnow.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderHistoryActivity extends AppCompatActivity {

    private RecyclerView rvOrders;
    private TextView tvEmptyOrders;
    private OrderAdapter adapter;
    private List<Order> orderList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        rvOrders = findViewById(R.id.rvOrders);
        tvEmptyOrders = findViewById(R.id.tvEmptyOrders);
        adapter = new OrderAdapter(orderList, order -> {
            Intent intent = new Intent(this, OrderDetailActivity.class);
            intent.putExtra("order", order);
            startActivity(intent);
        });
        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        rvOrders.setAdapter(adapter);

        fetchOrders();
    }

    private void fetchOrders() {
        ApiService apiService = RetrofitClient.getApiService(this);
        apiService.getOrders().enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    orderList.clear();
                    orderList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    updateEmptyState();
                } else {
                    orderList.clear();
                    adapter.notifyDataSetChanged();
                    updateEmptyState();
                }
            }

            @Override
            public void onFailure(Call<List<Order>> call, Throwable t) {
                orderList.clear();
                adapter.notifyDataSetChanged();
                updateEmptyState();
                Toast.makeText(OrderHistoryActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateEmptyState() {
        boolean isEmpty = orderList.isEmpty();
        rvOrders.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        tvEmptyOrders.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }
}