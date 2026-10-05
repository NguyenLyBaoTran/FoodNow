package com.example.foodnow.driver;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodnow.R;
import com.example.foodnow.adapter.OrderDetailAdapter;
import com.example.foodnow.model.Order;
import com.example.foodnow.model.StatusUpdate;
import com.example.foodnow.network.ApiService;
import com.example.foodnow.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DriverOrderDetailActivity extends AppCompatActivity {

    private TextView tvOrderId, tvStatus, tvRecipientName, tvPhone, tvAddress, tvTotal, tvPaymentMethod;
    private RecyclerView rvOrderItems;
    private Button btnAction;
    private ProgressBar progressBar;
    private Order order;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_order_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        initViews();

        order = (Order) getIntent().getSerializableExtra("order");
        if (order != null) {
            updateUI();
        } else {
            finish();
        }
    }

    private void initViews() {
        tvOrderId = findViewById(R.id.tvOrderId);
        tvStatus = findViewById(R.id.tvStatus);
        tvRecipientName = findViewById(R.id.tvRecipientName);
        tvPhone = findViewById(R.id.tvPhone);
        tvAddress = findViewById(R.id.tvAddress);
        tvTotal = findViewById(R.id.tvTotal);
        tvPaymentMethod = findViewById(R.id.tvPaymentMethod);
        rvOrderItems = findViewById(R.id.rvOrderItems);
        btnAction = findViewById(R.id.btnAction);
        progressBar = findViewById(R.id.progressBar);

        rvOrderItems.setLayoutManager(new LinearLayoutManager(this));
    }

    private void updateUI() {
        tvOrderId.setText("Order #" + order.getId());
        tvStatus.setText(order.getStatus());
        tvTotal.setText(String.format("%,.0f VND", order.getTotalAmount()));
        tvPaymentMethod.setText("Payment: " + order.getPaymentMethod());

        if (order.getAddress() != null) {
            tvRecipientName.setText(order.getAddress().getRecipientName());
            tvPhone.setText(order.getAddress().getPhone());
            tvAddress.setText(order.getAddress().getAddressLine());
        }

        if (order.getItems() != null) {
            rvOrderItems.setAdapter(new OrderDetailAdapter(order.getItems(), false, null));
        }

        setupActionButton();
    }

    private void setupActionButton() {
        String status = order.getStatus();
        btnAction.setVisibility(View.VISIBLE);
        
        if (order.getDriverId() == null && "WAITING_FOR_DRIVER".equals(status)) {
            btnAction.setText("Accept Order");
            btnAction.setOnClickListener(v -> acceptOrder());
        } else if ("DRIVER_ASSIGNED".equals(status)) {
            btnAction.setText("Start Preparing");
            btnAction.setOnClickListener(v -> updateStatus("PREPARING"));
        } else if ("PREPARING".equals(status)) {
            btnAction.setText("Ready for Pickup");
            btnAction.setOnClickListener(v -> updateStatus("READY_FOR_PICKUP"));
        } else if ("READY_FOR_PICKUP".equals(status)) {
            btnAction.setText("Pick Up Order");
            btnAction.setOnClickListener(v -> updateStatus("PICKED_UP"));
        } else if ("PICKED_UP".equals(status)) {
            btnAction.setText("Start Delivery");
            btnAction.setOnClickListener(v -> updateStatus("DELIVERING"));
        } else if ("DELIVERING".equals(status)) {
            btnAction.setText("Mark as Delivered");
            btnAction.setOnClickListener(v -> updateStatus("DELIVERED"));
        } else if ("DELIVERED".equals(status)) {
            btnAction.setText("Complete Order");
            btnAction.setOnClickListener(v -> updateStatus("COMPLETED"));
        } else {
            btnAction.setVisibility(View.GONE);
        }
    }

    private void acceptOrder() {
        showLoading(true);
        ApiService apiService = RetrofitClient.getApiService(this);
        apiService.acceptOrder(order.getId()).enqueue(new Callback<Order>() {
            @Override
            public void onResponse(Call<Order> call, Response<Order> response) {
                showLoading(false);
                if (response.isSuccessful()) {
                    order = response.body();
                    updateUI();
                    Toast.makeText(DriverOrderDetailActivity.this, "Order accepted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(DriverOrderDetailActivity.this, "Failed to accept order", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Order> call, Throwable t) {
                showLoading(false);
                Toast.makeText(DriverOrderDetailActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateStatus(String newStatus) {
        showLoading(true);
        ApiService apiService = RetrofitClient.getApiService(this);
        apiService.updateOrderStatus(order.getId(), new StatusUpdate(newStatus)).enqueue(new Callback<Order>() {
            @Override
            public void onResponse(Call<Order> call, Response<Order> response) {
                showLoading(false);
                if (response.isSuccessful()) {
                    order = response.body();
                    updateUI();
                    Toast.makeText(DriverOrderDetailActivity.this, "Status updated", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(DriverOrderDetailActivity.this, "Failed to update status", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Order> call, Throwable t) {
                showLoading(false);
                Toast.makeText(DriverOrderDetailActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnAction.setEnabled(!loading);
    }
}
