package com.example.foodnow.model;

import com.google.gson.annotations.SerializedName;

public class PaymentConfirmRequest {
    @SerializedName("transaction_code")
    private String transactionCode;

    public PaymentConfirmRequest(String transactionCode) {
        this.transactionCode = transactionCode;
    }
}
