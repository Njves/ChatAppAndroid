package com.example.androiduni

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.inputmethod.InputMethodManager
import android.widget.AbsListView.OnScrollListener.SCROLL_STATE_FLING
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.androiduni.message.Attachment
import com.example.androiduni.message.Message
import com.example.androiduni.message.adapter.AttachmentAdapter
import com.example.androiduni.message.request.MessageRequest
import com.example.androiduni.room.model.RoomWithMessages
import com.example.androiduni.room.request.RoomService
import com.example.androiduni.room.view_model.RoomResponseViewModel
import com.example.androiduni.select.ActionModeController
import com.example.androiduni.select.MessageKeyProvider
import com.example.androiduni.select.MessageLookup
import com.example.androiduni.socket.Socket
import com.example.androiduni.ui.AttachmentDiffUtilCallback
import com.example.androiduni.ui.MessageAdapter
import com.google.android.material.snackbar.Snackbar
import com.google.gson.GsonBuilder
import com.squareup.picasso.Picasso
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.ArrayList
import java.util.Date


class MessageListActivity : AppCompatActivity() {
    private lateinit var root: ConstraintLayout
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
    private var attachmentAdapter: AttachmentAdapter? = null
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1
    private var count = 10
    private var offset = 0
    private val attachmentViewModel: AttachmentViewModel by viewModels()


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
        root = findViewById(R.id.root)
        buttonSend = findViewById(R.id.btnSend)
        buttonAttach = findViewById(R.id.btnAttach)
        editText = findViewById(R.id.inputMessage)
        progressBar = findViewById(R.id.progressBar)

        recyclerViewAttachment = findViewById(R.id.rvAttachments)
        recyclerViewAttachment.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        attachmentViewModel.attachments.observe(this) {
            if(it.isNotEmpty() && recyclerViewAttachment.visibility == GONE) {
                recyclerViewAttachment.visibility = View.VISIBLE
                attachmentAdapter = AttachmentAdapter(this, attachmentViewModel.attachments.value?.toMutableList() ?: mutableListOf(), attachmentViewModel)
                recyclerViewAttachment.adapter = attachmentAdapter
            }
            if(it.isEmpty() && recyclerViewAttachment.visibility == View.VISIBLE) {
                recyclerViewAttachment.visibility = GONE
            }
            if(attachmentAdapter == null) {
                return@observe
            }
            val callback = AttachmentDiffUtilCallback(attachmentAdapter!!.attachments, it)
            val diffResult = DiffUtil.calculateDiff(callback)
            attachmentAdapter?.attachments = it.toMutableList()
            diffResult.dispatchUpdatesTo(attachmentAdapter!!)
        }

        buttonSend.setOnClickListener {
            sendMessage()

        }
        listenNewMessage()
        requestPermissionsIfNecessary(listOf(READ_EXTERNAL_STORAGE))
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
                                messageList.addAll(0, response.body()!!.messages.reversed())
                                Log.d(this@MessageListActivity.toString(), messageList.toString())
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
                if(response.body()?.messages == null) {
                    Snackbar.make(this@MessageListActivity.root, "Не удается получить сообщения из комнаты", Snackbar.LENGTH_SHORT).show()
                }
                messageList = response.body()!!.messages.reversed().toMutableList()
                adapter = MessageAdapter(this@MessageListActivity, messageList)
                recyclerViewMessagesList.adapter = adapter

                tracker = SelectionTracker.Builder(
                    "someId",
                    recyclerViewMessagesList,
                    MessageKeyProvider(messageList),
                    MessageLookup(recyclerViewMessagesList),
                    StorageStrategy.createParcelableStorage(Message::class.java)
                ).withSelectionPredicate(object: SelectionTracker.SelectionPredicate<Message>() {
                    override fun canSetStateForKey(key: Message, nextState: Boolean): Boolean {
                        return key.user.id == UserProvider.user?.id
                    }

                    override fun canSetStateAtPosition(position: Int, nextState: Boolean): Boolean {
                        return true
                    }

                    override fun canSelectMultiple(): Boolean {
                        return true
                    }

                }).build()
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


        val getContent = registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(5)) {
            it?.let { uris ->
                uris.forEach { uri ->
                    val file = File(getRealPathFromURI(uri, this))
                    if (file == null) {
                        Toast.makeText(this@MessageListActivity, "Null", Toast.LENGTH_SHORT).show()
                        return@registerForActivityResult
                    }
                    val requestFile = RequestBody.create(MultipartBody.FORM, file)
                    val body = MultipartBody.Part.createFormData("file", "file", requestFile)
                    messageService.uploadImage(UserProvider.token!!, body)
                        .enqueue(object : Callback<List<Attachment>> {
                            override fun onResponse(
                                call: Call<List<Attachment>>,
                                response: Response<List<Attachment>>
                            ) {
                                if (!response.isSuccessful) {
                                    Toast.makeText(
                                        this@MessageListActivity,
                                        "Неудалось загрузить изображение", Toast.LENGTH_SHORT
                                    ).show()
                                    Log.d(this@MessageListActivity.toString(), response.toString())
                                }
                                response.body()?.let {
                                    Toast.makeText(
                                        this@MessageListActivity,
                                        "Сообщение загружено", Toast.LENGTH_SHORT
                                    ).show()
                                    Log.d(
                                        this@MessageListActivity.toString(),
                                        response.body()!!.toString()
                                    )
                                    response.body()?.let { attach ->
                                        attach.forEach { item ->
                                            attachmentViewModel.addAttachment(item)
                                        }
                                    }
                                }

                            }

                            override fun onFailure(call: Call<List<Attachment>>, t: Throwable) {
                                Log.d(this@MessageListActivity.toString(), t.toString())
                            }
                        })
                }
            }
        }

        buttonAttach.setOnClickListener {
            getContent.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }


    }

    private fun getRealPathFromURI(uri: Uri, context: Context): String? {
        val returnCursor = context.contentResolver.query(uri, null, null, null, null)
        val nameIndex =  returnCursor!!.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        val sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE)
        returnCursor.moveToFirst()
        val name = returnCursor.getString(nameIndex)
        val size = returnCursor.getLong(sizeIndex).toString()
        val file = File(context.filesDir, name)
        try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(file)
            var read = 0
            val maxBufferSize = 1 * 1024 * 1024
            val bytesAvailable: Int = inputStream?.available() ?: 0
            val bufferSize = Math.min(bytesAvailable, maxBufferSize)
            val buffers = ByteArray(bufferSize)
            while (inputStream?.read(buffers).also {
                    if (it != null) {
                        read = it
                    }
                } != -1) {
                outputStream.write(buffers, 0, read)
            }
            Log.e("File Size", "Size " + file.length())
            inputStream?.close()
            outputStream.close()
            Log.e("File Path", "Path " + file.path)

        } catch (e: java.lang.Exception) {
            Log.e("Exception", e.message!!)
        }
        return file.path
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
        val attachmentsList = attachmentViewModel.attachments.value
        val message = Message(
                -1, editText.text.toString(), Date(), roomId,
                attachmentsList ?: listOf(), UserProvider.user!!
            )

        val gson = GsonBuilder().create()
        Socket.get().emit("new_message", gson.toJson(message))
        attachmentViewModel.clearAttachment()
        editText.text.clear()
    }

    private fun listenNewMessage() {
        val gson = Client.gson
        Socket.get().on("chat") { it ->
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
        menuInflater.inflate(R.menu.menu_message, menu);
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if(id == R.id.mapButton) {
            val intent = Intent(this, MapActivity::class.java)
            intent.putExtra("roomId", roomId)
            intent.putExtra("roomName", roomName)
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        Picasso.get().cancelTag(this)
    }
}