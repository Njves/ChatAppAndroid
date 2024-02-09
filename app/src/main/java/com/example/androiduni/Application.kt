package com.example.androiduni

import android.app.Application
import android.util.Log

class Application: Application() {
    override fun onCreate() {
        super.onCreate()
        UserProvider.loadUser(this)
        Socket.setConnect(this)
    }


}