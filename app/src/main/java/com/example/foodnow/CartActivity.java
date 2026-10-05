package com.example.foodnow;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodnow.adapter.CartAdapter;
import com.example.foodnow.model.Cart;
import com.example.foodnow.model.CartItem;
import com.example.foodnow.network.ApiService;
import com.example.foodnow.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartActivity extends AppCompatActivity implements CartAdapter.CartActionListener {
    private RecyclerView rvCartItems;
    private ProgressBar progressBar;
    private TextView tvTotal, tvSubtotal, tvDeliveryFee;
    private View tvEmptyLayout;
    private Button btnCheckout;
    private CartAdapter adapter;
    private List<CartItem> cartItems = new ArrayList<>();
    private ApiService apiService;
    private static final int DELIVERY_FEE = 15000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        initViews();
        apiService = RetrofitClient.getApiService(this);
        setupRecyclerView();
        fetchCart();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        btnCheckout.setOnClickListener(v -> {
            if (cartItems.isEmpty()) {
                Toast.makeText(this, "Cart is empty", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, CheckoutActivity.class);
            startActivity(intent);
        });
    }

    private void initViews() {
        rvCartItems = findViewById(R.id.rvCartItems);
        progressBar = findViewById(R.id.progressBar);
        tvTotal = findViewById(R.id.tvTotal);
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvDeliveryFee = findViewById(R.id.tvDeliveryFee);
        tvEmptyLayout = findViewById(R.id.tvEmptyLayout);
        btnCheckout = findViewById(R.id.btnCheckout);
        
        tvDeliveryFee.setText(String.format("%,d VND", DELIVERY_FEE));
    }

    private void setupRecyclerView() {
        adapter = new CartAdapter(cartItems, this);
        rvCartItems.setLayoutManager(new LinearLayoutManager(this));
        rvCartItems.setAdapter(adapter);
    }

    private void fetchCart() {
        progressBar.setVisibility(View.VISIBLE);
        apiService.getCart().enqueue(new Callback<Cart>() {
            @Override
            public void onResponse(Call<Cart> call, Response<Cart> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    cartItems.clear();
                    if (response.body().getItems() != null) {
                        cartItems.addAll(response.body().getItems());
                    }
                    adapter.notifyDataSetChanged();
                    updateUIState();
                    calculateTotal();
                } else {
                    cartItems.clear();
                    updateUIState();
                    Toast.makeText(CartActivity.this, "Failed to load cart", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Cart> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                cartItems.clear();
                updateUIState();
                Toast.makeText(CartActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUIState() {
        boolean isEmpty = cartItems.isEmpty();
        rvCartItems.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        tvEmptyLayout.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        btnCheckout.setEnabled(!isEmpty);
        btnCheckout.setAlpha(isEmpty ? 0.5f : 1.0f);
        
        findViewById(R.id.layoutBottom).setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onIncrease(CartItem item) {
        updateQuantity(item, item.getQuantity() + 1);
    }

    @Override
    public void onDecrease(CartItem item) {
        if (item.getQuantity() <= 1) {
            onDelete(item);
            return;
        }
        updateQuantity(item, item.getQuantity() - 1);
    }

    @Override
    public void onDelete(CartItem item) {
        if (item == null) return;
        progressBar.setVisibility(View.VISIBLE);
        apiService.deleteCartItem(item.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    fetchCart();
                } else {
                    Toast.makeText(CartActivity.this, "Unable to delete item", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(CartActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateQuantity(CartItem item, int newQuantity) {
        if (item == null) return;
        
        progressBar.setVisibility(View.VISIBLE);
        apiService.updateCartItem(item.getId(), new com.example.foodnow.model.CartItemRequest(item.getFoodId(), newQuantity))
                .enqueue(new Callback<CartItem>() {
                    @Override
                    public void onResponse(Call<CartItem> call, Response<CartItem> response) {
                        progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful()) {
                            fetchCart();
                        } else {
                            Toast.makeText(CartActivity.this, "Unable to update quantity", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<CartItem> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(CartActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void calculateTotal() {
        int subtotal = 0;
        for (CartItem item : cartItems) {
            if (item.getFood() != null) {
                subtotal += item.getFood().getPrice() * item.getQuantity();
            }
        }
        int total = subtotal + DELIVERY_FEE;
        
        tvSubtotal.setText(String.format("%,d VND", subtotal));
        tvTotal.setText(String.format("%,d VND", total));
    }
}
