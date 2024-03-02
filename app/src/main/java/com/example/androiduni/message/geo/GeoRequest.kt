package com.example.androiduni.message.geo

import com.example.androiduni.message.geo.model.GeoObject
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface GeoRequest {
    @GET("room/{id}/geo")
    fun getGeo(@Header("Authorization") token: String, @Path("id") roomId: Int): Call<GeoObject>
}