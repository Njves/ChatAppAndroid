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
import com.example.androiduni.socket.Socket
import com.example.androiduni.UserProvider
import com.example.androiduni.room.model.RoomModel
import com.example.androiduni.room.request.RoomService
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RoomAdapter(private val context: Context, private var roomModelList: MutableList<RoomModel>) : RecyclerView.Adapter<RoomAdapter.RoomViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomAdapter.RoomViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_room, parent, false)
        return RoomViewHolder(view)
    }

    override fun onBindViewHolder(holder: RoomAdapter.RoomViewHolder, position: Int) {
        holder.bind(roomModelList[position])
    }

    override fun getItemCount(): Int {
        return roomModelList.size
    }

    fun setData(roomModels: List<RoomModel>) {
        this.roomModelList = roomModels.toMutableList()
    }

    fun addData(rooms: List<RoomModel>) {
        this.roomModelList.addAll(rooms)
        notifyItemRangeInserted(this.roomModelList.size - rooms.size, this.roomModelList.size)
    }

    fun removeData(rooms: List<RoomModel>) {
        rooms.forEach {
            remove(it)
        }
    }

    fun remove(room: RoomModel) {
        val index = this.roomModelList.indexOf(room)
        this.roomModelList.removeAt(index)
        notifyItemRemoved(index)
    }

    inner class RoomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.roomTitle)
        private val buttonRemove: ImageButton = itemView.findViewById(R.id.btnRemove)
        fun bind(roomModel: RoomModel) {
            tvTitle.text = roomModel.name
            buttonRemove.setOnClickListener {
                Client.getClient().create(RoomService::class.java).removeRoom(roomModel.id, UserProvider.token!!).enqueue(object: Callback<Response<Void>> {
                    override fun onResponse(call: Call<Response<Void>>, response: Response<Response<Void>>) {
                        Log.d("RoomAdapter", response.toString())
                        if(response.isSuccessful) {
                            Log.d("RoomAdapter", roomModelList.indexOf(roomModel).toString())
                            val index = roomModelList.indexOf(roomModel)
                            this@RoomAdapter.notifyItemRemoved(index)
                            roomModelList.removeAt(index)
                            return
                        }
                        Toast.makeText(context, response.errorBody()?.string(), Toast.LENGTH_SHORT).show()
                    }

                    override fun onFailure(call: Call<Response<Void>>, t: Throwable) {
                        Log.d("RoomAdapter", t.toString())
                    }

                })

            }
            itemView.setOnClickListener {
                val gson = Gson()
                Socket.get().emit("join", gson.toJson(mapOf("id" to roomModel.id)))
                val intent = Intent(context, MessageListActivity::class.java)
                intent.putExtra("roomId", roomModel.id)
                intent.putExtra("roomName", roomModel.name)
                context.startActivity(intent)
            }
        }
    }
}
