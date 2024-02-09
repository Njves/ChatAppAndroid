package com.example.androiduni.message

import com.example.androiduni.user.User
import com.google.gson.annotations.SerializedName
import java.util.Date

data class Message(val id: Int, val text: String,
                   val date: Date, @SerializedName("room_id") val roomId: Int,
                    val attachments: List<Attachment>, val user: User)