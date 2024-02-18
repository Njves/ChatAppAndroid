package com.example.androiduni.message

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Entity(tableName = "attachment")
@Parcelize
data class Attachment(@PrimaryKey val id: Int, val type: String, val link: String,
    @SerializedName("message_id") val messageId: Int): Parcelable