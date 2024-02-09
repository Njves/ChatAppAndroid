package com.example.androiduni.room.request

import com.example.androiduni.message.Message
import com.example.androiduni.room.model.Room
import com.example.androiduni.room.model.RoomWithMessages
import retrofit2.Call
import retrofit2.http.Body
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
}