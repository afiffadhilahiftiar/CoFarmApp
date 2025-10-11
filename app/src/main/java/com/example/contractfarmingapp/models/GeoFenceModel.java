package com.example.contractfarmingapp.models;

public class GeoFenceModel {
    private double area;
    private String coordinates; // GeoJSON

    public GeoFenceModel(double area, String coordinates) {
        this.area = area;
        this.coordinates = coordinates;
    }
}

