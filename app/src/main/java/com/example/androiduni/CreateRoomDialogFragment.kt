package com.example.androiduni

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.androiduni.room.request.RoomService
import com.example.androiduni.room.view_model.RoomResponseViewModel
import com.google.android.material.textfield.TextInputEditText

class CreateRoomDialogFragment : DialogFragment() {

    private val roomService: RoomService = Client.getClient().create(RoomService::class.java)
    private val roomResponseViewModel: RoomResponseViewModel by activityViewModels()


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let { fragmentActivity ->
            val builder = AlertDialog.Builder(fragmentActivity)
            val inflater = requireActivity().layoutInflater
            val view: View = inflater.inflate(R.layout.dialog_room_create_fragment, null)

            val roomName: TextInputEditText = view.findViewById(R.id.inputRoomName)
            roomName.addTextChangedListener {
                if(it?.count()!! < 4) {
                    roomName.error = "Название комнаты должно быть длинее 4 символов"
                }
            }
            builder.setView(view)
                .setPositiveButton("Создать комнату") { dialog, id ->
                    roomResponseViewModel.setResponse(roomName.text.toString())
                }
                .setNegativeButton("Закрыть") { dialog, id ->
                    getDialog()?.cancel()
                }

            builder.create()
        } ?: throw IllegalStateException("activity cannot be null")
    }
}