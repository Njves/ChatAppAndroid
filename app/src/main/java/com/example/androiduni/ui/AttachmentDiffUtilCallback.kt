package com.example.androiduni.ui

import androidx.recyclerview.widget.DiffUtil
import com.example.androiduni.message.Attachment

class AttachmentDiffUtilCallback(private val oldList: List<Attachment>, private val newList: List<Attachment>) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList.size


    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem: Attachment = oldList[oldItemPosition]
        val newItem: Attachment = newList[newItemPosition]
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem: Attachment = oldList[oldItemPosition]
        val newItem: Attachment = newList[newItemPosition]
        return oldItem == newItem
    }

}