package com.example.foodnow.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Payment implements Serializable {
    private int id;
    @SerializedName("order_id")
    private int orderId;
    private String method;
    private String status;
    private double amount;
    @SerializedName("transaction_code")
    private String transactionCode;
    @SerializedName("created_at")
    private String createdAt;
    @SerializedName("paid_at")
    private String paidAt;

    public int getId() { return id; }
    public int getOrderId() { return orderId; }
    public String getMethod() { return method; }
    public String getStatus() { return status; }
    public double getAmount() { return amount; }
    public String getTransactionCode() { return transactionCode; }
    public String getCreatedAt() { return createdAt; }
    public String getPaidAt() { return paidAt; }
}
