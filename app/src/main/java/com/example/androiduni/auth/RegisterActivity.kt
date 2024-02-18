package com.example.androiduni.auth

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
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
    private lateinit var progressBar: ProgressBar
    private lateinit var sharedPreferences: SharedPreferences
    private var validLogin = false
    private var validPassword = false
    private var validRepeatPassword = false
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
        progressBar = findViewById(R.id.progressBar)

        sharedPreferences = this.getSharedPreferences("Login", MODE_PRIVATE)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        loginInput.addTextChangedListener {
            it?.let {
                validLogin = false
                if(it.count() < 4) {
                    loginInput.error = "Длина логина должна быть больше 4 символов"
                }
                else if(it.isEmpty()) {
                    loginInput.error = "Логин обязателен"
                }
                else {
                    validLogin = true
                }
            }
            buttonRegister.isEnabled = validLogin && validPassword && validRepeatPassword
        }
        passwordInput.addTextChangedListener {
            it?.let {
                validPassword = false
                if(it.count() < 6) {
                    passwordInput.error = "Длина пароля должна быть больше 6 символов"
                }
                else if(it.isEmpty()) {
                    passwordInput.error = "Пароль обязателен"
                }
                else {
                    validPassword = true
                }
            }
            buttonRegister.isEnabled = validLogin && validPassword && validRepeatPassword
        }
        passwordInputRepeat.addTextChangedListener {
            it?.let {
                validRepeatPassword = false
                if(it.toString() != passwordInput.text.toString()) {
                    passwordInputRepeat.error = "Пароль не совпадают"
                }
                else {
                    validRepeatPassword = true
                }
            }
            buttonRegister.isEnabled = validLogin && validPassword && validRepeatPassword
        }
        buttonRegister.isEnabled = validLogin && validPassword && validRepeatPassword
        buttonRegister.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            val model = RegisterModel(loginInput.text.toString(), passwordInput.text.toString())
            authService.register(model).enqueue(object: Callback<AccessModel> {
                override fun onResponse(call: Call<AccessModel>, response: Response<AccessModel>) {
                    progressBar.visibility = View.GONE
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
                    val intent = Intent(this@RegisterActivity, RoomListActivity::class.java)
                    startActivity(intent)
                    finish()
                }

                override fun onFailure(call: Call<AccessModel>, t: Throwable) {
                    progressBar.visibility = View.GONE
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