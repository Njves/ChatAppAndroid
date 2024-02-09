package com.example.androiduni.user

import com.google.gson.annotations.SerializedName

data class User(val id: Int, val username: String, val color: String, @SerializedName("last_seen") val lastSeen: String)