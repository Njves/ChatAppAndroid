package com.example.androiduni.message

import android.os.Parcelable
import com.example.androiduni.user.User
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class Message(val id: Int, val text: String,
                   val date: Date, @SerializedName("room_id") val roomId: Int,
                    val attachments: List<Attachment>, val user: User): Parcelable