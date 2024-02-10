package com.example.androiduni.ui

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.androiduni.Client
import com.example.androiduni.MessageListActivity
import com.example.androiduni.R
import com.example.androiduni.Socket
import com.example.androiduni.UserProvider
import com.example.androiduni.room.model.Room
import com.example.androiduni.room.request.RoomService
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.Serializable

class RoomAdapter(private val context: Context, private val roomList: MutableList<Room>) : RecyclerView.Adapter<RoomAdapter.RoomViewHolder>() {
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

    inner class RoomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.roomTitle)
        private val buttonRemove: ImageButton = itemView.findViewById(R.id.btnRemove)
        fun bind(room: Room) {
            tvTitle.text = room.name
            buttonRemove.setOnClickListener {
                Client.getClient().create(RoomService::class.java).removeRoom(room.id, UserProvider.token!!).enqueue(object: Callback<Response<Void>> {
                    override fun onResponse(call: Call<Response<Void>>, response: Response<Response<Void>>) {
                        Log.d("RoomAdapter", response.toString())
                        if(response.isSuccessful) {
                            Log.d("RoomAdapter", roomList.indexOf(room).toString())
                            val index = roomList.indexOf(room)
                            this@RoomAdapter.notifyItemRemoved(index)
                            roomList.removeAt(index)
                        }
                    }

                    override fun onFailure(call: Call<Response<Void>>, t: Throwable) {
                        Log.d("RoomAdapter", t.toString())
                    }

                })

            }
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
