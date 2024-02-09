package com.example.androiduni.room.model

import com.example.androiduni.message.Message
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Room(
    val id: Int, val name: String, @SerializedName("owner_id") val ownerId: Int,
    @SerializedName("last_message") val lastMessage: Message?
) : Serializable