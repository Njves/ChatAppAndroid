package com.example.androiduni.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room.databaseBuilder
import androidx.room.RoomDatabase
import com.example.androiduni.database.models.RoomWithLastMessage
import com.example.androiduni.message.Attachment
import com.example.androiduni.message.Message
import com.example.androiduni.room.model.RoomModel
import com.example.androiduni.user.User


@Database(entities = [RoomModel::class, Message::class, Attachment::class, User::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun roomDao(): RoomDao?

    companion object {
        private var instance: AppDatabase? = null
        @Synchronized
        fun getInstance(context: Context): AppDatabase? {
            if (instance == null) {
                instance = databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java, "app_database"
                )
                    .fallbackToDestructiveMigration()
                    .build();
            }
            return instance
        }
    }
}