package com.example.foodnow;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ForgotPassword extends AppCompatActivity implements View.OnClickListener {

    EditText edtEmail;
    Button btnSend, btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        edtEmail = findViewById(R.id.edtEmail);
        btnSend = findViewById(R.id.btnSend);
        btnBack = findViewById(R.id.btnBack);

        btnSend.setOnClickListener(this);
        btnBack.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btnSend) {
            handleSend();
        } else if (id == R.id.btnBack) {
            finish();
        }
    }

    private void handleSend() {
        String email = edtEmail.getText().toString().trim();
        if (email.isEmpty()) {
            edtEmail.setError("Please enter your email");
            edtEmail.requestFocus();
            return;
        }
        
        Toast.makeText(this, "Recovery email sent to: " + email, Toast.LENGTH_LONG).show();
        finish();
    }
}