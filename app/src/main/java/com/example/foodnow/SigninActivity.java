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

import com.example.foodnow.model.User;
import com.example.foodnow.model.AuthResponse;
import com.example.foodnow.network.ApiService;
import com.example.foodnow.network.RetrofitClient;
import com.example.foodnow.network.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SigninActivity extends AppCompatActivity implements View.OnClickListener{
    EditText edtEmail, edtPassword;
    Button btnForgot, btnSignIn, btnSignUp;
    TextView btnSignInFacebook, btnSignInGoogle;
    TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        tokenManager = new TokenManager(this);
        if (tokenManager.isLoggedIn()) {
            navigateToCorrectMain();
        }

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnForgot = findViewById(R.id.btnForgot);
        btnSignIn = findViewById(R.id.btnSignIn);
        btnSignUp = findViewById(R.id.btnSignUp);
        btnSignInFacebook = findViewById(R.id.btnSignInFacebook);
        btnSignInGoogle = findViewById(R.id.btnSignInGoogle);

        btnForgot.setOnClickListener(this);
        btnSignIn.setOnClickListener(this);
        btnSignUp.setOnClickListener(this);
        btnSignInFacebook.setOnClickListener(this);
        btnSignInGoogle.setOnClickListener(this);
    }
    @Override
    public void onClick(View v){
        int id = v.getId();

        if (id == R.id.btnSignIn) {
            handleSignIn();
        } else if (id == R.id.btnSignUp) {
            Intent intent = new Intent(this, SignupActivity.class);
            startActivity(intent);
        } else if (id == R.id.btnForgot) {
            Intent intent = new Intent(this, ForgotPassword.class);
            startActivity(intent);
        } else if (id == R.id.btnSignInFacebook) {
            Toast.makeText(this, "Signing by Facebook button is clicked", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.btnSignInGoogle) {
            Toast.makeText(this, "Signing by Google button is clicked", Toast.LENGTH_SHORT).show();
        }
    }
    private void handleSignIn() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (email.isEmpty()) {
            edtEmail.setError("Please enter your email");
            edtEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Please enter a valid email address");
            edtEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            edtPassword.setError("Please enter your password");
            edtPassword.requestFocus();
            return;
        }

        btnSignIn.setEnabled(false);
        btnSignIn.setText("Signing in...");

        ApiService apiService = RetrofitClient.getApiService(this);
        apiService.login(email, password).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                btnSignIn.setEnabled(true);
                btnSignIn.setText(R.string.sign_in);
                if (response.isSuccessful() && response.body() != null) {
                    tokenManager.saveToken(response.body().getAccessToken());
                    Toast.makeText(SigninActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                    navigateToCorrectMain();
                } else {
                    Toast.makeText(SigninActivity.this, "Login failed. Please check your email and password.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                btnSignIn.setEnabled(true);
                btnSignIn.setText(R.string.sign_in);
                Toast.makeText(SigninActivity.this, "Unable to connect. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToCorrectMain() {
        ApiService apiService = RetrofitClient.getApiService(this);
        apiService.getMe().enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    Intent intent;
                    if ("DRIVER".equals(user.getRole())) {
                        intent = new Intent(SigninActivity.this, com.example.foodnow.driver.DriverMainActivity.class);
                    } else if ("ADMIN".equals(user.getRole())) {
                        intent = new Intent(SigninActivity.this, com.example.foodnow.admin.AdminMainActivity.class);
                    } else {
                        intent = new Intent(SigninActivity.this, MainActivity.class);
                    }
                    startActivity(intent);
                    finish();
                } else {
                    tokenManager.clearToken();
                    Toast.makeText(SigninActivity.this, "Session expired, please login again", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(SigninActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}