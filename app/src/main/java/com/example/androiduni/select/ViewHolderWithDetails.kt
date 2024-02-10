package com.example.androiduni.select

import androidx.recyclerview.selection.ItemDetailsLookup.ItemDetails
import com.example.androiduni.message.Message

interface ViewHolderWithDetails<TItem> {
    fun getItemDetail(): ItemDetails<Message>
}