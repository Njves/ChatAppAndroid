package com.example.androiduni.room.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import androidx.room.Relation
import androidx.room.TypeConverters
import com.example.androiduni.database.converter.MessageConverter
import com.example.androiduni.message.Message
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Entity(tableName = "room")
@TypeConverters(MessageConverter::class)
data class RoomModel(
    @PrimaryKey val id: Int, val name: String, @SerializedName("owner_id") val ownerId: Int,
    @SerializedName("last_message") val lastMessage: Message?
) : Serializable