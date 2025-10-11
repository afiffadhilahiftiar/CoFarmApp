package com.example.contractfarmingapp.models;

public class InvoiceHistory {
    private int id;
    private String paymentMethod;
    private int amount;
    private String timestamp;
    private String status;

    public InvoiceHistory(int id, String paymentMethod, int amount, String timestamp, String status) {
        this.id = id;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.timestamp = timestamp;
        this.status = status;
    }

    public int getId() { return id; }
    public String getPaymentMethod() { return paymentMethod; }
    public int getAmount() { return amount; }
    public String getTimestamp() { return timestamp; }
    public String getStatus() { return status; }
}

