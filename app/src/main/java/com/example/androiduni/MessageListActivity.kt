package com.example.androiduni

import android.Manifest
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.Manifest.permission.READ_MEDIA_VIDEO
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View.GONE
import android.view.inputmethod.InputMethodManager
import android.widget.AbsListView.OnScrollListener.SCROLL_STATE_FLING
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toFile
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.androiduni.message.Attachment
import com.example.androiduni.message.Message
import com.example.androiduni.message.adapter.AttachmentAdapter
import com.example.androiduni.message.request.MessageRequest
import com.example.androiduni.room.model.RoomWithMessages
import com.example.androiduni.room.request.RoomService
import com.example.androiduni.select.ActionModeController
import com.example.androiduni.select.MessageKeyProvider
import com.example.androiduni.select.MessageLookup
import com.example.androiduni.ui.MessageAdapter
import com.google.gson.GsonBuilder
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.ArrayList
import java.util.Date


class MessageListActivity : AppCompatActivity() {
    private lateinit var recyclerViewMessagesList: RecyclerView
    private lateinit var buttonSend: ImageButton
    private lateinit var buttonAttach: ImageButton
    private lateinit var editText: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerViewAttachment: RecyclerView
    private var roomId: Int = -1
    private var roomName: String = ""
    private var actionMode: ActionMode? = null
    private var messageList: MutableList<Message> = mutableListOf()
    private var messageService: MessageRequest = Client.getClient().create(MessageRequest::class.java)
    private val roomService: RoomService = Client.getClient().create(RoomService::class.java)
    private lateinit var tracker: SelectionTracker<Message>
    private lateinit var  adapter: MessageAdapter
    private val attachmentList: MutableList<Attachment> = mutableListOf()
    private lateinit var attachmentAdapter: AttachmentAdapter
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1
    private var count = 10
    private var offset = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_list)
        intent.extras.let {
            if (it != null) {
                roomId = it.getInt("roomId", 0)
                roomName = it.getString("roomName", "")
            }
        }
        if(roomId == -1) {
            finish()
        }
        supportActionBar?.title = roomName
        buttonSend = findViewById(R.id.btnSend)
        buttonAttach = findViewById(R.id.btnAttach)
        editText = findViewById(R.id.inputMessage)
        progressBar = findViewById(R.id.progressBar)

        recyclerViewAttachment = findViewById(R.id.rvAttachments)
        recyclerViewAttachment.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        attachmentAdapter = AttachmentAdapter(this, attachmentList)
        val testData = listOf(Attachment(id=0, type="multipart/form-data; charset=utf-8", link="/content/file_$50b3d825-8d04-47ee-94a1-591b05c8d99a.png", messageId=0),
            Attachment(id=0, type="multipart/form-data; charset=utf-8", link="/content/file_$77512c12-3ba3-4900-bb9b-0659b7a6084f.png", messageId=0))
        recyclerViewAttachment.adapter = attachmentAdapter

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
                if(newState == SCROLL_STATE_FLING) {
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
                }
            }
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy < 0) {
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                    if (firstVisibleItemPosition == 0 && layoutManager.findViewByPosition(0)?.top == 0) {
                        Log.d(this@MessageListActivity.toString(), "OnTop")
                        offset += count
                        roomService.getMessages(roomId, count, offset).enqueue(object: Callback<RoomWithMessages> {
                            override fun onResponse(call: Call<RoomWithMessages>, response: Response<RoomWithMessages>) {
                                if(response.body()!!.messages.isEmpty())
                                    return
                                messageList = (response.body()!!.messages.reversed() + messageList).toMutableList()
                                Log.d(this@MessageListActivity.toString(), messageList.toString())
                                adapter.setData(messageList)
                                adapter.notifyItemRangeInserted(0, count)
                            }

                            override fun onFailure(call: Call<RoomWithMessages>, t: Throwable) {
                                Log.e(this@MessageListActivity.toString(), t.toString())
                                Toast.makeText(this@MessageListActivity, "Неудалось загрузить сообщение", Toast.LENGTH_SHORT).show()
                            }

                        })
                    }
                }
            }
        })

        roomService.getMessages(roomId, count, offset).enqueue(object: Callback<RoomWithMessages> {
            override fun onResponse(call: Call<RoomWithMessages>, response: Response<RoomWithMessages>) {
                progressBar.visibility = GONE
                messageList = response.body()!!.messages.reversed().toMutableList()
                adapter = MessageAdapter(this@MessageListActivity, messageList)
                recyclerViewMessagesList.adapter = adapter

                tracker = SelectionTracker.Builder(
                    "someId",
                    recyclerViewMessagesList,
                    MessageKeyProvider(messageList),
                    MessageLookup(recyclerViewMessagesList),
                    StorageStrategy.createParcelableStorage(Message::class.java)
                ).build()
                adapter.tracker = tracker
                tracker.addObserver(object : SelectionTracker.SelectionObserver<Message>() {
                    override fun onSelectionChanged() {
                        super.onSelectionChanged()
                        if (tracker.hasSelection() && actionMode == null) {
                            actionMode = startSupportActionMode(ActionModeController(tracker,
                                this@MessageListActivity, messageList, adapter))
                            setSelectedTitle(tracker.selection.size())
                        } else if (!tracker.hasSelection()) {
                            actionMode?.finish()
                            actionMode = null
                        } else {
                            setSelectedTitle(tracker.selection.size())
                        }
                    }
                })
                recyclerViewMessagesList.scrollToPosition(response.body()!!.messages.size-1)
            }

            override fun onFailure(call: Call<RoomWithMessages>, t: Throwable) {
                Log.d(MessageListActivity::class.java.toString(), t.toString())
                Toast.makeText(this@MessageListActivity, "Неудалось получить сообщения", Toast.LENGTH_SHORT).show()
            }
        })

        requestPermissionsIfNecessary(listOf(READ_MEDIA_IMAGES))

        val getContent = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) {
            it?.let { uri ->
                val cursor = contentResolver.query(uri, arrayOf(MediaStore.Images.Media.DATA), null, null, null)
                val columnIndex = cursor?.getColumnIndex(MediaStore.Images.Media.DATA)
                cursor?.moveToFirst()
                val path = cursor?.getString(columnIndex!!)!!
                cursor?.close()
                val file = File(path)
                if(file == null) {
                    Toast.makeText(this@MessageListActivity, "Null", Toast.LENGTH_SHORT).show()
                    return@registerForActivityResult
                }
                val requestFile = RequestBody.create(MultipartBody.FORM, file)
                val body = MultipartBody.Part.createFormData("file", "file", requestFile)
                messageService.uploadImage(UserProvider.token!!, body).enqueue(object: Callback<List<Attachment>> {
                    override fun onResponse(call: Call<List<Attachment>>, response: Response<List<Attachment>>) {
                        if(!response.isSuccessful) {
                            Toast.makeText(this@MessageListActivity,
                                "Неудалось загрузить изображение", Toast.LENGTH_SHORT).show()
                            Log.d(this@MessageListActivity.toString(), response.toString())
                        }
                        response.body()?.let {
                            Toast.makeText(this@MessageListActivity,
                                "Сообщение загружено", Toast.LENGTH_SHORT).show()
                            Log.d(this@MessageListActivity.toString(), response.body()!!.toString())
                            attachmentAdapter.addAttachments(it)
                        }

                    }

                    override fun onFailure(call: Call<List<Attachment>>, t: Throwable) {
                        Log.d(this@MessageListActivity.toString(), t.toString())
                    }
                })
            }
        }

        buttonAttach.setOnClickListener {
            getContent.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }


    }
    private fun requestPermissionsIfNecessary(permissions: List<String>) {
        val permissionsToRequest = mutableListOf<String>()
        permissions.forEach { permission ->
            if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                permissionsToRequest.add(permission);
            }
        }
        if (permissionsToRequest.size > 0) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val permissionsToRequest = ArrayList<String>()
        var i = 0
        while (i < grantResults.size) {
            permissionsToRequest.add(permissions[i])
            i++
        }
        if (permissionsToRequest.size > 0) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                REQUEST_PERMISSIONS_REQUEST_CODE)
        }
    }
    private fun setSelectedTitle(selected: Int) {
        actionMode?.title = "Selected: $selected"
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