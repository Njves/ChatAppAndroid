package com.example.androiduni.ui

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.androiduni.MessageListActivity
import com.example.androiduni.R
import com.example.androiduni.Socket
import com.example.androiduni.room.model.Room
import com.google.gson.Gson
import java.io.Serializable

class RoomAdapter(private val context: Context, private val roomList: List<Room>) : RecyclerView.Adapter<RoomAdapter.RoomViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomAdapter.RoomViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_room, parent, false)
        return RoomViewHolder(view)
    }

    override fun onBindViewHolder(holder: RoomAdapter.RoomViewHolder, position: Int) {
        holder.bind(roomList[position])
    }

    override fun getItemCount(): Int {
        return roomList.size
    }

    inner class RoomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        fun bind(room: Room) {
            val tvTitle = itemView.findViewById<TextView>(R.id.roomTitle)
            tvTitle.text = room.name
            itemView.setOnClickListener {
                val gson = Gson()
                Socket.get().emit("join", gson.toJson(room))
                val intent = Intent(context, MessageListActivity::class.java)
                intent.putExtra("roomId", room.id)
                context.startActivity(intent)
            }
        }
    }
}
