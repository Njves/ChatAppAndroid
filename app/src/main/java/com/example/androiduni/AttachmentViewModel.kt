package com.example.androiduni

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.androiduni.message.Attachment
import androidx.lifecycle.LiveData

class AttachmentViewModel : ViewModel() {
    // Список вложений
    private val _attachments = MutableLiveData<List<Attachment>>()
    val attachments: LiveData<List<Attachment>>
        get() = _attachments

    // Метод для установки списка вложений
    fun setAttachments(attachments: List<Attachment>) {
        _attachments.value = attachments
    }

    // Метод для добавления вложения
    fun addAttachment(attachment: Attachment) {
        val currentList = _attachments.value.orEmpty().toMutableList()
        currentList.add(attachment)
        _attachments.value = currentList
    }

    fun clearAttachment() {
        val currentList = _attachments.value.orEmpty().toMutableList()
        currentList.clear()
        _attachments.value = currentList
    }

    // Метод для удаления вложения
    fun removeAttachment(attachment: Attachment) {
        val currentList = _attachments.value.orEmpty().toMutableList()
        currentList.remove(attachment)
        _attachments.value = currentList
    }
}