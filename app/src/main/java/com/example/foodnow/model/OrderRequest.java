package com.example.foodnow.model;

import com.google.gson.annotations.SerializedName;

public class OrderRequest {
    @SerializedName("address_id")
    private int addressId;
    @SerializedName("payment_method")
    private String paymentMethod;

    public OrderRequest(int addressId, String paymentMethod) {
        this.addressId = addressId;
        this.paymentMethod = paymentMethod;
    }
}