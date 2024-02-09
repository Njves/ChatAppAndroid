package com.example.androiduni.auth.request

import com.example.androiduni.auth.model.AccessModel
import com.example.androiduni.auth.model.LoginModel
import com.example.androiduni.auth.model.RegisterModel
import com.example.androiduni.user.User
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface AuthService {
    @POST("/login")
    fun login(@Body loginModel: LoginModel): Call<AccessModel>

    @POST("/register")
    fun register(@Body registerModel: RegisterModel): Call<AccessModel>

    @GET("/user")
    fun getUser(@Query("token") token: String) : Call<User>
}