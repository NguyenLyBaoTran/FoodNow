package com.example.foodnow.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class User implements Serializable {
    private int id;
    @SerializedName("full_name")
    private String fullName;
    private String email;
    private String role;

    public User(String fullName, String email) {
        this.fullName = fullName;
        this.email = email;
    }

    public int getId() { return id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
