package com.example.foodnow;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.foodnow.model.Address;
import com.example.foodnow.model.User;
import com.example.foodnow.network.ApiService;
import com.example.foodnow.network.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {

    private EditText etFullName, etEmail;
    private TextView tvDefaultAddress;
    private Button btnSave;
    private ProgressBar progressBar;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        tvDefaultAddress = findViewById(R.id.tvDefaultAddress);
        btnSave = findViewById(R.id.btnSave);
        progressBar = findViewById(R.id.progressBar);

        currentUser = (User) getIntent().getSerializableExtra("user");
        if (currentUser != null) {
            etFullName.setText(currentUser.getFullName());
            etEmail.setText(currentUser.getEmail());
        }

        findViewById(R.id.btnManageAddress).setOnClickListener(v -> {
            startActivity(new Intent(this, AddressActivity.class));
        });

        btnSave.setOnClickListener(v -> saveChanges());
        
        fetchAddresses();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchAddresses();
    }

    private void fetchAddresses() {
        ApiService apiService = RetrofitClient.getApiService(this);
        apiService.getAddresses().enqueue(new Callback<List<Address>>() {
            @Override
            public void onResponse(Call<List<Address>> call, Response<List<Address>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Address> addresses = response.body();
                    for (Address addr : addresses) {
                        if (addr.isDefault()) {
                            tvDefaultAddress.setText(addr.getAddressLine());
                            return;
                        }
                    }
                    if (!addresses.isEmpty()) {
                        tvDefaultAddress.setText(addresses.get(0).getAddressLine());
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Address>> call, Throwable t) {}
        });
    }

    private void saveChanges() {
        String fullName = etFullName.getText().toString().trim();
        if (fullName.isEmpty()) {
            Toast.makeText(this, "Full name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSave.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        User updateData = new User(fullName, currentUser.getEmail());
        ApiService apiService = RetrofitClient.getApiService(this);
        apiService.updateProfile(updateData).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                progressBar.setVisibility(View.GONE);
                btnSave.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(EditProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(EditProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnSave.setEnabled(true);
                Toast.makeText(EditProfileActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
