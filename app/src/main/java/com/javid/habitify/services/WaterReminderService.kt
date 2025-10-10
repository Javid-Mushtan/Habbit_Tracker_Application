package com.javid.habitify.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.javid.habitify.R
import com.javid.habitify.receivers.WaterReminderReceiver
import com.javid.habitify.utils.PrefsManager
import com.javid.habitify.model.WaterLog
import java.util.*
import java.util.concurrent.TimeUnit

class WaterReminderService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("WaterReminder", "🚀 Starting water reminder service")

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())

        // Check if we should schedule reminders
        checkAndScheduleReminders()

        stopSelf()
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun checkAndScheduleReminders() {
        try {
            val prefsManager = PrefsManager(this)
            val today = WaterLog.getTodayDate()

            val dailyGoal = prefsManager.getUserPreference("water_daily_goal", "2000").toIntOrNull() ?: 2000
            val currentLogsJson = prefsManager.getUserPreference("water_logs_$today", "")
            val currentIntake = if (currentLogsJson.isNotEmpty()) {
                try {
                    val logs = com.google.gson.Gson().fromJson<List<WaterLog>>(currentLogsJson, object : com.google.gson.reflect.TypeToken<List<WaterLog>>() {}.type)
                    logs?.sumOf { it.amount } ?: 0
                } catch (e: Exception) {
                    0
                }
            } else {
                0
            }

            val remaining = dailyGoal - currentIntake

            Log.d("WaterReminder", "💧 Progress: $currentIntake/$dailyGoal ml, Remaining: $remaining ml")

            if (remaining > 0) {
                scheduleHourlyReminders(remaining, dailyGoal, currentIntake)
            } else {
                Log.d("WaterReminder", "✅ Goal completed. Cancelling all reminders.")
                cancelAllReminders(this)
            }

        } catch (e: Exception) {
            Log.e("WaterReminder", "❌ Error checking reminders: ${e.message}")
        }
    }

    private fun scheduleHourlyReminders(remaining: Int, dailyGoal: Int, currentIntake: Int) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        cancelAllReminders(this)

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            add(Calendar.MINUTE, 5)
        }

        for (i in 0 until 12) {
            val reminderTime = calendar.timeInMillis

            val intent = Intent(this, WaterReminderReceiver::class.java).apply {
                putExtra("reminder_id", i)
                putExtra("current_intake", currentIntake)
                putExtra("daily_goal", dailyGoal)
                putExtra("remaining", remaining)
                putExtra("reminder_time", reminderTime)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                this,
                i + 1000,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminderTime,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    reminderTime,
                    pendingIntent
                )
            }

            Log.d("WaterReminder", "⏰ Scheduled reminder #$i at ${Date(reminderTime)}")

            calendar.add(Calendar.HOUR_OF_DAY, 1)
        }

        val prefsManager = PrefsManager(this)
        prefsManager.setUserPreference("reminders_active", "true")
        prefsManager.setUserPreference("last_reminder_schedule", System.currentTimeMillis().toString())

        Log.d("WaterReminder", "✅ Hourly reminders scheduled for 12 hours")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Water Reminder Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Manages hourly water drinking reminders"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Water Tracker")
            .setContentText("Managing your hourly water reminders")
            .setSmallIcon(R.drawable.ic_water_drop)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(false)
            .setAutoCancel(true)
            .build()
    }

    companion object {
        private const val CHANNEL_ID = "water_reminder_service"
        private const val NOTIFICATION_ID = 1003

        fun startService(context: Context) {
            try {
                val intent = Intent(context, WaterReminderService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
                Log.d("WaterReminder", "🏁 Reminder service started")
            } catch (e: Exception) {
                Log.e("WaterReminder", "❌ Failed to start reminder service: ${e.message}")
            }
        }

        fun scheduleReminders(context: Context) {
            startService(context)
        }

        fun cancelAllReminders(context: Context) {
            try {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

                for (i in 0 until 12) {
                    val intent = Intent(context, WaterReminderReceiver::class.java)
                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        i + 1000,
                        intent,
                        PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
                    )

                    pendingIntent?.let {
                        alarmManager.cancel(it)
                        it.cancel()
                    }
                }

                val prefsManager = PrefsManager(context)
                prefsManager.setUserPreference("reminders_active", "false")

                Log.d("WaterReminder", "❌ All water reminders cancelled")
            } catch (e: Exception) {
                Log.e("WaterReminder", "❌ Failed to cancel reminders: ${e.message}")
            }
        }

        fun areRemindersActive(context: Context): Boolean {
            val prefsManager = PrefsManager(context)
            return prefsManager.getUserPreference("reminders_active", "false") == "true"
        }
    }
}