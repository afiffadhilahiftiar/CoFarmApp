package com.example.contractfarmingapp.network;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface UploadServiceKlaim {

    @Multipart
    @POST("upload_pembayaran_klaim.php")
    Call<ResponseBody> uploadBuktiKlaim(
            @Part("petani_id") RequestBody petaniId,
            @Part("user_id") RequestBody userId,
            @Part("company_id") RequestBody companyId,
            @Part("catatan") RequestBody catatan,
            @Part MultipartBody.Part foto
    );

}
