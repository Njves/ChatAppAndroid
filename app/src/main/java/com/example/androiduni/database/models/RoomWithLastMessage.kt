package com.example.androiduni.database.models

import androidx.room.Embedded
import androidx.room.Relation
import com.example.androiduni.message.Message
import com.example.androiduni.room.model.RoomModel

data class RoomWithLastMessage(
    @Embedded val room: RoomModel,
    @Relation(
        parentColumn = "id",
        entityColumn = "id"
    )
    val lastMessage: Message?
)