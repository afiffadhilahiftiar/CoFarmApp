package com.example.contractfarmingapp.api;

import com.example.contractfarmingapp.models.GeoFenceModel;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface GeoFenceApi {
    @POST("save_geofence.php")
    Call<Void> saveGeoFence(@Body GeoFenceModel model);
}
