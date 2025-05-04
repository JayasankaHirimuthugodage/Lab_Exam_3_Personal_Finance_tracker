package com.example.lab_exam_3_personal_finance_tracker

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.widget.Toolbar
import com.example.lab_exam_3_personal_finance_tracker.data.SharedPrefManager
import java.io.File

class SettingsActivity : AppCompatActivity() {

    private lateinit var spinnerCurrency: Spinner
    private lateinit var btnSaveCurrency: Button
    private lateinit var switchNotifications: SwitchCompat
    private lateinit var etFullName: EditText
    private lateinit var etEmail: EditText
    private lateinit var btnDeleteAccount: Button
    private lateinit var btnBackup: Button
    private lateinit var btnRestore: Button
    private lateinit var btnExportText: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Toolbar setup
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.settings_title)
        toolbar.setTitleTextColor(resources.getColor(android.R.color.white, theme))
        toolbar.setNavigationOnClickListener { onBackPressed() }

        // Initialize UI components
        spinnerCurrency     = findViewById(R.id.spinnerCurrency)
        btnSaveCurrency     = findViewById(R.id.btnSaveCurrency)
        switchNotifications = findViewById(R.id.switchNotifications)
        etFullName          = findViewById(R.id.etFullName)
        etEmail             = findViewById(R.id.etEmail)
        btnDeleteAccount    = findViewById(R.id.btnDeleteAccount)
        btnBackup           = findViewById(R.id.btnBackup)
        btnRestore          = findViewById(R.id.btnRestore)
        btnExportText       = findViewById(R.id.btnExportText)

        val prefs = SharedPrefManager(this)

        // Currency spinner setup
        val currencyList = resources.getStringArray(R.array.currency_array).toList()
        ArrayAdapter(this, android.R.layout.simple_spinner_item, currencyList).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerCurrency.adapter = it
        }

        // Pre-load saved values
        spinnerCurrency.setSelection(
            currencyList.indexOf(prefs.loadCurrency()).takeIf { it >= 0 } ?: 0
        )
        switchNotifications.isChecked = prefs.isBudgetNotificationEnabled()
        etFullName.setText(prefs.loadFullName())
        etEmail.setText(prefs.loadEmail())

        // Save Preferences
        btnSaveCurrency.setOnClickListener {
            prefs.saveCurrency(spinnerCurrency.selectedItem.toString())
            prefs.setBudgetNotificationEnabled(switchNotifications.isChecked)
            prefs.saveFullName(etFullName.text.toString().trim())
            prefs.saveEmail(etEmail.text.toString().trim())

            Toast.makeText(this, "Preferences saved", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Delete account
        btnDeleteAccount.setOnClickListener {
            prefs.clearAll()
            Toast.makeText(this, "Account deleted", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Backup
        btnBackup.setOnClickListener {
            prefs.backupToFile()
            Toast.makeText(this, "Backup exported", Toast.LENGTH_SHORT).show()
        }

        // Restore
        btnRestore.setOnClickListener {
            prefs.restoreFromFile()
            Toast.makeText(this, "Backup restored", Toast.LENGTH_SHORT).show()
        }

        // Export as text
        btnExportText.setOnClickListener {
            val transactions = prefs.loadTransactions()
            if (transactions.isEmpty()) {
                Toast.makeText(this, "No transactions to export", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val formattedText = transactions.joinToString("\n-----------------------------\n") {
                "Title   : ${it.title}\n" +
                        "Amount  : ${it.amount} ${prefs.loadCurrency()}\n" +
                        "Type    : ${it.type}\n" +
                        "Category: ${it.category}\n" +
                        "Date    : ${it.date}"
            }
            try {
                val fileName = "exported_transactions.txt"
                val downloads = android.os.Environment
                    .getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
                if (!downloads.exists()) downloads.mkdirs()

                File(downloads, fileName).writeText(formattedText)
                Toast.makeText(
                    this,
                    "Exported to Downloads:\n${downloads.absolutePath}/$fileName",
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Export failed", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
