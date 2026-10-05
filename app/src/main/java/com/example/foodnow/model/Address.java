package com.example.foodnow.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Address implements Serializable {
    private int id;
    @SerializedName("user_id")
    private int userId;
    @SerializedName("recipient_name")
    private String recipientName;
    private String phone;
    @SerializedName("address_line")
    private String addressLine;
    @SerializedName("is_default")
    private boolean isDefault;

    public Address(String recipientName, String phone, String addressLine) {
        this.recipientName = recipientName;
        this.phone = phone;
        this.addressLine = addressLine;
    }

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getRecipientName() { return recipientName; }
    public String getPhone() { return phone; }
    public String getAddressLine() { return addressLine; }
    public boolean isDefault() { return isDefault; }
}