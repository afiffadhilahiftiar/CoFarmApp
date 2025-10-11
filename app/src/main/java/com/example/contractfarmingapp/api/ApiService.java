package com.example.contractfarmingapp.api;

import com.example.contractfarmingapp.models.Product;
import com.example.contractfarmingapp.models.Review;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ApiService {

    @GET("products/{id}")
    Call<Product> getProductDetail(@Path("id") String productId);

    @GET("products/{id}/reviews")
    Call<List<Review>> getProductReviews(@Path("id") String productId);
}

