package com.example.androiduni

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.util.Log
import com.example.androiduni.auth.request.AuthService
import com.example.androiduni.user.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object UserProvider {
    var user: User? = null
    var token: String? = null
    fun loadUser(context: Context) {
        if(user == null) {
            val sharedPreferences: SharedPreferences =
                context.getSharedPreferences("Login", MODE_PRIVATE)

            val token: String = sharedPreferences.getString("access_token", null) ?: return
            this.token = token
            Client.getClient().create(AuthService::class.java).getUser(token).enqueue(object: Callback<User>{
                override fun onResponse(call: Call<User>, response: Response<User>) {
                    this@UserProvider.user = response.body()!!
                    Log.d(this@UserProvider.toString(), this@UserProvider.user.toString())
                }

                override fun onFailure(call: Call<User>, t: Throwable) {
                    Log.d(this@UserProvider.toString(), t.toString())
                }

            })
            Log.d("UserProvider", user.toString())

        }
    }
}