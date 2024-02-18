package com.example.androiduni.database.converter

import androidx.room.TypeConverter
import com.example.androiduni.message.Attachment
import com.example.androiduni.user.User
import com.google.gson.Gson

class AttachmentConverter {
    @TypeConverter
    fun fromAttachment(attachment: Attachment?): String? {
        return Gson().toJson(attachment)
    }

    @TypeConverter
    fun toAttachment(json: String?): Attachment? {
        return Gson().fromJson(json, Attachment::class.java)
    }
}