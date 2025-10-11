package com.example.contractfarmingapp.models;

public class CartItem {
    private int id;
    private String userEmail;
    private int productId;
    private int variationId;
    private int quantity;
    private double totalPrice;
    private double pricePerUnit; // Tambahan
    private String addedAt;
    private String namaProduk;
    private String namaPerusahaan;
    private String namaVariasi;
    private String logoProduk;

    // Constructor dengan pricePerUnit
    public CartItem(int id, String userEmail, int productId, int variationId, int quantity, double totalPrice,
                    double pricePerUnit, String addedAt, String namaProduk, String namaPerusahaan,
                    String namaVariasi, String logoProduk) {
        this.id = id;
        this.userEmail = userEmail;
        this.productId = productId;
        this.variationId = variationId;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.pricePerUnit = pricePerUnit;
        this.addedAt = addedAt;
        this.namaProduk = namaProduk;
        this.namaPerusahaan = namaPerusahaan;
        this.namaVariasi = namaVariasi;
        this.logoProduk = logoProduk;
    }

    // Getters
    public int getId() { return id; }
    public String getUserEmail() { return userEmail; }
    public int getProductId() { return productId; }
    public int getVariationId() { return variationId; }
    public int getQuantity() { return quantity; }
    public double getTotalPrice() { return totalPrice; }
    public double getPricePerUnit() { return pricePerUnit; }
    public String getAddedAt() { return addedAt; }
    public String getNamaProduk() { return namaProduk; }
    public String getNamaPerusahaan() { return namaPerusahaan; }
    public String getNamaVariasi() { return namaVariasi; }
    public String getLogoProduk() { return logoProduk; }
    private boolean isChecked = false;

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }
    // Setters
    public void setQuantity(int quantity) {
        this.quantity = quantity;
        recalculateTotal();
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public void setPricePerUnit(double pricePerUnit) {
        this.pricePerUnit = pricePerUnit;
        recalculateTotal();
    }

    // Logic
    public void increaseQuantity() {
        this.quantity++;
        recalculateTotal();
    }

    public void decreaseQuantity() {
        if (this.quantity > 1) {
            this.quantity--;
            recalculateTotal();
        }
    }

    public void updateQuantity(int newQuantity) {
        if (newQuantity >= 1) {
            this.quantity = newQuantity;
            recalculateTotal();
        }
    }

    private void recalculateTotal() {
        this.totalPrice = this.quantity * this.pricePerUnit;
    }
}
