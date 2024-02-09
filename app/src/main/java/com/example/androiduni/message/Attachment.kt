package com.example.androiduni.message

import com.google.gson.annotations.SerializedName

data class Attachment(val id: Int, val type: String, val link: String,
                      @SerializedName("message_id") val messageId: Int)