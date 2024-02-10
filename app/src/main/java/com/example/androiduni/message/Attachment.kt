package com.example.androiduni.message

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Attachment(val id: Int, val type: String, val link: String,
                      @SerializedName("message_id") val messageId: Int): Parcelable