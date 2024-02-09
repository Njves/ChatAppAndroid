package com.example.androiduni.auth

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.androiduni.Client
import com.example.androiduni.R
import com.example.androiduni.RoomListActivity
import com.example.androiduni.UserProvider
import com.example.androiduni.auth.model.AccessModel
import com.example.androiduni.auth.model.LoginModel
import com.example.androiduni.auth.request.AuthService
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    private lateinit var loginInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var buttonLogin: Button
    private lateinit var buttonSwitch: Button
    private lateinit var tvError: TextView
    private lateinit var sharedPreferences: SharedPreferences
    private val authService: AuthService = Client.getClient().create(AuthService::class.java)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = this.getSharedPreferences("Login", MODE_PRIVATE)
        if(sharedPreferences.getString("access_token", null) != null) {
            val intent = Intent(this, RoomListActivity::class.java)
            startActivity(intent)
            finish()
        }
        setContentView(R.layout.activity_login)

        tvError = findViewById(R.id.tvError)
        loginInput = findViewById(R.id.inputLogin)
        passwordInput = findViewById(R.id.inputPassword)
        buttonSwitch = findViewById(R.id.buttonSwitch)
        buttonLogin = findViewById(R.id.buttonLogin)
        buttonLogin.setOnClickListener {
            val model = LoginModel(loginInput.text.toString(), passwordInput.text.toString())
            authService.login(model).enqueue(object: Callback<AccessModel> {
                override fun onResponse(call: Call<AccessModel>, response: Response<AccessModel>) {
                    Log.d(this@MainActivity.toString(), response.toString())
                    tvError.text = ""
                    if(response.code() != 200) {
                        if(response.code() == 404) {
                            tvError.text = "Пользователь не найден"
                        }
                        if(response.code() == 401) {
                            tvError.text = "Неверный пароль"
                        }
                        return
                    }
                    val editor = sharedPreferences.edit()
                    editor.putString("access_token", response.body()!!.accessToken)
                    editor.apply()
                    UserProvider.loadUser(this@MainActivity)
                    Log.d(this@MainActivity.toString(), response.body().toString())
                    Toast.makeText(this@MainActivity, "Залогинен", Toast.LENGTH_SHORT).show()
                }

                override fun onFailure(call: Call<AccessModel>, t: Throwable) {
                    Toast.makeText(this@MainActivity, "Неудалось отправить запрос", Toast.LENGTH_SHORT).show()
                    Log.d(this@MainActivity.toString(), t.toString())
                }

            })
        }
        buttonSwitch.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

    }
}