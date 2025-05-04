package com.example.lab_exam_3_personal_finance_tracker

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lab_exam_3_personal_finance_tracker.adapter.TransactionAdapter
import com.example.lab_exam_3_personal_finance_tracker.data.SharedPrefManager
import com.example.lab_exam_3_personal_finance_tracker.model.Transaction
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var rvTransactions: RecyclerView
    private lateinit var adapter: TransactionAdapter
    private var allTransactions = mutableListOf<Transaction>()

    private lateinit var spinnerYear: Spinner
    private lateinit var spinnerMonth: Spinner
    private lateinit var spinnerType: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //  Toolbar setup
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "All Transactions"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Initialize RecyclerView and filter spinners

        rvTransactions = findViewById(R.id.rvTransactions)
        spinnerYear = findViewById(R.id.spinnerYear)
        spinnerMonth = findViewById(R.id.spinnerMonth)
        spinnerType = findViewById(R.id.spinnerType)

        // Load and display data
        allTransactions = SharedPrefManager(this).loadTransactions().toMutableList()
        adapter = TransactionAdapter(allTransactions.toMutableList(),
            onEditClick = { transaction ->
                val intent = Intent(this, EditTransactionActivity::class.java)
                intent.putExtra("transaction", transaction)
                startActivity(intent)
            },
            onDeleteClick = { transaction ->
                val sp = SharedPrefManager(this)
                val updatedList = sp.loadTransactions().toMutableList().apply {
                    removeAll { it.id == transaction.id }
                }
                sp.overwriteTransactions(updatedList)

                allTransactions = updatedList
                applyFilters()
                Toast.makeText(this, "Transaction deleted", Toast.LENGTH_SHORT).show()
            }
        )

        rvTransactions.layoutManager = LinearLayoutManager(this)

        // Apply custom spacing between RecyclerView items

        val itemSpacing = resources.getDimensionPixelSize(R.dimen.transaction_item_spacing)
        rvTransactions.addItemDecoration(TransactionItemDecoration(itemSpacing))

        rvTransactions.adapter = adapter

        setupSpinners()
        setupBottomNavigation()
    }

    override fun onResume() {
        super.onResume()
        allTransactions = SharedPrefManager(this).loadTransactions().toMutableList()
        applyFilters()
    }

    private fun setupSpinners() {
        // Year Spinner
        val years = allTransactions.map {
            SimpleDateFormat("yyyy", Locale.getDefault()).format(it.dateAsDate())
        }.distinct().sortedDescending()
        val yearOptions = listOf("All") + years

        val yearAdapter = ArrayAdapter(this, R.layout.spinner_item, yearOptions)
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerYear.adapter = yearAdapter

        // Month Spinner
        val monthOptions = listOf("All", "01", "02", "03", "04", "05", "06",
            "07", "08", "09", "10", "11", "12")
        val monthAdapter = ArrayAdapter(this, R.layout.spinner_item, monthOptions)
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerMonth.adapter = monthAdapter

        // Type Spinner
        val typeOptions = listOf("All", "Income", "Expense")
        val typeAdapter = ArrayAdapter(this, R.layout.spinner_item, typeOptions)
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerType.adapter = typeAdapter

        // Listeners
        val listener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                applyFilters()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        spinnerYear.onItemSelectedListener = listener
        spinnerMonth.onItemSelectedListener = listener
        spinnerType.onItemSelectedListener = listener
    }

    private fun applyFilters() {
        val selectedYear = spinnerYear.selectedItem?.toString()
        val selectedMonth = spinnerMonth.selectedItem?.toString()
        val selectedType = spinnerType.selectedItem?.toString()

        val filtered = allTransactions.filter { transaction ->
            val dateParts = transaction.date.split("-") // yyyy-MM-dd
            val yearMatch = selectedYear == "All" || dateParts[0] == selectedYear
            val monthMatch = selectedMonth == "All" || dateParts[1] == selectedMonth
            val typeMatch = selectedType == "All" || transaction.type.equals(selectedType, ignoreCase = true)
            yearMatch && monthMatch && typeMatch
        }

        adapter.updateList(filtered)
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNav.selectedItemId = R.id.menu_transactions

        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.menu_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    true
                }
                R.id.menu_transactions -> true
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
    }

    private fun Transaction.dateAsDate(): Date {
        return try {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(this.date)!!
        } catch (e: Exception) {
            Date()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    /**
     * ItemDecoration to add padding around each transaction item in the list.
     * Adds extra top padding to the first item for visual separation.
     */

    inner class TransactionItemDecoration(private val spacing: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            with(outRect) {
                // Add top spacing for the first item
                if (parent.getChildAdapterPosition(view) == 0) {
                    top = spacing
                }

                left = spacing
                right = spacing
                bottom = spacing
            }
        }
    }
}