package com.example.androiduni.database.models

import androidx.room.Embedded
import androidx.room.Relation
import androidx.room.TypeConverters
import com.example.androiduni.database.converter.UserConverter
import com.example.androiduni.message.Attachment
import com.example.androiduni.message.Message
import com.example.androiduni.user.User

data class MessageWithUserAndAttachments(
    @Embedded val message: Message,
    @Relation(
        parentColumn = "user_id",
        entityColumn = "id"
    )
    val user: User,
    @Relation(
        parentColumn = "id",
        entityColumn = "message_id"
    )
    val attachments: List<Attachment>
)