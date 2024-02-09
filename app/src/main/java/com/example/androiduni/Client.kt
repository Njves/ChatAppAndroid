package com.example.androiduni

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object Client {
    private var retrofit: Retrofit? = null
    val BASE_URL: String
        get() = "https://5.35.88.60:5000"
    val okHttpClient: OkHttpClient = UnsafeHttpClient.getUnsafeOkHttpClient()
    val gson = GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create()
    fun getClient(): Retrofit {
        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(okHttpClient)
                .build()
        }
        return retrofit!!
    }

}