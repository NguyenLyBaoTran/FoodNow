package com.example.foodnow.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodnow.R;
import com.example.foodnow.adapter.OrderAdapter;
import com.example.foodnow.model.Order;
import com.example.foodnow.network.ApiService;
import com.example.foodnow.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrdersFragment extends Fragment {

    private RecyclerView rvOrders;
    private TextView tvEmptyOrders;
    private OrderAdapter adapter;
    private final List<Order> orderList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_orders, container, false);

        rvOrders = view.findViewById(R.id.rvOrders);
        tvEmptyOrders = view.findViewById(R.id.tvEmptyOrders);

        adapter = new OrderAdapter(orderList, order -> {
            Intent intent = new Intent(requireContext(), com.example.foodnow.OrderDetailActivity.class);
            intent.putExtra("order", order);
            startActivity(intent);
        });
        rvOrders.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvOrders.setAdapter(adapter);

        fetchOrders();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchOrders();
    }

    private void fetchOrders() {
        ApiService apiService = RetrofitClient.getApiService(requireContext());
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
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateEmptyState() {
        boolean isEmpty = orderList.isEmpty();
        rvOrders.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        tvEmptyOrders.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }
}
