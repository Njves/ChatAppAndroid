package com.example.androiduni.user

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "user")
data class User(@PrimaryKey val id: Int, val username: String, val color: String, @SerializedName("last_seen") val lastSeen: String): Parcelable