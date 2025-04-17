package com.example.lab_exam_3_personal_finance_tracker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class Onboarding1Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding1)

        val btnNext = findViewById<Button>(R.id.btnNextOnboarding)
        btnNext.setOnClickListener {
            startActivity(Intent(this, Onboarding2Activity::class.java))
            finish()
        }
    }
}
