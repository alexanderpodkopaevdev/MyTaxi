package com.example.mytaxi

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.ProgressBar
import kotlinx.android.synthetic.main.activity_splash_screen.*

class SplashScreenActivity : AppCompatActivity() {
    companion object {
        const val SPLASH_DELAY = 1000L
        const val SPLASH_DELAY_PROGRESS_BAR = 500L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        Handler().postDelayed({
            pbSlash.visibility = ProgressBar.VISIBLE
        },SPLASH_DELAY_PROGRESS_BAR)

        Handler().postDelayed({
            pbSlash.visibility = ProgressBar.GONE
            startActivity(Intent(this,ChooseModeActivity::class.java))
            finish()
        },SPLASH_DELAY)
    }
}
