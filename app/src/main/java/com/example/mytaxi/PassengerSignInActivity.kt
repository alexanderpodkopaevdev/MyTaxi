package com.example.mytaxi

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

import kotlinx.android.synthetic.main.activity_passenger_sign_in.*

class PassengerSignInActivity : AppCompatActivity() {

    private var isLogin = false

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var usersDB: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_passenger_sign_in)
        FirebaseAuth.getInstance().signOut()
        auth = FirebaseAuth.getInstance()
        updateUI(auth.currentUser)
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

    private fun updateUI(currentUser: FirebaseUser?) {
        if (currentUser != null) {
            startActivity(Intent(this, PassengerMap::class.java))
        }
    }

    private fun loginSignUpUser() {
        if (!isLogin && validateName() && validateEmail() && validatePassword() && validatePasswordConfirm()) {
            createAccount(
                tilName.editText?.text.toString().trim(),
                tilEmail.editText?.text.toString().trim(),
                tilPassword.editText?.text.toString().trim()
            )
            Toast.makeText(this, "Регистрация успешна", Toast.LENGTH_SHORT).show()
        } else if (isLogin && validateEmail() && validatePassword()) {
            loginAccount(
                tilEmail.editText?.text.toString().trim(),
                tilPassword.editText?.text.toString().trim()
            )
            Toast.makeText(this, "Вход успешен", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loginAccount(email: String, password: String) {
        auth.signInWithEmailAndPassword(email,password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                    //updateUI(null)
                }
            }
    }

    private fun createAccount(name: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    updateProfile(user)
                    createUser(user)
                    updateUI(user)
                } else {
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun createUser(FirebaseUser: FirebaseUser?) {
        if (FirebaseUser != null) {
            val user = Passenger(
                FirebaseUser.displayName ?: tilName.editText?.text.toString().trim(),
                FirebaseUser.email,
                FirebaseUser.uid
            )
            database = FirebaseDatabase.getInstance()
            usersDB = database.getReference("passengers")
            usersDB.push().setValue(user)
        }
    }

    private fun updateProfile(user: FirebaseUser?) {
        if (user != null) {
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(tilName.editText?.text.toString().trim())
                .build()
            user.updateProfile(profileUpdates)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "User profile updated.")
                    }
                }
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

    companion object {
        private const val TAG = "EmailPassword"
    }
}
