package com.example.lab_exam_3_personal_finance_tracker

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.lab_exam_3_personal_finance_tracker.data.SharedPrefManager
import com.example.lab_exam_3_personal_finance_tracker.model.Transaction
import com.google.android.material.textfield.TextInputLayout
import java.util.*

class EditTransactionActivity : AppCompatActivity() {

    private lateinit var transaction: Transaction

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_transaction)

        // Setup Toolbar with back arrow and custom title
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        val etTitle = findViewById<EditText>(R.id.etEditTitle)
        val etAmount = findViewById<EditText>(R.id.etEditAmount)
        val spinnerCategory = findViewById<Spinner>(R.id.spinnerEditCategory)
        val etDate = findViewById<EditText>(R.id.etEditDate)
        val btnUpdate = findViewById<Button>(R.id.btnUpdateTransaction)
        val tvTypeLabel = findViewById<TextView>(R.id.tvEditTypeLabel)
        val ivTypeImage = findViewById<ImageView>(R.id.ivTypeImage)

        val layoutTitle = findViewById<TextInputLayout>(R.id.layoutEditTitle)
        val layoutAmount = findViewById<TextInputLayout>(R.id.layoutEditAmount)
        val layoutDate = findViewById<TextInputLayout>(R.id.layoutEditDate)

        val currency = SharedPrefManager(this).loadCurrency()
        layoutAmount.hint = "Amount ($currency)"

        transaction = intent.getSerializableExtra("transaction") as Transaction

        if (transaction.type == "Income") {
            toolbar.title = "Edit Income"
            tvTypeLabel.text = "Editing Income"
            tvTypeLabel.setTextColor(resources.getColor(android.R.color.holo_green_dark))
            ivTypeImage.setImageResource(R.drawable.income)
        } else {
            toolbar.title = "Edit Expense"
            tvTypeLabel.text = "Editing Expense"
            tvTypeLabel.setTextColor(resources.getColor(android.R.color.holo_red_dark))
            ivTypeImage.setImageResource(R.drawable.expense)
        }

        val categoryArrayRes = if (transaction.type == "Income") {
            R.array.income_category_array
        } else {
            R.array.category_array
        }

        val adapter = ArrayAdapter.createFromResource(
            this,
            categoryArrayRes,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter

        etTitle.setText(transaction.title)
        etAmount.setText(transaction.amount.toString())
        etDate.setText(transaction.date)

        val categoryIndex = adapter.getPosition(transaction.category)
        spinnerCategory.setSelection(if (categoryIndex >= 0) categoryIndex else 0)

        etDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePicker = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    etDate.setText("%04d-%02d-%02d".format(year, month + 1, dayOfMonth))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        }

        btnUpdate.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val amountText = etAmount.text.toString().trim()
            val amount = amountText.toDoubleOrNull()
            val category = spinnerCategory.selectedItem.toString().trim()
            val date = etDate.text.toString().trim()

            layoutTitle.error = null
            layoutAmount.error = null
            layoutDate.error = null

            when {
                title.isBlank() || title.length < 3 -> layoutTitle.error = "Title must be at least 3 characters"
                title.any { it.isDigit() } -> layoutTitle.error = "Title should not contain numbers"
                amount == null || amount <= 0 -> layoutAmount.error = "Enter a valid positive amount"
                category.isBlank() || category.equals("Select Category", ignoreCase = true) -> showToast("Please select a valid category")
                date.isBlank() -> layoutDate.error = "Please select a date"
                else -> {
                    val updatedTransaction = transaction.copy(
                        title = title,
                        amount = amount,
                        category = category,
                        date = date,
                        type = transaction.type
                    )
                    val sp = SharedPrefManager(this)
                    val list = sp.loadTransactions().toMutableList()
                    val index = list.indexOfFirst { it.id == updatedTransaction.id }

                    if (index != -1) {
                        list[index] = updatedTransaction
                        sp.overwriteTransactions(list)
                        showToast("Transaction updated!")
                        finish()
                    } else {
                        showToast("Failed to find transaction to update")
                    }
                }
            }
        }
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
