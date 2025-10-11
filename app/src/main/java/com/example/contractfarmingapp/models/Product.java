package com.example.contractfarmingapp.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Product {

    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("namaperusahaan")
    private String namaperusahaan;

    @SerializedName("lokasiperusahaan")
    private String lokasiperusahaan;

    @SerializedName("logoperusahaan")
    private String logoperusahaan;

    @SerializedName("price")
    private String price;

    @SerializedName("original_price")
    private String originalPrice;

    @SerializedName("sold_count")
    private int soldCount;

    @SerializedName("rating")
    private float rating;

    @SerializedName("review_count")
    private int reviewCount;

    @SerializedName("description")
    private String description;

    @SerializedName("image_urls")
    private List<String> imageUrls;

    @SerializedName("variations")
    private List<Variation> variations;

    @SerializedName("youtube_video_id")
    private String youtubeVideoId;

    // ====== GETTERS ======
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getNamaperusahaan() {
        return namaperusahaan;
    }

    public String getLokasiperusahaan() {
        return lokasiperusahaan;
    }

    public String getLogoperusahaan() {
        return logoperusahaan;
    }

    public String getPrice() {
        return price;
    }

    public String getOriginalPrice() {
        return originalPrice;
    }

    public int getSoldCount() {
        return soldCount;
    }

    public float getRating() {
        return rating;
    }

    public int getReviewCount() {
        return reviewCount;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public List<Variation> getVariations() {
        return variations;
    }

    public String getYoutubeVideoId() {
        return youtubeVideoId;
    }

    // ====== SETTERS ======
    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNamaperusahaan(String namaperusahaan) {
        this.namaperusahaan = namaperusahaan;
    }

    public void setLokasiperusahaan(String lokasiperusahaan) {
        this.lokasiperusahaan = lokasiperusahaan;
    }

    public void setLogoperusahaan(String logoperusahaan) {
        this.logoperusahaan = logoperusahaan;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setOriginalPrice(String originalPrice) {
        this.originalPrice = originalPrice;
    }

    public void setSoldCount(int soldCount) {
        this.soldCount = soldCount;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public void setReviewCount(int reviewCount) {
        this.reviewCount = reviewCount;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public void setVariations(List<Variation> variations) {
        this.variations = variations;
    }

    public void setYoutubeVideoId(String youtubeVideoId) {
        this.youtubeVideoId = youtubeVideoId;
    }
}
