package com.javid.habitify.receivers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.javid.habitify.services.HabitReminderService

class HabitReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        try {
            HabitReminderService.startService(context)

            val habitName = intent.getStringExtra("habit_name") ?: "Your habit"
            val habitId = intent.getLongExtra("habit_id", 0L)

            showReminderNotification(context, habitName, habitId)

            val isDaily = intent.getBooleanExtra("is_daily", true)
            if (isDaily) {
                rescheduleDailyReminder(context, intent)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showReminderNotification(context: Context, habitName: String, habitId: Long) {
        try {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            createNotificationChannel(notificationManager)

            val notification = NotificationCompat.Builder(context, "habit_reminders")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("⏰ Habit Reminder")
                .setContentText("Time for: $habitName")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setColor(Color.BLUE)
                .setStyle(NotificationCompat.BigTextStyle()
                    .bigText("Don't forget to complete your habit: $habitName. Stay consistent!"))
                .build()

            val notificationId = habitId.toInt()
            notificationManager.notify(notificationId, notification)

            Log.d("HabitReminder", "✅ Notification shown for: $habitName")

        } catch (e: Exception) {
            Log.e("HabitReminder", "❌ Failed to show notification: ${e.message}")
        }
    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager.getNotificationChannel("habit_reminders") == null) {
                val channel = NotificationChannel(
                    "habit_reminders",
                    "Habit Reminders",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Reminders for your daily habits"
                    enableLights(true)
                    lightColor = Color.BLUE
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 500, 250, 500)
                }
                notificationManager.createNotificationChannel(channel)
            }
        }
    }

    private fun rescheduleDailyReminder(context: Context, originalIntent: Intent) {
        try {
            val habitName = originalIntent.getStringExtra("habit_name") ?: return
            val habitId = originalIntent.getLongExtra("habit_id", 0L)
            val hour = originalIntent.getIntExtra("hour", 9)
            val minute = originalIntent.getIntExtra("minute", 0)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
            val intent = Intent(context, HabitReminderReceiver::class.java).apply {
                putExtra("habit_name", habitName)
                putExtra("habit_id", habitId)
                putExtra("hour", hour)
                putExtra("minute", minute)
                putExtra("is_daily", true)
            }

            val pendingIntent = android.app.PendingIntent.getBroadcast(
                context,
                habitId.toInt(),
                intent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )

            val calendar = java.util.Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(java.util.Calendar.HOUR_OF_DAY, hour)
                set(java.util.Calendar.MINUTE, minute)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
                add(java.util.Calendar.DAY_OF_MONTH, 1)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    android.app.AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    android.app.AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }

            Log.d("HabitReminder", "✅ Daily reminder rescheduled for: $habitName")

        } catch (e: Exception) {
            Log.e("HabitReminder", "❌ Error rescheduling: ${e.message}")
        }
    }
}