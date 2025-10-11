package com.example.contractfarmingapp.models;

public class VoucherModel {
    private String title;
    private String description;
    private String value;
    private String expiryDate; // Format: yyyy-MM-dd

    public VoucherModel(String title, String description, String value, String expiryDate) {
        this.title = title;
        this.description = description;
        this.value = value;
        this.expiryDate = expiryDate;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getValue() {
        return value;
    }
    public String getExpiryDate() {
        return expiryDate;
    }
}
