package com.example.foodnow;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodnow.adapter.CartAdapter;
import com.example.foodnow.model.Address;
import com.example.foodnow.model.Cart;
import com.example.foodnow.model.CartItem;
import com.example.foodnow.model.Order;
import com.example.foodnow.model.OrderRequest;
import com.example.foodnow.network.ApiService;
import com.example.foodnow.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckoutActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_SELECT_ADDRESS = 1001;

    private CardView cardAddress;
    private TextView tvAddressDetails, tvTotal;
    private RecyclerView rvSummaryItems;
    private Button btnPlaceOrder;
    private RadioButton rbCOD, rbMoMo, rbVNPay;

    private Address selectedAddress;
    private List<CartItem> cartItems = new ArrayList<>();
    private int totalAmount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        initViews();
        fetchCart();

        cardAddress.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddressActivity.class);
            intent.putExtra("selection_mode", true);
            startActivityForResult(intent, REQUEST_CODE_SELECT_ADDRESS);
        });

        btnPlaceOrder.setOnClickListener(v -> placeOrder());
        
        setupPaymentSelection();
    }

    private void initViews() {
        cardAddress = findViewById(R.id.cardAddress);
        tvAddressDetails = findViewById(R.id.tvAddressDetails);
        tvTotal = findViewById(R.id.tvTotal);
        rvSummaryItems = findViewById(R.id.rvSummaryItems);
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);
        
        rbCOD = findViewById(R.id.rbCOD);
        rbMoMo = findViewById(R.id.rbMoMo);
        rbVNPay = findViewById(R.id.rbVNPay);

        rvSummaryItems.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupPaymentSelection() {
        // Manual exclusivity logic for RadioButtons inside different layouts
        View.OnClickListener listener = v -> {
            rbCOD.setChecked(v == rbCOD || v.getId() == R.id.rbCOD);
            rbMoMo.setChecked(v == rbMoMo || v.getId() == R.id.rbMoMo);
            rbVNPay.setChecked(v == rbVNPay || v.getId() == R.id.rbVNPay);
        };

        rbCOD.setOnClickListener(listener);
        rbMoMo.setOnClickListener(listener);
        rbVNPay.setOnClickListener(listener);
        
        // Also allow clicking the parent card-like structures if needed, 
        // but here the RadioButtons fill the Cards, so this is enough.
    }

    private void fetchCart() {
        ApiService apiService = RetrofitClient.getApiService(this);
        apiService.getCart().enqueue(new Callback<Cart>() {
            @Override
            public void onResponse(Call<Cart> call, Response<Cart> response) {
                if (response.isSuccessful() && response.body() != null) {
                    cartItems.clear();
                    cartItems.addAll(response.body().getItems());
                    updateUI();
                }
            }

            @Override
            public void onFailure(Call<Cart> call, Throwable t) {
                Toast.makeText(CheckoutActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI() {
        CartAdapter adapter = new CartAdapter(cartItems);
        rvSummaryItems.setAdapter(adapter);

        totalAmount = 0;
        for (CartItem item : cartItems) {
            totalAmount += item.getFood().getPrice() * item.getQuantity();
        }
        tvTotal.setText(String.format("%,d VND", totalAmount));
    }

    private void placeOrder() {
        if (selectedAddress == null) {
            Toast.makeText(this, "Please select a delivery address", Toast.LENGTH_SHORT).show();
            return;
        }

        String paymentMethod = "COD";
        if (rbMoMo.isChecked()) {
            paymentMethod = "MOMO";
        } else if (rbVNPay.isChecked()) {
            paymentMethod = "VNPAY";
        }

        btnPlaceOrder.setEnabled(false);
        btnPlaceOrder.setAlpha(0.5f);
        btnPlaceOrder.setText("Processing...");

        ApiService apiService = RetrofitClient.getApiService(this);
        OrderRequest request = new OrderRequest(selectedAddress.getId(), paymentMethod);

        apiService.createOrder(request).enqueue(new Callback<Order>() {
            @Override
            public void onResponse(Call<Order> call, Response<Order> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Order order = response.body();
                    Toast.makeText(CheckoutActivity.this, "Order created successfully!", Toast.LENGTH_SHORT).show();
                    
                    if (order.getPaymentMethod().equals("COD")) {
                        Intent intent = new Intent(CheckoutActivity.this, OrderTrackingActivity.class);
                        intent.putExtra("order_id", order.getId());
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(CheckoutActivity.this, PaymentActivity.class);
                        intent.putExtra("order", order);
                        intent.putExtra("method", order.getPaymentMethod());
                        startActivity(intent);
                    }
                } else {
                    btnPlaceOrder.setEnabled(true);
                    btnPlaceOrder.setAlpha(1.0f);
                    btnPlaceOrder.setText("Place Order");
                    Toast.makeText(CheckoutActivity.this, "Failed to place order", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Order> call, Throwable t) {
                btnPlaceOrder.setEnabled(true);
                btnPlaceOrder.setAlpha(1.0f);
                btnPlaceOrder.setText("Place Order");
                Toast.makeText(CheckoutActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_ADDRESS && resultCode == RESULT_OK && data != null) {
            selectedAddress = (Address) data.getSerializableExtra("selected_address");
            if (selectedAddress != null) {
                tvAddressDetails.setText(selectedAddress.getRecipientName() + "\n" +
                        selectedAddress.getPhone() + "\n" +
                        selectedAddress.getAddressLine());
            }
        }
    }
}
