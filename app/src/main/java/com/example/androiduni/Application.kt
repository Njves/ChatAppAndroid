package com.example.androiduni

import android.app.Application
import android.widget.Toast
import com.example.androiduni.socket.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Application: Application() {
    override fun onCreate() {
        super.onCreate()
        UserProvider.loadUser(this@Application)
        try {
            Socket.setConnect(this@Application)
        } catch (exception: Exception) {
            return
        }
        Socket.get().on("disconnect") {
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(this@Application, "Вы отключились от сервера", Toast.LENGTH_LONG)
                    .show()
            }
        }
        Socket.get().on("connect") {
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(this@Application, "Вы подключились к серверу", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }
}


