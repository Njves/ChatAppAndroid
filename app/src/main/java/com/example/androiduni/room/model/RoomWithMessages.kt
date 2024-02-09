package com.example.androiduni.room.model

import com.example.androiduni.message.Message
import com.google.gson.annotations.SerializedName

data class RoomWithMessages(val id: Int, val messages: List<Message>, val name: String, @SerializedName("owner_id") val ownerId: Int)