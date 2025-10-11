package com.example.contractfarmingapp.models;

public class ProdukCheckoutModel {
    private String namaProduk;
    private int jumlah;
    private double harga;
    private String variasi;

    public ProdukCheckoutModel(String namaProduk, String variasi, int jumlah, double harga) {
        this.namaProduk = namaProduk;
        this.jumlah = jumlah;
        this.harga = harga;
        this.variasi = variasi;
    }

    public String getNamaProduk() {
        return namaProduk;
    }

    public String getVariasi() {
        return variasi;
    }
    public int getJumlah() {
        return jumlah;
    }
    public double getHarga() {
        return harga;
    }
}
