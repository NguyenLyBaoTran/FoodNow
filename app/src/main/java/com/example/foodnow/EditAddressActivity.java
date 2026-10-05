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

public class EditAddressActivity extends AppCompatActivity {

    private EditText edtRecipientName, edtPhone, edtAddressLine;
    private Button btnSave;
    private int addressId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_address);

        edtRecipientName = findViewById(R.id.edtRecipientName);
        edtPhone = findViewById(R.id.edtPhone);
        edtAddressLine = findViewById(R.id.edtAddressLine);
        btnSave = findViewById(R.id.btnSave);

        addressId = getIntent().getIntExtra("address_id", -1);
        String recipientName = getIntent().getStringExtra("recipient_name");
        String phone = getIntent().getStringExtra("phone");
        String addressLine = getIntent().getStringExtra("address_line");

        if (addressId == -1 || recipientName == null || phone == null || addressLine == null) {
            Toast.makeText(this, "Invalid address data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        edtRecipientName.setText(recipientName);
        edtPhone.setText(phone);
        edtAddressLine.setText(addressLine);

        ((android.widget.TextView) findViewById(R.id.titleText)).setText("Edit Address");
        btnSave.setText("Save");

        btnSave.setOnClickListener(v -> saveAddress());
    }

    private void saveAddress() {
        String recipientName = edtRecipientName.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String addressLine = edtAddressLine.getText().toString().trim();

        if (recipientName.isEmpty() || phone.isEmpty() || addressLine.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Address address = new Address(recipientName, phone, addressLine);
        ApiService apiService = RetrofitClient.getApiService(this);
        apiService.updateAddress(addressId, address).enqueue(new Callback<Address>() {
            @Override
            public void onResponse(Call<Address> call, Response<Address> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(EditAddressActivity.this, "Address updated", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(EditAddressActivity.this, "Failed to update address", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Address> call, Throwable t) {
                Toast.makeText(EditAddressActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
