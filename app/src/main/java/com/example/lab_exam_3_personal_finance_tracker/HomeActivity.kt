package com.example.lab_exam_3_personal_finance_tracker

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.lab_exam_3_personal_finance_tracker.data.SharedPrefManager
import com.example.lab_exam_3_personal_finance_tracker.utils.NotificationHelper
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.SimpleDateFormat
import java.util.*

class HomeActivity : AppCompatActivity() {

    private lateinit var tvTotalIncome: TextView
    private lateinit var tvTotalExpense: TextView
    private lateinit var tvBalance: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Request POST_NOTIFICATIONS permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
        }

        // Set up Toolbar (no back arrow needed)
        val toolbar = findViewById<MaterialToolbar>(R.id.homeToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Home"
        toolbar.setTitleTextColor(resources.getColor(android.R.color.white, theme))

        // ✅ Handle toolbar menu clicks
        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_sign_out -> {
                    showSignOutDialog()
                    true
                }
                else -> false
            }
        }

        // Initialize text views
        tvTotalIncome = findViewById(R.id.tvTotalIncome)
        tvTotalExpense = findViewById(R.id.tvTotalExpense)
        tvBalance = findViewById(R.id.tvBalance)

        // Buttons
        val btnAddTransaction = findViewById<Button>(R.id.btnAddTransaction)
        val btnAddIncome = findViewById<Button>(R.id.btnAddIncome)
        val btnSetBudget = findViewById<Button>(R.id.btnSetBudget)
        val btnSignOut = findViewById<Button>(R.id.btnSignOut) // ✅ From layout
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        btnAddTransaction.setOnClickListener {
            startActivity(Intent(this, AddTransactionActivity::class.java))
        }

        btnAddIncome.setOnClickListener {
            startActivity(Intent(this, AddIncomeActivity::class.java))
        }

        btnSetBudget.setOnClickListener {
            startActivity(Intent(this, BudgetActivity::class.java))
        }

        btnSignOut.setOnClickListener {
            showSignOutDialog()
        }

        // Bottom nav actions
        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.menu_home -> true
                R.id.menu_transactions -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                R.id.menu_add -> {
                    startActivity(Intent(this, AddTransactionActivity::class.java))
                    true
                }
                R.id.menu_summary -> {
                    startActivity(Intent(this, SummaryActivity::class.java))
                    true
                }
                else -> false
            }
        }

        bottomNav.selectedItemId = R.id.menu_home
    }

    override fun onResume() {
        super.onResume()
        updateSummary()
    }

    private fun updateSummary() {
        val transactions = SharedPrefManager(this).loadTransactions()

        val calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val monthFormat = SimpleDateFormat("MMMM", Locale.getDefault())
        val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())
        val currentMonthStr = monthFormat.format(calendar.time)
        val currentYearStr = yearFormat.format(calendar.time)

        val monthlyTransactions = transactions.filter {
            runCatching {
                val date = sdf.parse(it.date)
                val itemMonth = monthFormat.format(date!!)
                val itemYear = yearFormat.format(date)
                itemMonth == currentMonthStr && itemYear == currentYearStr
            }.getOrDefault(false)
        }

        val totalIncome = monthlyTransactions.filter { it.type == "Income" }.sumOf { it.amount }
        val totalExpense = monthlyTransactions.filter { it.type == "Expense" }.sumOf { it.amount }
        val balance = totalIncome - totalExpense

        val currency = SharedPrefManager(this).loadCurrency()
        tvTotalIncome.text = "Total Income: $currency %.2f".format(totalIncome)
        tvTotalExpense.text = "Total Expenses: $currency %.2f".format(totalExpense)
        tvBalance.text = "Balance: $currency %.2f".format(balance)

        val sharedPrefs = SharedPrefManager(this)
        val budget = getSharedPreferences("FinancePrefs", MODE_PRIVATE).getFloat("monthly_budget", -1f)

        if (
            budget > 0 &&
            totalExpense >= budget * 0.9 &&
            sharedPrefs.isBudgetNotificationEnabled()
        ) {
            NotificationHelper.showBudgetWarning(this, totalExpense, budget)
        }
    }

    private fun showSignOutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Sign Out")
            .setMessage("Are you sure you want to sign out?")
            .setPositiveButton("Yes") { _, _ ->
                val userPrefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                userPrefs.edit().putBoolean("isLoggedIn", false).apply()

                Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, SignInActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
