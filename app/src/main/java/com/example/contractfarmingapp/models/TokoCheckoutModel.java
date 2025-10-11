package com.example.contractfarmingapp.models;

import java.util.List;

public class TokoCheckoutModel {
    private String namaToko;
    private List<ProdukCheckoutModel> produkList;

    // Info pengiriman (wajib untuk ongkir)
    private String originId;
    private String destinationId;
    private String originPinPoint;
    private String destinationPinPoint;
    private String cod; // "yes" atau "no"
    private float weight;
    private int itemValue;

    // Informasi ongkir yang dikembalikan
    private int ongkir;
    private String kurir;
    private String layanan;
    private String etd;

    public TokoCheckoutModel(String namaToko, List<ProdukCheckoutModel> produkList) {
        this.namaToko = namaToko;
        this.produkList = produkList;
    }

    // --- Getters & Setters ---

    public String getNamaToko() {
        return namaToko;
    }

    public void setNamaToko(String namaToko) {
        this.namaToko = namaToko;
    }

    public List<ProdukCheckoutModel> getProdukList() {
        return produkList;
    }

    public void setProdukList(List<ProdukCheckoutModel> produkList) {
        this.produkList = produkList;
    }

    // === Info ongkir ===

    public int getOngkir() {
        return ongkir;
    }

    public void setOngkir(int ongkir) {
        this.ongkir = ongkir;
    }

    public String getKurir() {
        return kurir;
    }

    public void setKurir(String kurir) {
        this.kurir = kurir;
    }

    public String getLayanan() {
        return layanan;
    }

    public void setLayanan(String layanan) {
        this.layanan = layanan;
    }

    public String getEtd() {
        return etd;
    }

    public void setEtd(String etd) {
        this.etd = etd;
    }

    // === Data pengiriman ===

    public String getOriginId() {
        return originId;
    }

    public void setOriginId(String originId) {
        this.originId = originId;
    }

    public String getDestinationId() {
        return destinationId;
    }

    public void setDestinationId(String destinationId) {
        this.destinationId = destinationId;
    }

    public String getOriginPinPoint() {
        return originPinPoint;
    }

    public void setOriginPinPoint(String originPinPoint) {
        this.originPinPoint = originPinPoint;
    }

    public String getDestinationPinPoint() {
        return destinationPinPoint;
    }

    public void setDestinationPinPoint(String destinationPinPoint) {
        this.destinationPinPoint = destinationPinPoint;
    }

    public String getCod() {
        return cod;
    }

    public void setCod(String cod) {
        this.cod = cod;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public int getItemValue() {
        return itemValue;
    }

    public void setItemValue(int itemValue) {
        this.itemValue = itemValue;
    }
}
