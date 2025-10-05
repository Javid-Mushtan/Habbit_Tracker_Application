package com.javid.habitify.receivers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.javid.habitify.R
import com.javid.habitify.services.WaterReminderService
import com.javid.habitify.utils.PrefsManager
import com.javid.habitify.model.WaterLog

class WaterReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        try {
            val reminderId = intent.getIntExtra("reminder_id", 0)
            val currentIntake = intent.getIntExtra("current_intake", 0)
            val dailyGoal = PrefsManager(context).getAllPreferences()["daily_goal"]
            val remaining = intent.getIntExtra("remaining", 2000)

            val prefsManager = PrefsManager(context)
            val today = WaterLog.getTodayDate()
            val currentLogsJson = prefsManager.getUserPreference("water_logs_$today", "")
            val actualCurrentIntake = if (currentLogsJson.isNotEmpty()) {
                try {
                    val logs = Gson().fromJson<List<WaterLog>>(currentLogsJson, object : TypeToken<List<WaterLog>>() {}.type)
                    logs?.sumOf { it.amount } ?: 0
                } catch (e: Exception) {
                    0
                }
            } else {
                0
            }

            val actualRemaining = (dailyGoal as Int) - actualCurrentIntake

            Log.d("WaterReminder", "📊 Reminder #$reminderId - Progress: $actualCurrentIntake/$dailyGoal ml, Remaining: $actualRemaining ml")

            if (actualRemaining > 0) {
                showReminderNotification(context, actualCurrentIntake,
                    dailyGoal, actualRemaining, reminderId)

                if (reminderId < 11) {
                    WaterReminderService.startService(context)
                }
            } else {
                Log.d("WaterReminder", "✅ Goal completed. Cancelling reminders.")
                WaterReminderService.cancelAllReminders(context)
            }

        } catch (e: Exception) {
            Log.e("WaterReminder", "❌ Error in reminder receiver: ${e.message}")
        }
    }

    private fun showReminderNotification(context: Context, currentIntake: Int, dailyGoal: Int, remaining: Int, reminderId: Int) {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    "water_reminders",
                    "Water Reminders",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Reminders to drink water"
                    enableVibration(true)
                    enableLights(true)
                }
                notificationManager.createNotificationChannel(channel)
            }

            val progressPercent = (currentIntake.toFloat() / dailyGoal * 100).toInt()
            val notification = NotificationCompat.Builder(context, "water_reminders")
                .setSmallIcon(R.drawable.ic_water_drop)
                .setContentTitle("💧 Time to Drink Water!")
                .setContentText("You've drunk $currentIntake/$dailyGoal ml ($progressPercent%)")
                .setStyle(NotificationCompat.BigTextStyle()
                    .bigText("💧 Stay Hydrated!\n\n" +
                            "Progress: $currentIntake/$dailyGoal ml ($progressPercent%)\n" +
                            "Remaining: $remaining ml\n\n" +
                            "Keep going! Your body needs water to stay healthy. 💪"))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
//                .setSound()
                .setAutoCancel(false)
                .build()

            notificationManager.notify(reminderId + 2000, notification)

            Log.d("WaterReminder", "📱 Reminder notification shown - Progress: $progressPercent%")

        } catch (e: Exception) {
            Log.e("WaterReminder", "❌ Failed to show reminder notification: ${e.message}")
        }
    }
}