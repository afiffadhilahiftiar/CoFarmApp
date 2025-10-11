package com.example.contractfarmingapp.models;

public class OngkirModel {
    public String courierName;
    public String serviceType;
    public String description;
    public int estimatedCost;
    public String estimatedDeliveryTime;

    public OngkirModel(String courierName, String serviceType, String description, int estimatedCost, String estimatedDeliveryTime) {
        this.courierName = courierName;
        this.serviceType = serviceType;
        this.description = description;
        this.estimatedCost = estimatedCost;
        this.estimatedDeliveryTime = estimatedDeliveryTime;
    }
}
