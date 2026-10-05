package com.example.foodnow;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.foodnow.model.AuthResponse;
import com.example.foodnow.model.RegisterRequest;
import com.example.foodnow.network.ApiService;
import com.example.foodnow.network.RetrofitClient;
import com.example.foodnow.network.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignupActivity extends AppCompatActivity implements View.OnClickListener {
    EditText edtFullName, edtEmail, edtPassword, edtConfirmPassword;
    Button btnSignUp, btnSignIn;
    TextView btnSignUpFacebook, btnSignUpGoogle;
    TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        tokenManager = new TokenManager(this);

        edtFullName = findViewById(R.id.edtFullName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        btnSignUp = findViewById(R.id.btnSignUp);
        btnSignIn = findViewById(R.id.btnSignIn);
        btnSignUpFacebook = findViewById(R.id.btnSignUpFacebook);
        btnSignUpGoogle = findViewById(R.id.btnSignUpGoogle);

        btnSignUp.setOnClickListener(this);
        btnSignIn.setOnClickListener(this);
        btnSignUpFacebook.setOnClickListener(this);
        btnSignUpGoogle.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.btnSignUp) {
            handleSignUp();
        } else if (id == R.id.btnSignIn) {
            Intent intent = new Intent(this, SigninActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.btnSignUpFacebook) {
            Toast.makeText(this, "Signing up by Facebook", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.btnSignUpGoogle) {
            Toast.makeText(this, "Signing up by Google", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleSignUp() {
        String fullName = edtFullName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String confirmPassword = edtConfirmPassword.getText().toString().trim();

        if (fullName.isEmpty()) {
            edtFullName.setError("Please enter your full name");
            edtFullName.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            edtEmail.setError("Please enter your email");
            edtEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Please enter a valid email");
            edtEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            edtPassword.setError("Please enter a password");
            edtPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            edtPassword.setError("Password must be at least 6 characters");
            edtPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            edtConfirmPassword.setError("Passwords do not match");
            edtConfirmPassword.requestFocus();
            return;
        }

        btnSignUp.setEnabled(false);
        btnSignUp.setText("Signing up...");

        ApiService apiService = RetrofitClient.getApiService(this);
        RegisterRequest registerRequest = new RegisterRequest(email, password, fullName);

        apiService.register(registerRequest).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                btnSignUp.setEnabled(true);
                btnSignUp.setText(R.string.sign_up);
                if (response.isSuccessful() && response.body() != null) {
                    tokenManager.saveToken(response.body().getAccessToken());
                    Toast.makeText(SignupActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(SignupActivity.this, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                btnSignUp.setEnabled(true);
                btnSignUp.setText(R.string.sign_up);
                Toast.makeText(SignupActivity.this, "Unable to connect. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}