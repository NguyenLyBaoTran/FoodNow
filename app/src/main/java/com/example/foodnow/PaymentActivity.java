package com.example.foodnow;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.foodnow.model.Order;
import com.example.foodnow.model.Payment;
import com.example.foodnow.model.PaymentConfirmRequest;
import com.example.foodnow.network.ApiService;
import com.example.foodnow.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentActivity extends AppCompatActivity {

    private TextView tvMethod, tvAmount, tvOrderId;
    private Button btnCompleted;
    private Order order;
    private String method;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        order = (Order) getIntent().getSerializableExtra("order");
        method = getIntent().getStringExtra("method");

        if (order == null) {
            finish();
            return;
        }

        tvMethod = findViewById(R.id.tvMethod);
        tvAmount = findViewById(R.id.tvAmount);
        tvOrderId = findViewById(R.id.tvOrderId);
        btnCompleted = findViewById(R.id.btnCompleted);

        tvMethod.setText(method);
        tvAmount.setText(String.format("%,.0f VND", order.getTotalAmount()));
        tvOrderId.setText("Order #" + order.getId());

        btnCompleted.setOnClickListener(v -> confirmPayment());
        findViewById(R.id.btnCancel).setOnClickListener(v -> finish());
    }

    private void confirmPayment() {
        if (order.getPayment() == null) {
            Toast.makeText(this, "Payment info missing", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = RetrofitClient.getApiService(this);
        PaymentConfirmRequest request = new PaymentConfirmRequest(null);

        apiService.confirmPayment(order.getPayment().getId(), request).enqueue(new Callback<Payment>() {
            @Override
            public void onResponse(Call<Payment> call, Response<Payment> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(PaymentActivity.this, "Payment Confirmed", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(PaymentActivity.this, OrderTrackingActivity.class);
                    intent.putExtra("order_id", order.getId());
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                } else {
                    Toast.makeText(PaymentActivity.this, "Confirmation Failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Payment> call, Throwable t) {
                Toast.makeText(PaymentActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
