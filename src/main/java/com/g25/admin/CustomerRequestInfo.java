package com.g25.admin;

public class CustomerRequestInfo {
    public String requestId;
    public String customerId;
    public double topupValue;

    public CustomerRequestInfo(String requestId, String customerId, double topupValue) {
        this.requestId = requestId;
        this.customerId = customerId;
        this.topupValue = topupValue;
    }

    public String getId() {
        return this.requestId;
    }

    public String getCustomerId() {
        return this.customerId;
    }

    public double getTopupValue() {
        return this.topupValue;
    }
}