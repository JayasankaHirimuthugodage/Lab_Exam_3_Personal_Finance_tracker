package com.example.lab_exam_3_personal_finance_tracker

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.lab_exam_3_personal_finance_tracker.data.SharedPrefManager
import com.example.lab_exam_3_personal_finance_tracker.model.Transaction
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.SimpleDateFormat
import java.util.*

class SummaryActivity : AppCompatActivity() {

    private lateinit var pieChart: PieChart
    private lateinit var spinnerYear: Spinner
    private lateinit var spinnerMonth: Spinner
    private lateinit var spinnerChartType: Spinner
    private lateinit var tvMonthlyBudget: TextView
    private lateinit var btnEditBudget: Button
    private lateinit var btnOpenSettings: Button

    private val monthFormat = SimpleDateFormat("MMMM", Locale.getDefault())
    private val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())
    private val fullDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private var selectedMonth = ""
    private var selectedYear = ""
    private var selectedChartType = "Income vs Expense"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_summary)

        // ✅ Setup custom toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setTitleTextColor(Color.WHITE) // ✅ Force white title
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Summary"
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        pieChart = findViewById(R.id.pieChart)
        spinnerYear = findViewById(R.id.spinnerYear)
        spinnerMonth = findViewById(R.id.spinnerMonth)
        spinnerChartType = findViewById(R.id.spinnerChartType)
        tvMonthlyBudget = findViewById(R.id.tvMonthlyBudget)
        btnEditBudget = findViewById(R.id.btnEditBudget)
        btnOpenSettings = findViewById(R.id.btnOpenSettings)

        btnEditBudget.setOnClickListener { showEditBudgetDialog() }

        btnOpenSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        setupYearSpinner()
        setupMonthSpinner()
        setupChartTypeSpinner()
        displayMonthlyBudget()

        // ✅ Bottom Navigation Bar setup
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNav.selectedItemId = R.id.menu_summary

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
                R.id.menu_add -> {
                    startActivity(Intent(this, AddTransactionActivity::class.java))
                    true
                }
                R.id.menu_summary -> true
                else -> false
            }
        }
    }

    private fun showEditBudgetDialog() {
        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        input.hint = "Enter new budget"

        AlertDialog.Builder(this)
            .setTitle("Edit Monthly Budget")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val newBudget = input.text.toString().toFloatOrNull()
                if (newBudget != null && newBudget >= 0f) {
                    val prefs = getSharedPreferences("FinancePrefs", MODE_PRIVATE)
                    prefs.edit().putFloat("monthly_budget", newBudget).apply()
                    displayMonthlyBudget()
                    Toast.makeText(this, "Budget updated", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Invalid budget value", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

    private fun setupYearSpinner() {
        val transactionYears = SharedPrefManager(this).loadTransactions()
            .mapNotNull {
                try {
                    val date = fullDateFormat.parse(it.date)
                    yearFormat.format(date!!)
                } catch (e: Exception) {
                    null
                }
            }

        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val fallbackYears = (currentYear downTo currentYear - 4).map { it.toString() }

        val years = (transactionYears + fallbackYears)
            .distinct()
            .sortedDescending()

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, years)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerYear.adapter = adapter

        val defaultIndex = years.indexOf(currentYear.toString()).takeIf { it >= 0 } ?: 0
        spinnerYear.setSelection(defaultIndex)
        selectedYear = years[defaultIndex]

        spinnerYear.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                selectedYear = parent.getItemAtPosition(pos).toString()
                updateChart()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupMonthSpinner() {
        val months = listOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, months)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerMonth.adapter = adapter

        val currentMonth = monthFormat.format(Date())
        spinnerMonth.setSelection(months.indexOf(currentMonth))
        selectedMonth = currentMonth

        spinnerMonth.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                selectedMonth = parent.getItemAtPosition(pos).toString()
                updateChart()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupChartTypeSpinner() {
        val chartTypes = listOf(
            "Income vs Expense",
            "Category-wise Income",
            "Category-wise Expenses"
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, chartTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerChartType.adapter = adapter

        spinnerChartType.setSelection(0)
        spinnerChartType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                selectedChartType = parent.getItemAtPosition(pos).toString()
                updateChart()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun displayMonthlyBudget() {
        val prefs = getSharedPreferences("FinancePrefs", MODE_PRIVATE)
        val budget = prefs.getFloat("monthly_budget", 0f)
        val currency = SharedPrefManager(this).loadCurrency()
        tvMonthlyBudget.text = "Monthly Budget: $currency %.2f".format(budget)
    }

    private fun updateChart() {
        val transactions = SharedPrefManager(this).loadTransactions()
            .filter { isInSelectedMonthAndYear(it.date, selectedMonth, selectedYear) }

        when (selectedChartType) {
            "Income vs Expense" -> showIncomeVsExpenseChart(transactions)
            "Category-wise Income" -> showCategoryChart(transactions, "Income")
            "Category-wise Expenses" -> showCategoryChart(transactions, "Expense")
        }
    }

    private fun showIncomeVsExpenseChart(transactions: List<Transaction>) {
        val incomeTotal = transactions.filter { it.type == "Income" }.sumOf { it.amount }
        val expenseTotal = transactions.filter { it.type == "Expense" }.sumOf { it.amount }

        val entries = listOf(
            PieEntry(incomeTotal.toFloat(), "Income"),
            PieEntry(expenseTotal.toFloat(), "Expenses")
        )

        val dataSet = PieDataSet(entries, "Monthly Summary")
        dataSet.colors = listOf(Color.parseColor("#4CAF50"), Color.parseColor("#F44336"))
        dataSet.valueTextSize = 14f
        dataSet.valueTextColor = Color.WHITE

        val data = PieData(dataSet)

        pieChart.apply {
            this.data = data
            description.isEnabled = false
            centerText = "Income vs Expense"
            setCenterTextSize(16f)
            setUsePercentValues(false)
            animateY(1000)

            legend.orientation = Legend.LegendOrientation.VERTICAL
            legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
            legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            legend.setDrawInside(false)
            legend.isWordWrapEnabled = true
            legend.maxSizePercent = 1f
            legend.textSize = 14f

            invalidate()
        }
    }

    private fun showCategoryChart(transactions: List<Transaction>, type: String) {
        val filtered = transactions.filter { it.type == type }
        val grouped = filtered.groupBy { it.category }

        val entries = grouped.map { (category, list) ->
            val total = list.sumOf { it.amount }
            PieEntry(total.toFloat(), category)
        }

        val distinctColors = listOf(
            Color.parseColor("#1f77b4"), Color.parseColor("#ff7f0e"),
            Color.parseColor("#2ca02c"), Color.parseColor("#d62728"),
            Color.parseColor("#9467bd"), Color.parseColor("#8c564b"),
            Color.parseColor("#e377c2"), Color.parseColor("#7f7f7f"),
            Color.parseColor("#bcbd22"), Color.parseColor("#17becf"),
            Color.parseColor("#f44336"), Color.parseColor("#3f51b5"),
            Color.parseColor("#009688"), Color.parseColor("#795548"),
            Color.parseColor("#9c27b0")
        )

        val dataSet = PieDataSet(entries, "$type by Category")
        dataSet.colors = if (entries.size <= distinctColors.size)
            distinctColors.take(entries.size)
        else
            distinctColors + List(entries.size - distinctColors.size) {
                Color.rgb((0..255).random(), (0..255).random(), (0..255).random())
            }

        dataSet.valueTextSize = 14f
        dataSet.valueTextColor = Color.WHITE

        val data = PieData(dataSet)

        pieChart.apply {
            this.data = data
            description.isEnabled = false
            centerText = "$type by Category"
            setCenterTextSize(16f)
            setUsePercentValues(false)
            animateY(1000)

            legend.orientation = Legend.LegendOrientation.VERTICAL
            legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
            legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            legend.setDrawInside(false)
            legend.isWordWrapEnabled = true
            legend.maxSizePercent = 1f
            legend.textSize = 14f

            invalidate()
        }
    }

    private fun isInSelectedMonthAndYear(dateStr: String, selectedMonth: String, selectedYear: String): Boolean {
        return try {
            val date = fullDateFormat.parse(dateStr)
            val monthMatches = monthFormat.format(date!!) == selectedMonth
            val yearMatches = yearFormat.format(date) == selectedYear
            monthMatches && yearMatches
        } catch (e: Exception) {
            false
        }
    }

    override fun onResume() {
        super.onResume()
        displayMonthlyBudget()
        updateChart()
    }
}
