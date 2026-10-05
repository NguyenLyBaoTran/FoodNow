package com.example.foodnow.model;

public class LoginRequest {
    private String username; // OAuth2PasswordRequestForm expects 'username' for email
    private String password;

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
