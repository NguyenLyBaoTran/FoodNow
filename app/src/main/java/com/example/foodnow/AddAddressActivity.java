package com.example.foodnow;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.foodnow.model.Address;
import com.example.foodnow.network.ApiService;
import com.example.foodnow.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddAddressActivity extends AppCompatActivity {

    private EditText edtRecipientName, edtPhone, edtAddressLine;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_address);

        edtRecipientName = findViewById(R.id.edtRecipientName);
        edtPhone = findViewById(R.id.edtPhone);
        edtAddressLine = findViewById(R.id.edtAddressLine);
        btnSave = findViewById(R.id.btnSave);

        btnSave.setOnClickListener(v -> saveAddress());
    }

    private void saveAddress() {
        String name = edtRecipientName.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String addressLine = edtAddressLine.getText().toString().trim();

        if (name.isEmpty() || phone.isEmpty() || addressLine.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Address address = new Address(name, phone, addressLine);
        ApiService apiService = RetrofitClient.getApiService(this);
        apiService.addAddress(address).enqueue(new Callback<Address>() {
            @Override
            public void onResponse(Call<Address> call, Response<Address> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AddAddressActivity.this, "Address saved", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AddAddressActivity.this, "Failed to save address", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Address> call, Throwable t) {
                Toast.makeText(AddAddressActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}