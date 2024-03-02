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
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.androiduni.auth.MainActivity
import com.example.androiduni.database.AppDatabase
import com.example.androiduni.database.models.RoomWithLastMessage
import com.example.androiduni.room.model.RoomModel
import com.example.androiduni.room.request.RoomService
import com.example.androiduni.room.view_model.RoomResponseViewModel
import com.example.androiduni.socket.Socket
import com.example.androiduni.ui.RoomAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
    private val roomResponseViewModel: RoomResponseViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Socket.setConnect(this)
        recyclerViewRoomList = findViewById(R.id.rvRoom)
        buttonCreateRoom = findViewById(R.id.floatingActionButton)
        progressBar = findViewById(R.id.progressBar)
        recyclerViewRoomList.layoutManager = LinearLayoutManager(this)
        adapter = RoomAdapter(this@RoomListActivity, roomModelList)
        recyclerViewRoomList.adapter = adapter
        supportActionBar?.title = UserProvider.user?.username
        val databaseRoomsHashSet: HashSet<RoomModel> = hashSetOf()
        CoroutineScope(Dispatchers.IO).launch {
            AppDatabase.getInstance(this@RoomListActivity)?.let {appDatabase ->
                appDatabase.roomDao()?.getAllRooms()?.let { data ->
                    databaseRoomsHashSet.addAll(data.map { it.room })
                }
            }
        }
        val networkRoomsHashSet: HashSet<RoomModel> = hashSetOf()
        CoroutineScope(Dispatchers.IO).launch {
            val response = service.getRooms().execute()
            CoroutineScope(Dispatchers.Main).launch {
                progressBar.visibility = GONE
                response.body()?.let {
                    networkRoomsHashSet.addAll(it)
                    networkRoomsHashSet.removeAll(databaseRoomsHashSet)
                    adapter.addData(networkRoomsHashSet.toList().sortedBy { it.id })
                }
                Log.d(this@RoomListActivity.toString(), response.body()!!.toString())
                if (!response.isSuccessful) {
                    Log.d("RoomListActivity", response.message())
                    val error: Error = gson.fromJson(response.message(), Error::class.java)
                    Toast.makeText(this@RoomListActivity, error.error, Toast.LENGTH_SHORT).show()
                }
            }
        }

        roomResponseViewModel.response.observe(this, Observer { item ->
            service.createRoom(
                RoomModel(
                    -1,
                    item,
                    UserProvider.user!!.id,
                    null
                ), UserProvider.token!!
            ).enqueue(object: Callback<RoomModel> {
                override fun onResponse(call: Call<RoomModel>, response: Response<RoomModel>) {
                    if (!response.isSuccessful) {
                        Log.d("CreateRoomDialogFragment", response.errorBody()!!.string())
                        Toast.makeText(this@RoomListActivity, "Неудалось создать комнату", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<RoomModel>, t: Throwable) {
                    Log.d("CreateRoomDialogFragment", t.toString())
                }
            })

            Toast.makeText(this@RoomListActivity, item, Toast.LENGTH_SHORT).show()
        })

        buttonCreateRoom.setOnClickListener {
            val dialog = CreateRoomDialogFragment()
            dialog.show(supportFragmentManager, "Create Room")
        }

        Socket.get().on("new_room") { anies ->
            anies.forEach {
                runOnUiThread {
                    adapter.addData(listOf(gson.fromJson(it.toString(), RoomModel::class.java)))
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
            val confirmDialog = AlertDialog.Builder(this).setTitle("Вы уверены что хотите выйти?").setPositiveButton("Да") { dialog, id ->
                UserProvider.logoutUser(this.applicationContext)
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }.setNegativeButton("Нет") { dialog, id ->
                dialog.cancel()
            }
            confirmDialog.show()

        }
        return super.onOptionsItemSelected(item)
    }
}