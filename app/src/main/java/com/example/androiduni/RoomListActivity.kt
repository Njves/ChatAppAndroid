package com.example.androiduni

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View.GONE
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.androiduni.auth.MainActivity
import com.example.androiduni.database.AppDatabase
import com.example.androiduni.room.model.RoomModel
import com.example.androiduni.room.request.RoomService
import com.example.androiduni.ui.RoomAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class RoomListActivity : AppCompatActivity() {
    private lateinit var recyclerViewRoomList: RecyclerView
    private lateinit var buttonCreateRoom: FloatingActionButton
    private lateinit var progressBar: ProgressBar
    private var roomModelList: MutableList<RoomModel> = mutableListOf()
    private val gson: Gson = Gson()
    private val service = Client.getClient().create(RoomService::class.java)
    private lateinit var adapter: RoomAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        recyclerViewRoomList = findViewById(R.id.rvRoom)
        buttonCreateRoom = findViewById(R.id.floatingActionButton)
        progressBar = findViewById(R.id.progressBar)
        recyclerViewRoomList.layoutManager = LinearLayoutManager(this)

        service.getRooms().enqueue(object: Callback<List<RoomModel>> {
            override fun onResponse(call: Call<List<RoomModel>>, response: Response<List<RoomModel>>) {
                progressBar.visibility = GONE
                roomModelList = response.body()!!.toMutableList()
                adapter = RoomAdapter(this@RoomListActivity, roomModelList)
                recyclerViewRoomList.adapter = adapter
                Log.d(this@RoomListActivity.toString(), response.body()!!.toString())
                if(!response.isSuccessful) {
                    Log.d("RoomListActivity", response.message())
                    val error: Error = gson.fromJson(response.message(), Error::class.java)
                    Toast.makeText(this@RoomListActivity, error.error, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<RoomModel>>, t: Throwable) {
                progressBar.visibility = GONE
                Log.e(this@RoomListActivity.toString(), t.toString())
            }

        })
        CoroutineScope(Dispatchers.IO).launch {
            AppDatabase.getInstance(this@RoomListActivity)?.let {
                val rooms = it.roomDao()?.getAllRooms()!!
                if(rooms.isNotEmpty()) {
                    CoroutineScope(Dispatchers.Main).launch {
                        Log.d(this@RoomListActivity.toString(), "Из базы данных пришли данные $rooms")
                        adapter.addData(roomModelList + rooms.map {
                            it.room
                        }.toMutableList())
                        Toast.makeText(this@RoomListActivity, "из базы данных $rooms.size", Toast.LENGTH_SHORT).show()
                    }

                }

            }
        }


        buttonCreateRoom.setOnClickListener {
            val dialog = CreateRoomDialogFragment()
            dialog.show(supportFragmentManager, "Create Room")
        }

        Socket.get().on("new_room") { anies ->
            anies.forEach {
                roomModelList.add(gson.fromJson(it.toString(), RoomModel::class.java))
                runOnUiThread {
                    recyclerViewRoomList.adapter?.notifyItemInserted(roomModelList.size - 1)
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.dashboard, menu)

        val searchItem: MenuItem? = menu?.findItem(R.id.action_search)
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView: SearchView = searchItem?.actionView as SearchView

        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                val searchedList: List<RoomModel> = roomModelList.filter {
                    it.name.startsWith(query!!)
                }
                adapter.setData(searchedList)
                adapter.notifyDataSetChanged()
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val searchedList: List<RoomModel> = roomModelList.filter {
                    it.name.lowercase().startsWith(newText!!)
                }
                adapter.setData(searchedList)
                adapter.notifyDataSetChanged()
                return false
            }
        })
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if(id == R.id.action_logout) {
            UserProvider.logoutUser(this.applicationContext)
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}