package com.example.contractfarmingapp.models;

public class UlasanModel {
    private String namaPerusahaan;
    private String ulasan;
    private float rating;

    public UlasanModel(String namaPerusahaan, String ulasan, float rating) {
        this.namaPerusahaan = namaPerusahaan;
        this.ulasan = ulasan;
        this.rating = rating;
    }

    public String getNamaPerusahaan() {
        return namaPerusahaan;
    }

    public String getUlasan() {
        return ulasan;
    }

    public float getRating() {
        return rating;
    }
}
