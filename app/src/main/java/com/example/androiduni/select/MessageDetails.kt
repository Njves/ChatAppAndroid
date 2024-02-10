package com.example.androiduni.select

import androidx.recyclerview.selection.ItemDetailsLookup
import com.example.androiduni.message.Message

class MessageDetails(private val adapterPosition: Int, private val selectedKey: Message?) : ItemDetailsLookup.ItemDetails<Message>() {

    override fun getSelectionKey() = selectedKey

    override fun getPosition() = adapterPosition

}