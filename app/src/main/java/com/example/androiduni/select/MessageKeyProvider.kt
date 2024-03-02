package com.example.androiduni.select

import androidx.recyclerview.selection.ItemKeyProvider
import com.example.androiduni.message.Message

class MessageKeyProvider(private val items: List<Message>) : ItemKeyProvider<Message>(SCOPE_CACHED) {
    override fun getKey(position: Int): Message? = items[position]
    override fun getPosition(key: Message): Int = items.indexOf(key)
}