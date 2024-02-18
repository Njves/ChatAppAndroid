package com.example.androiduni.select

import android.content.Context
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.view.ActionMode
import androidx.recyclerview.selection.SelectionTracker
import com.example.androiduni.Client
import com.example.androiduni.R
import com.example.androiduni.UserProvider
import com.example.androiduni.message.Message
import com.example.androiduni.message.request.MessageRequest
import com.example.androiduni.ui.MessageAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ActionModeController(
    private val tracker: SelectionTracker<Message>,
    private val context: Context,
    private val data: MutableList<Message>,
    private val adapter: MessageAdapter
) : ActionMode.Callback {

    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        mode.menuInflater.inflate(R.menu.action_menu, menu)
        return true
    }

    override fun onDestroyActionMode(mode: ActionMode) {
        tracker.clearSelection()
    }

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean = true

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean = when (item.itemId) {
        R.id.menu_clear -> {
            tracker.selection.forEach {
                Client.getClient().create(MessageRequest::class.java)
                    .removeMessage(it.id, UserProvider.token!!).enqueue(object :
                    Callback<Response<Void>> {
                    override fun onResponse(
                        call: Call<Response<Void>>,
                        response: Response<Response<Void>>
                    ) {
                        if (response.isSuccessful) {
                            val index = data.indexOf(it)
                            data.removeAt(index)
                            adapter.notifyItemRemoved(index)
                            Toast.makeText(context, "Удалено", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Чота не получилось", Toast.LENGTH_SHORT).show()
                        }
                        Log.d("ActionModeController", response.toString())
                    }

                    override fun onFailure(call: Call<Response<Void>>, t: Throwable) {
                        Log.e("ActionModeController", t.toString())
                    }
                })
            }
            mode.finish()
            true
        }
        else -> false
    }

}