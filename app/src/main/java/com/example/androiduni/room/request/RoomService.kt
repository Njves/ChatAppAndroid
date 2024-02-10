package com.example.androiduni.room.request

import com.example.androiduni.message.Message
import com.example.androiduni.room.model.Room
import com.example.androiduni.room.model.RoomWithMessages
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface RoomService {
    @GET("/rooms")
    fun getRooms(): Call<List<Room>>

    @GET("/room/{id}")
    fun getMessages(@Path("id") id: Int): Call<RoomWithMessages>

    @POST("/room")
    fun createRoom(@Body room: Room, @Header("Authorization") token: String): Call<Room>

    @DELETE("/room/{id}")
    fun removeRoom(@Path("id") id: Int, @Header("Authorization") token: String): Call<Response<Void>>
}