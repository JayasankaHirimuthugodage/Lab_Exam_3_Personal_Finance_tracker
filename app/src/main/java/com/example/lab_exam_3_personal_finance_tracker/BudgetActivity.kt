package com.example.lab_exam_3_personal_finance_tracker

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class BudgetActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget)

        // Initialize toolbar with title and back button

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Set Budget"

        val etBudget = findViewById<EditText>(R.id.etMonthlyBudget)
        val btnSave = findViewById<Button>(R.id.btnSaveBudget)

        val prefs = getSharedPreferences("FinancePrefs", MODE_PRIVATE)
        val savedBudget = prefs.getFloat("monthly_budget", 0f)
        if (savedBudget > 0f) {
            etBudget.setText(savedBudget.toString())
        }

        btnSave.setOnClickListener {
            val budgetText = etBudget.text.toString().trim()

            if (budgetText.isEmpty()) {
                Toast.makeText(this, "Budget field cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val budgetAmount = budgetText.toFloatOrNull()
            if (budgetAmount == null || budgetAmount <= 0) {
                Toast.makeText(this, "Please enter a valid positive amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            prefs.edit().putFloat("monthly_budget", budgetAmount).apply()
            Toast.makeText(this, "Monthly budget saved", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
