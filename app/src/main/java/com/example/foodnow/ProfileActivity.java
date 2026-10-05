package com.example.foodnow;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.foodnow.network.TokenManager;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        TextView tvUserName = findViewById(R.id.tvUserName);
        tvUserName.setText("Customer");

        findViewById(R.id.btnMyOrders).setOnClickListener(v -> {
            startActivity(new Intent(this, OrderHistoryActivity.class));
        });

        findViewById(R.id.btnMyAddresses).setOnClickListener(v -> {
            startActivity(new Intent(this, AddressActivity.class));
        });

        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            new TokenManager(this).clearToken();
            Intent intent = new Intent(this, SigninActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}