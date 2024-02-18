package com.example.androiduni.auth

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
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
    private lateinit var progressBar: ProgressBar
    private lateinit var sharedPreferences: SharedPreferences
    private val authService: AuthService = Client.getClient().create(AuthService::class.java)
    private var validLogin = false
    private var validPassword = false
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
        progressBar = findViewById(R.id.progressBar)

        passwordInput.addTextChangedListener {
            it?.let {
                validPassword = false
                if(it.count() < 6) {
                    passwordInput.error = "Пароль должен быть длинее 6 символов"
                }
                else if(it.isEmpty()) {
                    passwordInput.error = "Пароль обязателен"
                }
                else {
                    validPassword = true
                }
            }
            buttonLogin.isEnabled = validLogin && validPassword
        }

        loginInput.addTextChangedListener {
            it?.let {
                validLogin = false
                if(it.count() < 4) {
                    loginInput.error = "Логин должен быть длинее 4 символов"
                }
                else if(it.isEmpty()) {
                    loginInput.error = "Логин обязателен"
                }
                else {
                    validLogin = true
                }
            }

            buttonLogin.isEnabled = validLogin && validPassword
        }
        buttonLogin.isEnabled = false
        buttonLogin.setOnClickListener {
            val model = LoginModel(loginInput.text.toString(), passwordInput.text.toString())
            progressBar.visibility = View.VISIBLE
            authService.login(model).enqueue(object: Callback<AccessModel> {
                override fun onResponse(call: Call<AccessModel>, response: Response<AccessModel>) {
                    progressBar.visibility = View.GONE
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
                    val intent = Intent(this@MainActivity, RoomListActivity::class.java)
                    startActivity(intent)
                    finish()
                }

                override fun onFailure(call: Call<AccessModel>, t: Throwable) {
                    progressBar.visibility = View.GONE
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