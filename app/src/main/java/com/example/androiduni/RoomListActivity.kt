package com.example.androiduni

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.androiduni.room.model.Room
import com.example.androiduni.room.request.RoomService
import com.example.androiduni.ui.RoomAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class RoomListActivity : AppCompatActivity() {
    private lateinit var recyclerViewRoomList: RecyclerView
    private lateinit var buttonCreateRoom: FloatingActionButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        recyclerViewRoomList = findViewById(R.id.rvRoom)
        buttonCreateRoom = findViewById(R.id.floatingActionButton)
        recyclerViewRoomList.layoutManager = LinearLayoutManager(this)
            val service = Client.getClient().create(RoomService::class.java)
        service.getRooms().enqueue(object: Callback<List<Room>> {
            override fun onResponse(call: Call<List<Room>>, response: Response<List<Room>>) {
                recyclerViewRoomList.adapter = RoomAdapter(this@RoomListActivity, response.body()!!)
                Log.d(this@RoomListActivity.toString(), response.body()!!.toString())
            }

            override fun onFailure(call: Call<List<Room>>, t: Throwable) {
                Log.e(this@RoomListActivity.toString(), t.toString())
            }

        })

        buttonCreateRoom.setOnClickListener {
            val dialog = CreateRoomDialogFragment()
            dialog.show(supportFragmentManager, "Create Room")
        }
    }
}