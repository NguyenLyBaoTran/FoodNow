package com.example.foodnow.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.foodnow.AddressActivity;
import com.example.foodnow.EditProfileActivity;
import com.example.foodnow.OrderHistoryActivity;
import com.example.foodnow.R;
import com.example.foodnow.SigninActivity;
import com.example.foodnow.model.User;
import com.example.foodnow.network.ApiService;
import com.example.foodnow.network.RetrofitClient;
import com.example.foodnow.network.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private static final int REQUEST_CODE_EDIT_PROFILE = 2001;
    private TextView tvUserName, tvUserEmail;
    private User currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        tvUserName = view.findViewById(R.id.tvUserName);
        tvUserEmail = view.findViewById(R.id.tvUserEmail);

        view.findViewById(R.id.btnEditProfile).setOnClickListener(v -> {
            if (currentUser != null) {
                Intent intent = new Intent(requireContext(), EditProfileActivity.class);
                intent.putExtra("user", currentUser);
                startActivityForResult(intent, REQUEST_CODE_EDIT_PROFILE);
            }
        });

        view.findViewById(R.id.btnMyOrders).setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), OrderHistoryActivity.class));
        });

        view.findViewById(R.id.btnMyAddresses).setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), AddressActivity.class));
        });

        view.findViewById(R.id.btnLogout).setOnClickListener(v -> {
            new TokenManager(requireContext()).clearToken();
            Intent intent = new Intent(requireContext(), SigninActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        fetchUserProfile();

        return view;
    }

    private void fetchUserProfile() {
        ApiService apiService = RetrofitClient.getApiService(requireContext());
        apiService.getMe().enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentUser = response.body();
                    tvUserName.setText(currentUser.getFullName());
                    tvUserEmail.setText(currentUser.getEmail());
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Error fetching profile", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_EDIT_PROFILE && resultCode == android.app.Activity.RESULT_OK) {
            fetchUserProfile();
        }
    }
}
