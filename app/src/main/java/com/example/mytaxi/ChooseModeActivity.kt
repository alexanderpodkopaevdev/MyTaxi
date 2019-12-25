package com.example.mytaxi

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_choose_mode.*

class ChooseModeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_mode)
        btnPassenger.setOnClickListener { startActivity(Intent(this,PassengerSignInActivity::class.java)) }
        btnDriver.setOnClickListener { startActivity(Intent(this,DriverSignInActivity::class.java)) }
    }
}
