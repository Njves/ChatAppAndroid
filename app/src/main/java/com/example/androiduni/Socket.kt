package com.example.androiduni

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import okhttp3.OkHttpClient
import java.net.URISyntaxException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager


object Socket {
    private var instance: Socket? = null
    fun setConnect(context: Context) {
        val hostnameVerifier =
            HostnameVerifier { hostname, sslSession -> true }

        val trustManager: X509TrustManager = object : X509TrustManager {
            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return arrayOf<X509Certificate>()
            }

            override fun checkClientTrusted(arg0: Array<X509Certificate?>?, arg1: String?) {
                // not implemented
            }

            override fun checkServerTrusted(arg0: Array<X509Certificate?>?, arg1: String?) {
                // not implemented
            }
        }

        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, arrayOf<TrustManager>(trustManager), null)

        val okHttpClient = OkHttpClient.Builder()
            .hostnameVerifier(hostnameVerifier)
            .sslSocketFactory(sslContext.socketFactory, trustManager)
            .readTimeout(1, TimeUnit.MINUTES) // important for HTTP long-polling
            .build()

        val options = IO.Options()
        options.callFactory = okHttpClient
        options.webSocketFactory = okHttpClient
        val preferences = context.getSharedPreferences("Login", MODE_PRIVATE)
        if(preferences.getString("access_token", "") == "") {
            Log.e("Socket", "Токен пустой")
            return
        }
        options.query = "token=${preferences.getString("access_token", "")}"
        try {
            instance = IO.socket("https://5.35.88.60:5000/", options)
        } catch (e: URISyntaxException) {
            Log.e("Socket", e.toString())
        }
        instance?.connect()
        instance?.on(Socket.EVENT_CONNECT, Emitter.Listener {
            Log.d("Socket", "Socket connected ${instance?.id()}")
        })

        instance?.on(Socket.EVENT_DISCONNECT, Emitter.Listener {
            Log.d("Socket", "Socket disconnected")
        })
    }

    fun get(): Socket {
        return instance!!
    }
}