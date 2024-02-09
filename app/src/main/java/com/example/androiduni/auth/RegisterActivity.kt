package com.example.androiduni.auth

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.androiduni.Client
import com.example.androiduni.R
import com.example.androiduni.auth.model.AccessModel
import com.example.androiduni.auth.model.RegisterModel
import com.example.androiduni.auth.request.AuthService
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {
    private lateinit var loginInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var passwordInputRepeat: TextInputEditText
    private lateinit var buttonRegister: Button
    private lateinit var buttonSwitch: Button
    private lateinit var tvError: TextView
    private lateinit var sharedPreferences: SharedPreferences
    private val authService: AuthService = Client.getClient().create(AuthService::class.java)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        loginInput = findViewById(R.id.inputRegisterLogin)
        passwordInput = findViewById(R.id.inputRegisterPassword)
        buttonRegister = findViewById(R.id.buttonRegisterLogin)
        passwordInputRepeat = findViewById(R.id.inputRegisterPasswordRepeat)
        buttonSwitch = findViewById(R.id.buttonSwitchLogin)
        tvError = findViewById(R.id.tvError)
        sharedPreferences = this.getSharedPreferences("Login", MODE_PRIVATE)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        buttonRegister.setOnClickListener {
            if(passwordInput.text == passwordInputRepeat.text) {
                tvError.text = "Пароли не совпадают"
                return@setOnClickListener
            }
            val model = RegisterModel(loginInput.text.toString(), passwordInput.text.toString())
            authService.register(model).enqueue(object: Callback<AccessModel> {
                override fun onResponse(call: Call<AccessModel>, response: Response<AccessModel>) {
                    Log.d(this@RegisterActivity.toString(), response.toString())
                    tvError.text = ""
                    if(response.code() != 201) {
                        if(response.code() == 403) {
                            tvError.text = "Вы уже вошли в систему"
                        }
                        if(response.code() == 409) {
                            tvError.text = "Пользователь уже существует или слишком длинные значения"
                        }
                        if(response.code() == 400) {
                            tvError.text = "Слишком длинные значения"
                        }
                        return
                    }
                    val editor = sharedPreferences.edit()
                    editor.putString("access_token", response.body()!!.accessToken)
                    editor.apply()
                    Log.d(this@RegisterActivity.toString(), response.body().toString())
                    Toast.makeText(this@RegisterActivity, "Зареган", Toast.LENGTH_SHORT).show()
                }

                override fun onFailure(call: Call<AccessModel>, t: Throwable) {
                    Toast.makeText(this@RegisterActivity, "Неудалось отправить запрос", Toast.LENGTH_SHORT).show()
                    Log.e(this@RegisterActivity.toString(), t.toString())
                }

            })


        }
        buttonSwitch.setOnClickListener {
            finish()
        }
    }
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}