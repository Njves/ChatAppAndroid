package com.example.androiduni.database.converter

import androidx.room.TypeConverter
import com.example.androiduni.message.Message
import com.example.androiduni.user.User
import com.google.gson.Gson

class UserConverter {
    @TypeConverter
    fun fromUser(user: User?): String? {
        return Gson().toJson(user)
    }

    @TypeConverter
    fun toUser(json: String?): User? {
        return Gson().fromJson(json, User::class.java)
    }
}