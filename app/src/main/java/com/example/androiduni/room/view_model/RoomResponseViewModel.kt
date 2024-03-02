package com.example.androiduni.room.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RoomResponseViewModel : ViewModel() {
    private val mutableResponse = MutableLiveData<String>()
    val response: LiveData<String> get() = mutableResponse

    fun setResponse(item: String) {
        mutableResponse.value = item
    }
}