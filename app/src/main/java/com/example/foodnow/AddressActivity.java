package com.example.foodnow;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodnow.adapter.AddressAdapter;
import com.example.foodnow.model.Address;
import com.example.foodnow.network.ApiService;
import com.example.foodnow.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddressActivity extends AppCompatActivity implements AddressAdapter.OnAddressClickListener {

    private RecyclerView rvAddresses;
    private Button btnAddAddress;
    private TextView tvEmptyAddresses;
    private AddressAdapter adapter;
    private List<Address> addressList = new ArrayList<>();
    private boolean selectionMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address);

        selectionMode = getIntent().getBooleanExtra("selection_mode", false);

        rvAddresses = findViewById(R.id.rvAddresses);
        btnAddAddress = findViewById(R.id.btnAddAddress);
        tvEmptyAddresses = findViewById(R.id.tvEmptyAddresses);

        adapter = new AddressAdapter(addressList, this);
        rvAddresses.setLayoutManager(new LinearLayoutManager(this));
        rvAddresses.setAdapter(adapter);

        btnAddAddress.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddAddressActivity.class);
            startActivity(intent);
        });
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
                    addressList.clear();
                    addressList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    updateEmptyState();
                } else {
                    addressList.clear();
                    adapter.notifyDataSetChanged();
                    updateEmptyState();
                }
            }

            @Override
            public void onFailure(Call<List<Address>> call, Throwable t) {
                addressList.clear();
                adapter.notifyDataSetChanged();
                updateEmptyState();
                Toast.makeText(AddressActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateEmptyState() {
        boolean isEmpty = addressList.isEmpty();
        rvAddresses.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        tvEmptyAddresses.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onAddressClick(Address address) {
        if (selectionMode) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("selected_address", address);
            setResult(RESULT_OK, resultIntent);
            finish();
        }
    }

    @Override
    public void onEdit(Address address) {
        Intent intent = new Intent(this, EditAddressActivity.class);
        intent.putExtra("address_id", address.getId());
        intent.putExtra("recipient_name", address.getRecipientName());
        intent.putExtra("phone", address.getPhone());
        intent.putExtra("address_line", address.getAddressLine());
        startActivity(intent);
    }

    @Override
    public void onDelete(Address address) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Address?")
                .setMessage("Are you sure you want to delete this address?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> deleteAddress(address))
                .show();
    }

    private void deleteAddress(Address address) {
        ApiService apiService = RetrofitClient.getApiService(this);
        apiService.deleteAddress(address.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    fetchAddresses();
                    Toast.makeText(AddressActivity.this, "Address deleted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AddressActivity.this, "Unable to delete address", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(AddressActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}