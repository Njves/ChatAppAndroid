package com.example.androiduni.select

import android.view.MotionEvent
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.RecyclerView
import com.example.androiduni.message.Message
import com.example.androiduni.room.model.Room

class MessageLookup(private val recyclerView: RecyclerView) : ItemDetailsLookup<Message>() {
    override fun getItemDetails(e: MotionEvent): ItemDetails<Message>? = recyclerView.findChildViewUnder(e.x, e.y)?.let {
            (recyclerView.getChildViewHolder(it) as? ViewHolderWithDetails<*>)?.getItemDetail()
        }

}