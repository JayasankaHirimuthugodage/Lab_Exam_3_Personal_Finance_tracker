package com.example.lab_exam_3_personal_finance_tracker.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.lab_exam_3_personal_finance_tracker.R

object NotificationHelper {

    private const val CHANNEL_ID = "budget_channel"
    private const val CHANNEL_NAME = "Budget Alerts"

    fun showBudgetWarning(context: Context, expense: Double, budget: Float) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // you can customize this icon
            .setContentTitle("⚠️ Budget Limit Alert")
            .setContentText("You've spent LKR %.2f of your LKR %.2f budget.".format(expense, budget))
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        manager.notify(1, builder.build())
    }
}
