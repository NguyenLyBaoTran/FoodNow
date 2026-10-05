package com.example.foodnow;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.foodnow.model.Order;
import com.example.foodnow.network.ApiService;
import com.example.foodnow.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderTrackingActivity extends AppCompatActivity {

    private TextView tvOrderId, tvTotal, tvStatusTitle, tvStatusSubtitle;
    private ImageView imgDot1, imgDot2, imgDot3, imgDot4, imgDot5;
    private Button btnViewOrder, btnCancelOrder, btnBack;
    private int orderId;
    private Order order;

    private android.os.Handler handler = new android.os.Handler();
    private Runnable pollingRunnable = new Runnable() {
        @Override
        public void run() {
            fetchOrderDetails();
            handler.postDelayed(this, 10000); // Every 10 seconds
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_tracking);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back);
        }
        toolbar.setNavigationOnClickListener(v -> goBack());

        tvOrderId = findViewById(R.id.tvOrderId);
        tvTotal = findViewById(R.id.tvTotal);
        tvStatusTitle = findViewById(R.id.tvOrderPlacedTitle);
        tvStatusSubtitle = findViewById(R.id.tvOrderPlacedSubtitle);
        imgDot1 = findViewById(R.id.imgDot1);
        imgDot2 = findViewById(R.id.imgDot2);
        imgDot3 = findViewById(R.id.imgDot3);
        imgDot4 = findViewById(R.id.imgDot4);
        imgDot5 = findViewById(R.id.imgDot5);
        btnViewOrder = findViewById(R.id.btnViewOrder);
        btnCancelOrder = findViewById(R.id.btnCancelOrder);
        btnBack = findViewById(R.id.btnBack);

        orderId = getIntent().getIntExtra("order_id", -1);
        if (orderId == -1) {
            finish();
            return;
        }

        fetchOrderDetails();

        btnViewOrder.setOnClickListener(v -> {
            Intent intent = new Intent(this, OrderDetailActivity.class);
            intent.putExtra("order", order);
            startActivity(intent);
        });

        btnBack.setOnClickListener(v -> goBack());

        btnCancelOrder.setOnClickListener(v -> showCancelConfirmation());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                goBack();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        startPolling();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopPolling();
    }

    private void startPolling() {
        handler.post(pollingRunnable);
    }

    private void stopPolling() {
        handler.removeCallbacks(pollingRunnable);
    }

    private void goBack() {
        if (isTaskRoot()) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("open_orders", true);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
        finish();
    }

    private void fetchOrderDetails() {
        ApiService apiService = RetrofitClient.getApiService(this);
        apiService.getOrder(orderId).enqueue(new Callback<Order>() {
            @Override
            public void onResponse(Call<Order> call, Response<Order> response) {
                if (response.isSuccessful() && response.body() != null) {
                    order = response.body();
                    updateUI();
                }
            }

            @Override
            public void onFailure(Call<Order> call, Throwable t) {
                if (!isFinishing()) {
                    Toast.makeText(OrderTrackingActivity.this, "Error fetching order", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateUI() {
        if (order == null) return;

        tvOrderId.setText("Order #" + order.getId());
        tvTotal.setText(String.format("Total: %,.0f VND", order.getTotalAmount()));

        String status = order.getStatus();
        resetDots();

        imgDot1.setAlpha(1.0f);

        if (status.equals("PENDING_PAYMENT")) {
            tvStatusTitle.setText("Waiting for Payment");
            tvStatusSubtitle.setText("Please complete your payment to proceed.");
            btnCancelOrder.setVisibility(View.VISIBLE);
        } else if (status.equals("WAITING_FOR_DRIVER")) {
            tvStatusTitle.setText("Waiting for Driver");
            tvStatusSubtitle.setText("We're finding a driver for your order.");
            imgDot2.setAlpha(1.0f);
            imgDot3.setAlpha(1.0f);
            btnCancelOrder.setVisibility(View.VISIBLE);
        } else if (status.equals("DRIVER_ASSIGNED")) {
            tvStatusTitle.setText("Driver Assigned");
            tvStatusSubtitle.setText("A driver has been assigned to your order.");
            imgDot2.setAlpha(1.0f);
            imgDot3.setAlpha(1.0f);
            imgDot4.setAlpha(1.0f);
            btnCancelOrder.setVisibility(View.GONE);
        } else if (status.equals("DELIVERING") || status.equals("PICKED_UP")) {
            tvStatusTitle.setText("On the Way");
            tvStatusSubtitle.setText("The driver is on the way to you.");
            imgDot2.setAlpha(1.0f);
            imgDot3.setAlpha(1.0f);
            imgDot4.setAlpha(1.0f);
            btnCancelOrder.setVisibility(View.GONE);
        } else if (status.equals("DELIVERED") || status.equals("COMPLETED")) {
            tvStatusTitle.setText("Order Completed");
            tvStatusSubtitle.setText("Your food has been delivered. Enjoy!");
            imgDot2.setAlpha(1.0f);
            imgDot3.setAlpha(1.0f);
            imgDot4.setAlpha(1.0f);
            imgDot5.setAlpha(1.0f);
            btnCancelOrder.setVisibility(View.GONE);
        } else if (status.equals("CANCELLED")) {
            tvStatusTitle.setText("Order Cancelled");
            tvStatusSubtitle.setText("This order has been cancelled.");
            btnCancelOrder.setVisibility(View.GONE);
        }
    }

    private void resetDots() {
        imgDot2.setAlpha(0.3f);
        imgDot3.setAlpha(0.3f);
        imgDot4.setAlpha(0.3f);
        imgDot5.setAlpha(0.3f);
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
        apiService.cancelOrder(orderId).enqueue(new Callback<Order>() {
            @Override
            public void onResponse(Call<Order> call, Response<Order> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(OrderTrackingActivity.this, "Order cancelled successfully", Toast.LENGTH_SHORT).show();
                    order = response.body();
                    updateUI();
                } else {
                    Toast.makeText(OrderTrackingActivity.this, "Cannot cancel order at this time", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Order> call, Throwable t) {
                Toast.makeText(OrderTrackingActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
