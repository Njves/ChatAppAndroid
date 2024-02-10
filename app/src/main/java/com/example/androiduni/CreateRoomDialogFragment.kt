package com.example.androiduni

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.androiduni.room.model.Room
import com.example.androiduni.room.request.RoomService
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.create
import java.lang.IllegalStateException

class CreateRoomDialogFragment : DialogFragment() {
    private val roomService: RoomService = Client.getClient().create(RoomService::class.java)
    private val gson: Gson = Gson()
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater;
            val view: View = inflater.inflate(R.layout.dialog_room_create_fragment, null)
            val roomName: TextInputEditText = view.findViewById(R.id.inputRoomName)
            builder.setView(view)
                // Add action buttons.
                .setPositiveButton("Создать комнату") { dialog, id ->
                    roomService.createRoom(
                        Room(
                            -1,
                            roomName.text.toString(),
                            UserProvider.user!!.id,
                            null
                        ), UserProvider.token!!
                    ).enqueue(object : Callback<Room> {
                        override fun onResponse(call: Call<Room>, response: Response<Room>) {
                            if(!response.isSuccessful) {
                                Log.d("CreateRoomDialogFragment", response.errorBody()!!.string())
                                Toast.makeText(context, response.errorBody()!!.string(), Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onFailure(call: Call<Room>, t: Throwable) {
                            Log.d(this@CreateRoomDialogFragment.toString(), t.toString())
                        }

                    })
                }
                .setNegativeButton("Закрыть") { dialog, id ->
                    getDialog()?.cancel()
                }
            builder.create()
        } ?: throw IllegalStateException("activity cannot be null")
    }

}