package com.example.androiduni

import android.content.Context
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso

class PicassoNotSafe(context: Context) {
    private val picasso: Picasso = Picasso.Builder(context).downloader(OkHttp3Downloader(Client.okHttpClient)).build()
    fun get(): Picasso {
        picasso.setIndicatorsEnabled(true)
        return picasso
    }
}