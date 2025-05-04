package com.example.lab_exam_3_personal_finance_tracker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AlphaAnimation
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val tvAppName = findViewById<TextView>(R.id.tvAppName)
        val tvSubText = findViewById<TextView>(R.id.tvSubText)

        // Ensure text views are fully visible before starting animation

        tvAppName.alpha = 1f
        tvSubText.alpha = 1f

        val fadeIn = AlphaAnimation(0f, 1f).apply {
            duration = 1000
            fillAfter = true
        }

        tvAppName.startAnimation(fadeIn)
        tvSubText.startAnimation(fadeIn)

        // Delay navigation to next screen after splash animation

        Handler(Looper.getMainLooper()).postDelayed({
            val prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val isLoggedIn = prefs.getBoolean("isLoggedIn", false)

            val intent = if (isLoggedIn) {
                Intent(this, HomeActivity::class.java)
            } else {
                Intent(this, Onboarding1Activity::class.java)
            }

            startActivity(intent)
            finish()
        }, 4000)
    }
}
