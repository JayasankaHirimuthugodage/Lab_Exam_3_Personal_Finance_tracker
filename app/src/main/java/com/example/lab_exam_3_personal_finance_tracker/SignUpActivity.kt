package com.example.lab_exam_3_personal_finance_tracker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SignUpActivity : AppCompatActivity() {

    private lateinit var etFullName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnSignUp: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        // Initialize views
        etFullName = findViewById(R.id.etFullName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnSignUp = findViewById(R.id.btnSignUp)

        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

        btnSignUp.setOnClickListener {
            val fullName = etFullName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val savedEmail = sharedPref.getString("email", null)

            // Clear any previous errors
            etFullName.error = null
            etEmail.error = null
            etPassword.error = null

            // Validation checks
            when {
                fullName.isEmpty() || email.isEmpty() || password.isEmpty() -> {
                    Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                }
                !fullName.matches(Regex("^[a-zA-Z ]+$")) -> {
                    etFullName.error = "Name can only contain letters and spaces"
                }
                !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    etEmail.error = "Enter a valid email"
                }
                password.length < 6 -> {
                    etPassword.error = "Password must be at least 6 characters"
                }
                savedEmail != null && savedEmail.equals(email, ignoreCase = true) -> {
                    etEmail.error = "Email already registered"
                }
                else -> {
                    // Save user data
                    sharedPref.edit().apply {
                        putString("fullName", fullName)
                        putString("email", email)
                        putString("password", password)
                        apply()
                    }

                    Toast.makeText(this, "Sign up successful!", Toast.LENGTH_SHORT).show()

                    // Navigate to Sign In
                    val intent = Intent(this, SignInActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                }
            }
        }
    }
}
