package com.example.androiduni.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.androiduni.database.models.RoomWithLastMessage
import com.example.androiduni.room.model.RoomModel


@Dao
interface RoomDao {
    @Transaction
    @Query("SELECT * FROM room")
    suspend fun getAllRooms(): List<RoomWithLastMessage>?

    @Transaction
    @Query("SELECT * FROM room WHERE id = :roomId LIMIT 1")
    suspend fun getRoomById(roomId: Int): RoomWithLastMessage

    @Insert
    suspend fun insertRoom(roomModel: RoomModel)

//    @Update
//    fun updateRoom(roomModel: RoomWithLastMessage)
//
//    @Delete
//    fun deleteRoom(roomModel: RoomWithLastMessage)
}