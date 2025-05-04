package com.example.lab_exam_3_personal_finance_tracker

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.lab_exam_3_personal_finance_tracker.data.SharedPrefManager
import com.example.lab_exam_3_personal_finance_tracker.model.Transaction
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.textfield.TextInputLayout
import java.util.*

class AddTransactionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)

        // Set up top toolbar with back button and title

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Add Expense"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val etTitle = findViewById<EditText>(R.id.etTitle)
        val etAmount = findViewById<EditText>(R.id.etAmount)
        val etDate = findViewById<EditText>(R.id.etDate)
        val spinnerCategory = findViewById<Spinner>(R.id.spinnerCategory)
        val imgCategoryPreview = findViewById<ImageView>(R.id.imgCategoryPreview)
        val btnSave = findViewById<Button>(R.id.btnSaveTransaction)

        val layoutTitle = findViewById<TextInputLayout>(R.id.layoutTitle)
        val layoutAmount = findViewById<TextInputLayout>(R.id.layoutAmount)
        val layoutDate = findViewById<TextInputLayout>(R.id.layoutDate)

        // Update the amount input hint with the selected currency

        val currency = SharedPrefManager(this).loadCurrency()
        layoutAmount.hint = "Amount ($currency)"

        // Initialize spinner with categories from resources

        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.category_array,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter

        // Change the preview icon based on selected category

        spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedCategory = parent?.getItemAtPosition(position).toString()
                imgCategoryPreview.setImageResource(getCategoryIconRes(selectedCategory))
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Open date picker dialog on date field click

        etDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(this, { _, y, m, d ->
                val formatted = "%04d-%02d-%02d".format(y, m + 1, d)
                etDate.setText(formatted)
            }, year, month, day)
            datePickerDialog.show()
        }

        // Validate inputs and save expense transaction

        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val amountText = etAmount.text.toString().trim()
            val amount = amountText.toDoubleOrNull()
            val category = spinnerCategory.selectedItem.toString().trim()
            val date = etDate.text.toString().trim()

            // Clear previous errors
            layoutTitle.error = null
            layoutAmount.error = null
            layoutDate.error = null

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
                    layoutDate.error = "Please pick a date"
                }
                else -> {
                    val transaction = Transaction(
                        id = System.currentTimeMillis(),
                        title = title,
                        amount = amount,
                        category = category,
                        date = date,
                        type = "Expense"
                    )
                    SharedPrefManager(this).saveTransaction(transaction)
                    showToast("Transaction saved successfully")
                    finish()
                }
            }
        }

        //  Setup Bottom Navigation Bar
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNav.selectedItemId = R.id.menu_add

        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.menu_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    true
                }
                R.id.menu_transactions -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                R.id.menu_add -> true // Already here
                R.id.menu_summary -> {
                    startActivity(Intent(this, SummaryActivity::class.java))
                    true
                }
                else -> false
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

    private fun getCategoryIconRes(category: String): Int {
        return when (category.trim().lowercase()) {
            "food" -> R.drawable.ic_category_food
            "transport" -> R.drawable.ic_category_transport
            "salary" -> R.drawable.ic_category_salary
            "freelance" -> R.drawable.ic_category_freelance
            "entertainment" -> R.drawable.ic_category_entertainment
            "bills" -> R.drawable.ic_category_bills
            "shopping" -> R.drawable.ic_category_shopping
            "health" -> R.drawable.ic_category_health
            "other" -> R.drawable.ic_category_other
            else -> R.drawable.ic_category_default
        }
    }
}
