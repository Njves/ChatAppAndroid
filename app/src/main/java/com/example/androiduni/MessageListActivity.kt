package com.example.androiduni

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.OnLayoutChangeListener
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.androiduni.message.Message
import com.example.androiduni.room.model.RoomWithMessages
import com.example.androiduni.room.request.RoomService
import com.example.androiduni.ui.MessageAdapter
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Date


class MessageListActivity : AppCompatActivity() {
    private lateinit var recyclerViewMessagesList: RecyclerView
    private lateinit var buttonSend: ImageButton
    private lateinit var editText: EditText
    private var roomId: Int = -1
    private var messageList: MutableList<Message> = mutableListOf()
    private val roomService: RoomService = Client.getClient().create(RoomService::class.java)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_list)
        intent.extras.let {
            if (it != null) {
                roomId = it.getInt("roomId", 0)
            }

        }
        if(roomId == -1) {
            finish()
        }
        buttonSend = findViewById(R.id.btnSend)
        editText = findViewById(R.id.inputMessage)
        buttonSend.setOnClickListener {
            sendMessage()
            editText.text.clear()
        }
        listenNewMessage()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        recyclerViewMessagesList = findViewById(R.id.rvMessages)
        recyclerViewMessagesList.layoutManager = LinearLayoutManager(this)

        recyclerViewMessagesList.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
            }
        })

        roomService.getMessages(roomId).enqueue(object: Callback<RoomWithMessages> {
            override fun onResponse(call: Call<RoomWithMessages>, response: Response<RoomWithMessages>) {
                messageList = response.body()!!.messages.reversed().toMutableList()
                recyclerViewMessagesList.adapter = MessageAdapter(this@MessageListActivity, messageList)
                recyclerViewMessagesList.scrollToPosition(response.body()!!.messages.size-1)
            }

            override fun onFailure(call: Call<RoomWithMessages>, t: Throwable) {
                Log.d(MessageListActivity::class.java.toString(), t.toString())
                Toast.makeText(this@MessageListActivity, "Неудалось получить сообщения", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun sendMessage() {
        val message = Message(-1, editText.text.toString(), Date(), roomId, listOf(), UserProvider.user!!)
        val gson = GsonBuilder().create()
        Socket.get().emit("new_message", gson.toJson(message))
    }

    private fun listenNewMessage() {
        val gson = Client.gson
        Socket.get().on("chat") {

            it.forEach {
                messageList.add(gson.fromJson(it.toString(), Message::class.java))
                runOnUiThread {
                    recyclerViewMessagesList.adapter?.notifyItemInserted(messageList.size - 1)
                    recyclerViewMessagesList.scrollToPosition(messageList.size - 1)
                }
            }
        }
    }



    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        getMenuInflater().inflate(R.menu.menu_message, menu);
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if(id == R.id.mapButton) {
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}