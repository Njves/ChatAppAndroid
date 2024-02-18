package com.example.androiduni.database.converter

import androidx.room.TypeConverter
import com.example.androiduni.message.Attachment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.util.Arrays
import java.util.stream.Collectors

class AttachmentListConverter {
    val gson = Gson()
    @TypeConverter
    fun fromAttachment(attachments: List<Attachment>?): String? {
        return gson.toJson(attachments)
    }

    @TypeConverter
    fun toAttachment(json: String?): List<Attachment>? {
        val type: Type = object : TypeToken<List<Attachment>>() {}.type
        return gson.fromJson(json, type)
    }
}