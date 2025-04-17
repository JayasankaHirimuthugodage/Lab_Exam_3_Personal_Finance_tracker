package com.example.lab_exam_3_personal_finance_tracker

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.example.lab_exam_3_personal_finance_tracker.data.SharedPrefManager
import com.example.lab_exam_3_personal_finance_tracker.model.Transaction
import com.google.android.material.textfield.TextInputLayout
import java.util.*

class AddIncomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_income)

        // ✅ Setup toolbar with white title
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setTitleTextColor(ContextCompat.getColor(this, android.R.color.white))
        supportActionBar?.title = "Add Income"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val etTitle = findViewById<EditText>(R.id.etIncomeTitle)
        val etAmount = findViewById<EditText>(R.id.etIncomeAmount)
        val etDate = findViewById<EditText>(R.id.etIncomeDate)
        val spinnerCategory = findViewById<Spinner>(R.id.spinnerIncomeCategory)
        val btnSave = findViewById<Button>(R.id.btnSaveIncome)

        val layoutTitle = findViewById<TextInputLayout>(R.id.layoutIncomeTitle)
        val layoutAmount = findViewById<TextInputLayout>(R.id.layoutIncomeAmount)
        val layoutDate = findViewById<TextInputLayout>(R.id.layoutIncomeDate)

        // ✅ Set amount hint with selected currency
        val currency = SharedPrefManager(this).loadCurrency()
        layoutAmount.hint = "Amount ($currency)"

        // Setup spinner with income categories
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.income_category_array,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter

        // Date picker setup
        etDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePicker = DatePickerDialog(this,
                { _, year, month, dayOfMonth ->
                    etDate.setText("%04d-%02d-%02d".format(year, month + 1, dayOfMonth))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        }

        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val amountText = etAmount.text.toString().trim()
            val amount = amountText.toDoubleOrNull()
            val category = spinnerCategory.selectedItem.toString()
            val date = etDate.text.toString().trim()

            // Clear previous errors
            layoutTitle.error = null
            layoutAmount.error = null
            layoutDate.error = null

            // ✅ Validation
            when {
                title.isBlank() || title.length < 3 -> {
                    layoutTitle.error = "Title must be at least 3 characters"
                }
                title.any { it.isDigit() } -> {
                    layoutTitle.error = "Title should not contain numbers"
                }
                amount == null || amount <= 0 -> {
                    layoutAmount.error = "Enter a valid positive amount"
                }
                category.isBlank() || category.equals("Select Category", ignoreCase = true) -> {
                    showToast("Please select a valid category")
                }
                date.isBlank() -> {
                    layoutDate.error = "Please select a date"
                }
                else -> {
                    val transaction = Transaction(
                        id = System.currentTimeMillis(),
                        title = title,
                        amount = amount,
                        category = category,
                        date = date,
                        type = "Income"
                    )
                    SharedPrefManager(this).saveTransaction(transaction)
                    showToast("Income saved successfully")
                    finish()
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
