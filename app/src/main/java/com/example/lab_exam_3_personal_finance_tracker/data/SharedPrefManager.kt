package com.example.lab_exam_3_personal_finance_tracker.data

import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import com.example.lab_exam_3_personal_finance_tracker.model.Transaction
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileWriter

class SharedPrefManager(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("FinancePrefs", Context.MODE_PRIVATE)

    private val gson = Gson()
    private val key = "transactions"
    private val currencyKey = "selected_currency"
    private val notifyKey = "budget_notify"
    private val fullNameKey = "user_full_name"
    private val emailKey = "user_email"
    private val passwordKey = "user_password"

    // Add the new transaction to the existing list and save it in SharedPreferences

    fun saveTransaction(newTransaction: Transaction) {
        val currentList = loadTransactions().toMutableList()
        currentList.add(newTransaction)
        val json = gson.toJson(currentList)
        prefs.edit().putString(key, json).apply()
    }

    // Retrieve the list of transactions from SharedPreferences and return it as a list
    fun loadTransactions(): List<Transaction> {
        val json = prefs.getString(key, null) ?: return emptyList()
        val type = object : TypeToken<List<Transaction>>() {}.type
        return gson.fromJson(json, type)
    }

    // Replace the current list of transactions with the new list in SharedPreferences

    fun overwriteTransactions(newList: List<Transaction>) {
        val json = gson.toJson(newList)
        prefs.edit().putString(key, json).apply()
    }

    //  Save and load currency
    fun saveCurrency(currency: String) {
        prefs.edit().putString(currencyKey, currency).apply()
    }

    fun loadCurrency(): String {
        return prefs.getString(currencyKey, "LKR") ?: "LKR"
    }

    //  Notifications toggle
    fun setBudgetNotificationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(notifyKey, enabled).apply()
    }

    fun isBudgetNotificationEnabled(): Boolean {
        return prefs.getBoolean(notifyKey, true)
    }

    //  Save and load user profile info
    fun saveFullName(name: String) {
        prefs.edit().putString(fullNameKey, name).apply()
    }

    fun loadFullName(): String {
        return prefs.getString(fullNameKey, "") ?: ""
    }

    fun saveEmail(email: String) {
        prefs.edit().putString(emailKey, email).apply()
    }

    fun loadEmail(): String {
        return prefs.getString(emailKey, "") ?: ""
    }

    fun savePassword(password: String) {
        prefs.edit().putString(passwordKey, password).apply()
    }

    fun loadPassword(): String {
        return prefs.getString(passwordKey, "") ?: ""
    }

    //  Clear all data (delete account)
    fun clearAll() {
        prefs.edit().clear().apply()
    }

    // Serialize the list of transactions to JSON and save it as a backup file in internal storage

    fun backupToFile() {
        try {
            val transactionList = loadTransactions()
            val json = gson.toJson(transactionList)
            val file = File(context.filesDir, "backup_transactions.json")
            val writer = FileWriter(file)
            writer.write(json)
            writer.close()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Backup failed", Toast.LENGTH_SHORT).show()
        }
    }

    // Read the backup file from internal storage, deserialize it, and restore the transactions

    fun restoreFromFile() {
        try {
            val file = File(context.filesDir, "backup_transactions.json")
            if (file.exists()) {
                val json = file.readText()
                val type = object : TypeToken<List<Transaction>>() {}.type
                val restoredList: List<Transaction> = gson.fromJson(json, type)
                overwriteTransactions(restoredList)
            } else {
                Toast.makeText(context, "No backup file found", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Restore failed", Toast.LENGTH_SHORT).show()
        }
    }
}
