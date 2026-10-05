package com.example.foodnow;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodnow.adapter.OrderDetailAdapter;
import com.example.foodnow.model.Address;
import com.example.foodnow.model.Order;
import com.example.foodnow.model.OrderItem;
import com.example.foodnow.model.Restaurant;
import com.example.foodnow.network.ApiService;
import com.example.foodnow.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderDetailActivity extends AppCompatActivity implements OrderDetailAdapter.OrderItemActionListener {

    private TextView tvOrderId, tvOrderStatus, tvOrderDate, tvOrderTotal;
    private TextView tvOrderRoute, tvPaymentMethod, tvPaymentStatus;
    private RecyclerView rvOrderItems;
    private Button btnViewTracking, btnCancelOrder;
    private Order order;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        initViews();

        order = (Order) getIntent().getSerializableExtra("order");
        if (order == null) {
            finish();
            return;
        }

        updateUI();

        btnViewTracking.setOnClickListener(v -> {
            Intent intent = new Intent(this, OrderTrackingActivity.class);
            intent.putExtra("order_id", order.getId());
            startActivity(intent);
        });

        btnCancelOrder.setOnClickListener(v -> showCancelConfirmation());
    }

    private void initViews() {
        tvOrderId = findViewById(R.id.tvOrderId);
        tvOrderStatus = findViewById(R.id.tvOrderStatus);
        tvOrderDate = findViewById(R.id.tvOrderDate);
        tvOrderTotal = findViewById(R.id.tvOrderTotal);
        rvOrderItems = findViewById(R.id.rvOrderItems);
        tvOrderRoute = findViewById(R.id.tvOrderRoute);
        tvPaymentMethod = findViewById(R.id.tvPaymentMethod);
        tvPaymentStatus = findViewById(R.id.tvPaymentStatus);
        btnViewTracking = findViewById(R.id.btnViewTracking);
        btnCancelOrder = findViewById(R.id.btnCancelOrder);

        rvOrderItems.setLayoutManager(new LinearLayoutManager(this));
    }

    private void updateUI() {
        tvOrderId.setText("Order #" + order.getId());
        tvOrderStatus.setText(formatStatus(order.getStatus()));
        tvOrderDate.setText(order.getCreatedAt());
        tvOrderTotal.setText(String.format("Total: %,.0f VND", order.getTotalAmount()));
        
        tvPaymentMethod.setText(order.getPaymentMethod());
        if (order.getPayment() != null) {
            tvPaymentStatus.setText(order.getPayment().getStatus());
        } else {
            tvPaymentStatus.setText("Pending");
        }

        String status = order.getStatus();
        boolean isEditable = status.equals("PENDING_PAYMENT") || status.equals("WAITING_FOR_DRIVER");
        
        List<OrderItem> items = order.getItems() != null ? order.getItems() : new ArrayList<>();
        rvOrderItems.setAdapter(new OrderDetailAdapter(items, isEditable, this));

        loadOrderRoute(order);

        // Show/Hide Cancel button
        if (isEditable) {
            btnCancelOrder.setVisibility(View.VISIBLE);
        } else {
            btnCancelOrder.setVisibility(View.GONE);
        }
    }

    @Override
    public void onIncrease(OrderItem item) {
        updateItemQuantity(item, item.getQuantity() + 1);
    }

    @Override
    public void onDecrease(OrderItem item) {
        if (item.getQuantity() <= 1) {
            onDelete(item);
            return;
        }
        updateItemQuantity(item, item.getQuantity() - 1);
    }

    @Override
    public void onDelete(OrderItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Remove Item?")
                .setMessage("Are you sure you want to remove this item from the order?")
                .setPositiveButton("Remove", (dialog, which) -> {
                    ApiService apiService = RetrofitClient.getApiService(this);
                    apiService.deleteOrderItem(order.getId(), item.getId()).enqueue(new Callback<Order>() {
                        @Override
                        public void onResponse(Call<Order> call, Response<Order> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                order = response.body();
                                updateUI();
                            } else {
                                Toast.makeText(OrderDetailActivity.this, "Cannot remove item", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Order> call, Throwable t) {
                            Toast.makeText(OrderDetailActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateItemQuantity(OrderItem item, int newQuantity) {
        ApiService apiService = RetrofitClient.getApiService(this);
        apiService.updateOrderItem(order.getId(), item.getId(), new com.example.foodnow.model.OrderItemUpdate(newQuantity))
                .enqueue(new Callback<Order>() {
                    @Override
                    public void onResponse(Call<Order> call, Response<Order> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            order = response.body();
                            updateUI();
                        } else {
                            Toast.makeText(OrderDetailActivity.this, "Update failed", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Order> call, Throwable t) {
                        Toast.makeText(OrderDetailActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showCancelConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Cancel Order?")
                .setMessage("Are you sure you want to cancel this order?")
                .setPositiveButton("Yes, Cancel", (dialog, which) -> performCancellation())
                .setNegativeButton("No, Keep it", null)
                .show();
    }

    private void performCancellation() {
        ApiService apiService = RetrofitClient.getApiService(this);
        apiService.cancelOrder(order.getId()).enqueue(new Callback<Order>() {
            @Override
            public void onResponse(Call<Order> call, Response<Order> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(OrderDetailActivity.this, "Order cancelled", Toast.LENGTH_SHORT).show();
                    order = response.body();
                    updateUI();
                } else {
                    Toast.makeText(OrderDetailActivity.this, "Cannot cancel order", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Order> call, Throwable t) {
                Toast.makeText(OrderDetailActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadOrderRoute(Order order) {
        if (order.getItems() != null && !order.getItems().isEmpty()
                && order.getItems().get(0).getFood() != null) {
            loadRestaurantAddress(order.getItems().get(0).getFood().getRestaurantId(), order);
        } else {
            loadDeliveryAddress(order, "Restaurant address loading...");
        }
    }

    private void loadRestaurantAddress(int restaurantId, Order order) {
        ApiService apiService = RetrofitClient.getApiService(this);
        apiService.getRestaurantDetail(restaurantId).enqueue(new Callback<Restaurant>() {
            @Override
            public void onResponse(Call<Restaurant> call, Response<Restaurant> response) {
                String address = response.isSuccessful() && response.body() != null
                        ? response.body().getName() + "\n" + response.body().getAddress()
                        : "Restaurant address unavailable";
                loadDeliveryAddress(order, address);
            }

            @Override
            public void onFailure(Call<Restaurant> call, Throwable t) {
                loadDeliveryAddress(order, "Restaurant address unavailable");
            }
        });
    }

    private void loadDeliveryAddress(Order order, String restaurantAddress) {
        ApiService apiService = RetrofitClient.getApiService(this);
        apiService.getAddresses().enqueue(new Callback<List<Address>>() {
            @Override
            public void onResponse(Call<List<Address>> call, Response<List<Address>> response) {
                String deliveryAddress = "Delivery address unavailable";
                if (response.isSuccessful() && response.body() != null) {
                    for (Address address : response.body()) {
                        if (address.getId() == order.getAddressId()) {
                            deliveryAddress = address.getRecipientName() + " | " + address.getPhone() + "\n" + address.getAddressLine();
                            break;
                        }
                    }
                }
                setOrderRoute(restaurantAddress, deliveryAddress);
            }

            @Override
            public void onFailure(Call<List<Address>> call, Throwable t) {
                setOrderRoute(restaurantAddress, "Delivery address unavailable");
            }
        });
    }

    private void setOrderRoute(String restaurantAddress, String deliveryAddress) {
        tvOrderRoute.setText("From:\n" + restaurantAddress + "\n\nTo:\n" + deliveryAddress);
    }

    private String formatStatus(String status) {
        if (status == null) return "Unknown";
        String formatted = status.replace("_", " ").toLowerCase();
        return formatted.substring(0, 1).toUpperCase() + formatted.substring(1);
    }
}
