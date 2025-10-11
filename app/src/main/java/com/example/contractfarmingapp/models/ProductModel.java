package com.example.contractfarmingapp.models;

public class ProductModel {
    private int id;
    private String name;
    private int price;
    private int soldCount;
    private float rating;

    private String imageUrl;

    public ProductModel(int id, String name, int price, int soldCount, float rating, String imageUrl) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.soldCount = soldCount;
        this.rating = rating;
        this.imageUrl = imageUrl;
    }

    // Getter
    public int getId() { return id; }
    public String getName() { return name; }
    public int getPrice() { return price; }
    public int getSoldCount() { return soldCount; }
    public float getRating() { return rating; }

    public String getImageUrl() { return imageUrl; }

    // Setter (optional, kalau perlu)
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setPrice(int price) { this.price = price; }
    public void setSoldCount(int soldCount) { this.soldCount = soldCount; }
    public void setRating(float rating) { this.rating = rating; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
