package com.example.androiduni

import android.content.Context
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso

object PicassoNotSafe {
    fun get(context: Context): Picasso {
         return Picasso.Builder(context).downloader(OkHttp3Downloader(Client.okHttpClient)).build()
    }
}