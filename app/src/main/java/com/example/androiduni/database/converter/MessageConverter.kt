package com.example.androiduni.database.converter

import androidx.room.TypeConverter
import com.example.androiduni.message.Message
import com.google.gson.Gson

class MessageConverter {
    @TypeConverter
    fun fromMessage(message: Message?): String? {
        return Gson().toJson(message)
    }

    @TypeConverter
    fun toMessage(json: String?): Message? {
        return Gson().fromJson(json, Message::class.java)
    }
}