package com.example.mytaxi

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_driver_sign_in.*

class DriverSignInActivity : AppCompatActivity() {

    private var isLogin = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_sign_in)
        btnLoginSignUp.setOnClickListener {
            loginSignUpUser()
        }
        tvToggleLoginSignUp.setOnClickListener {
            if (isLogin) {
                tvToggleLoginSignUp.text = getString(R.string.press_to_login)
                btnLoginSignUp.text = getString(R.string.sign_up)
                tilName.visibility = View.VISIBLE
                tilPasswordConfirm.visibility = View.VISIBLE
                isLogin = false
            } else {
                tvToggleLoginSignUp.text = getString(R.string.press_to_sign_up)
                btnLoginSignUp.text = getString(R.string.login)
                tilName.visibility = View.GONE
                tilPasswordConfirm.visibility = View.GONE
                isLogin = true
            }
        }
    }

    private fun loginSignUpUser() {
        if (!isLogin && validateName() && validateEmail() && validatePassword() && validatePasswordConfirm()) {
            Toast.makeText(this, "Регистрация успешна", Toast.LENGTH_SHORT).show()
        } else if (isLogin && validateEmail() && validatePassword()) {
            Toast.makeText(this, "Вход успешен", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateName(): Boolean {
        val inputName = tilName.editText?.text.toString().trim()
        return when {
            inputName.isEmpty() -> {
                tilName.error = "Введите имя"
                false
            }
            inputName.length > 15 -> {
                tilName.error = "Имя должно быть меньше 15 символов"
                false
            }
            else -> {
                tilName.error = ""
                true
            }
        }
    }

    private fun validateEmail(): Boolean {
        val inputMail = tilEmail.editText?.text.toString().trim()
        return when {
            inputMail.isEmpty() -> {
                tilEmail.error = "Введите E-mail"
                false
            }
            else -> {
                tilEmail.error = ""
                true
            }
        }
    }

    private fun validatePassword(): Boolean {
        val inputPassword = tilPassword.editText?.text.toString().trim()
        return when {
            inputPassword.isEmpty() -> {
                tilPassword.error = "Введите пароль"
                false
            }
            inputPassword.length < 7 -> {
                tilPassword.error = "Пароль должен быть больше 6 символов"
                false
            }
            else -> {
                tilPassword.error = ""
                true
            }
        }
    }

    private fun validatePasswordConfirm(): Boolean {
        val inputPassword = tilPassword.editText?.text.toString().trim()
        val inputPasswordConfirm = tilPasswordConfirm.editText?.text.toString().trim()
        return if (inputPassword != inputPasswordConfirm) {
            tilPasswordConfirm.error = "Пароли должны совпадать"
            false
        } else {
            tilPasswordConfirm.error = ""
            true
        }
    }
}
