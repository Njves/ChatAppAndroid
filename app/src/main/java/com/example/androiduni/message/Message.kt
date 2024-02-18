package com.example.androiduni.message

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.androiduni.database.converter.AttachmentConverter
import com.example.androiduni.database.converter.AttachmentListConverter
import com.example.androiduni.database.converter.DateConverter
import com.example.androiduni.database.converter.UserConverter
import com.example.androiduni.user.User
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.util.Date

@Entity(tableName = "message")
@TypeConverters(DateConverter::class, UserConverter::class, AttachmentListConverter::class)
@Parcelize
data class Message(@PrimaryKey val id: Int, val text: String,
                   val date: Date, @SerializedName("room_id") val roomId: Int,
                   val attachments: List<Attachment>,
                   val user: User): Parcelable