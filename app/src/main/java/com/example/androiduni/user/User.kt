package com.example.androiduni.user

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(val id: Int, val username: String, val color: String, @SerializedName("last_seen") val lastSeen: String): Parcelable