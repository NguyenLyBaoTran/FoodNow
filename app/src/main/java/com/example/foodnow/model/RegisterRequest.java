package com.example.foodnow.model;

import com.google.gson.annotations.SerializedName;

public class RegisterRequest {
    private String email;
    private String password;
    @SerializedName("full_name")
    private String fullName;

    public RegisterRequest(String email, String password, String fullName) {
        this.email = email;
        this.password = password;
        this.fullName = fullName;
    }
}
