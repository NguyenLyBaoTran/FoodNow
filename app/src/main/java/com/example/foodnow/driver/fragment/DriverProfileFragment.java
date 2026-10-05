package com.example.foodnow.driver.fragment;

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

import com.example.foodnow.R;
import com.example.foodnow.SigninActivity;
import com.example.foodnow.model.User;
import com.example.foodnow.network.ApiService;
import com.example.foodnow.network.RetrofitClient;
import com.example.foodnow.network.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DriverProfileFragment extends Fragment {

    private TextView tvUserName, tvUserEmail;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_driver_profile, container, false);

        tvUserName = view.findViewById(R.id.tvUserName);
        tvUserEmail = view.findViewById(R.id.tvUserEmail);

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
                    User user = response.body();
                    tvUserName.setText(user.getFullName());
                    tvUserEmail.setText(user.getEmail());
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
}
