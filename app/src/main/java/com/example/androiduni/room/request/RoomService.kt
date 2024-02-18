package com.example.androiduni.room.request

import com.example.androiduni.room.model.RoomModel
import com.example.androiduni.room.model.RoomWithMessages
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface RoomService {
    @GET("/rooms")
    fun getRooms(): Call<List<RoomModel>>

    @GET("/room/{id}")
    fun getMessages(@Path("id") id: Int, @Query("count") count: Int, @Query("offset") offset: Int): Call<RoomWithMessages>

    @POST("/room")
    fun createRoom(@Body roomModel: RoomModel, @Header("Authorization") token: String): Call<RoomModel>

    @DELETE("/room/{id}")
    fun removeRoom(@Path("id") id: Int, @Header("Authorization") token: String): Call<Response<Void>>
}