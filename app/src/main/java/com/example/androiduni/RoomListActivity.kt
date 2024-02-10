package com.example.androiduni

import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.androiduni.room.model.Room
import com.example.androiduni.room.request.RoomService
import com.example.androiduni.ui.RoomAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class RoomListActivity : AppCompatActivity() {
    private lateinit var recyclerViewRoomList: RecyclerView
    private lateinit var buttonCreateRoom: FloatingActionButton
    private var roomList: MutableList<Room> = mutableListOf()
    private val gson: Gson = Gson()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        recyclerViewRoomList = findViewById(R.id.rvRoom)
        buttonCreateRoom = findViewById(R.id.floatingActionButton)
        recyclerViewRoomList.layoutManager = LinearLayoutManager(this)
        val service = Client.getClient().create(RoomService::class.java)

        service.getRooms().enqueue(object: Callback<List<Room>> {
            override fun onResponse(call: Call<List<Room>>, response: Response<List<Room>>) {
                roomList = response.body()!!.toMutableList()
                recyclerViewRoomList.adapter = RoomAdapter(this@RoomListActivity, roomList)
                Log.d(this@RoomListActivity.toString(), response.body()!!.toString())
                if(!response.isSuccessful) {
                    Log.d("RoomListActivity", response.message())
                    val error: Error = gson.fromJson(response.message(), Error::class.java)
                    Toast.makeText(this@RoomListActivity, error.error, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Room>>, t: Throwable) {
                Log.e(this@RoomListActivity.toString(), t.toString())
            }

        })


        buttonCreateRoom.setOnClickListener {
            val dialog = CreateRoomDialogFragment()
            dialog.show(supportFragmentManager, "Create Room")
        }

        Socket.get().on("new_room") { anies ->
            anies.forEach {
                roomList.add(gson.fromJson(it.toString(), Room::class.java))
                runOnUiThread {
                    recyclerViewRoomList.adapter?.notifyItemInserted(roomList.size - 1)
                }
            }
        }
    }
}