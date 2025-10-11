package com.example.contractfarmingapp.models;

public class GridProduct {
    private int id;
    private String name;
    private double price;
    private float averageRating;

    public GridProduct(int id, String name, double price, float averageRating) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.averageRating = averageRating;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public float getAverageRating() {
        return averageRating;
    }
}
