package com.example.androiduni.message.request

import com.example.androiduni.message.Attachment
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface MessageRequest {
    @DELETE("message/{id}")
    fun removeMessage(@Path("id") id: Int, @Header("Authorization") auth: String): Call<Response<Void>>

    @Multipart
    @POST("upload")
    fun uploadImage(@Header("Authorization") token: String, @Part file: MultipartBody.Part): Call<List<Attachment>>
}