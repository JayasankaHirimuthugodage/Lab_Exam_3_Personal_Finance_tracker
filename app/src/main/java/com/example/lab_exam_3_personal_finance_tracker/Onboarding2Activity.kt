package com.example.lab_exam_3_personal_finance_tracker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class Onboarding2Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding2)

        val btnNext = findViewById<Button>(R.id.btnNextToOnboarding3)
        btnNext.setOnClickListener {
            startActivity(Intent(this, Onboarding3Activity::class.java))
            finish()
        }
    }
}
