package com.example.foodnow.driver.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodnow.R;
import com.example.foodnow.driver.DriverOrderDetailActivity;
import com.example.foodnow.driver.adapter.DriverOrderAdapter;
import com.example.foodnow.model.Order;
import com.example.foodnow.network.ApiService;
import com.example.foodnow.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DriverOrdersListFragment extends Fragment {

    private String type; // AVAILABLE, ACTIVE, HISTORY
    private RecyclerView rvOrders;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private DriverOrderAdapter adapter;
    private List<Order> orderList = new ArrayList<>();

    public DriverOrdersListFragment() {
        // Required empty public constructor
    }

    public DriverOrdersListFragment(String type) {
        this.type = type;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_driver_orders_list, container, false);
        
        if (savedInstanceState != null) {
            type = savedInstanceState.getString("type");
        }

        rvOrders = view.findViewById(R.id.rvOrders);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmpty = view.findViewById(R.id.tvEmpty);

        adapter = new DriverOrderAdapter(orderList, order -> {
            Intent intent = new Intent(requireContext(), DriverOrderDetailActivity.class);
            intent.putExtra("order", order);
            startActivity(intent);
        });
        rvOrders.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvOrders.setAdapter(adapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchOrders();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("type", type);
    }

    private void fetchOrders() {
        progressBar.setVisibility(View.VISIBLE);
        ApiService apiService = RetrofitClient.getApiService(requireContext());
        Call<List<Order>> call;

        if ("AVAILABLE".equals(type)) {
            call = apiService.getAvailableOrders();
        } else if ("ACTIVE".equals(type)) {
            call = apiService.getActiveOrders();
        } else {
            call = apiService.getDriverOrderHistory();
        }

        call.enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    orderList.clear();
                    orderList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    updateEmptyState();
                }
            }

            @Override
            public void onFailure(Call<List<Order>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateEmptyState() {
        boolean isEmpty = orderList.isEmpty();
        rvOrders.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        tvEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }
}
