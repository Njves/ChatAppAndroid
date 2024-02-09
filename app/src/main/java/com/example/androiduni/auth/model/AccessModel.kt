package com.example.androiduni.auth.model

import com.google.gson.annotations.SerializedName

data class AccessModel(@SerializedName("token") val accessToken: String)