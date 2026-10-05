package com.example.foodnow.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class Order implements Serializable {
    private int id;
    @SerializedName("address_id")
    private int addressId;
    @SerializedName("driver_id")
    private Integer driverId;
    @SerializedName("total_amount")
    private double totalAmount;
    private String status;
    @SerializedName("created_at")
    private String createdAt;
    @SerializedName("payment_method")
    private String paymentMethod;
    private List<OrderItem> items;
    private Payment payment;
    private Address address;

    public int getId() { return id; }
    public int getAddressId() { return addressId; }
    public Integer getDriverId() { return driverId; }
    public double getTotalAmount() { return totalAmount; }
    public String getStatus() { return status; }
    public String getCreatedAt() { return createdAt; }
    public String getPaymentMethod() { return paymentMethod; }
    public List<OrderItem> getItems() { return items; }
    public Payment getPayment() { return payment; }
    public Address getAddress() { return address; }
}
