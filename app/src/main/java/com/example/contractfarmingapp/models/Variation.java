package com.example.contractfarmingapp.models;

import java.text.NumberFormat;
import java.util.Locale;

public class Variation {
    private String id;
    private String name;
    private int priceDifference;

    public Variation(String id, String name, int priceDifference) {
        this.id = id;
        this.name = name;
        this.priceDifference = priceDifference;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getPriceDifference() {
        return priceDifference;
    }

    @Override
    public String toString() {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));
        formatter.setMinimumFractionDigits(0); // Jangan tampilkan angka di belakang koma
        formatter.setMaximumFractionDigits(0);
        String formattedPrice = formatter.format(priceDifference);
        return name + " (+" + formattedPrice + ")";
    }
}
