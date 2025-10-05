package com.javid.habitify.receivers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.javid.habitify.R
import com.javid.habitify.services.HabitReminderService

class HabitReminderReceiver : BroadcastReceiver() {

    companion object {
        private const val CHANNEL_ID = "habit_reminders_channel"
    }

    override fun onReceive(context: Context, intent: Intent) {
        try {
            Log.d("HabitReminder", "🎯 Receiver triggered for: ${intent.getStringExtra("habit_name")}")

            HabitReminderService.startService(context)

            val habitName = intent.getStringExtra("habit_name") ?: "Your habit"
            val habitId = intent.getLongExtra("habit_id", 0L)

            showReminderNotification(context, habitName, habitId)

            val isDaily = intent.getBooleanExtra("is_daily", true)
            if (isDaily) {
                rescheduleDailyReminder(context, intent)
            }

        } catch (e: Exception) {
            Log.e("HabitReminder", "❌ Error in receiver: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun showReminderNotification(context: Context, habitName: String, habitId: Long) {
        try {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            createNotificationChannel(context, notificationManager)

            // Try multiple sound sources
            val soundUri = getAlarmSoundUri(context)
            Log.d("HabitReminder", "Using sound URI: $soundUri")

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("⏰ Habit Reminder")
                .setContentText("Time for: $habitName")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(true)
                .setColor(Color.RED)
                .setOnlyAlertOnce(false)
                .setSound(soundUri)
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText("Don't forget to complete your habit: $habitName. Stay consistent!")
                )
                .build()

            val notificationId = habitId.toInt()
            notificationManager.notify(notificationId, notification)

            Log.d("HabitReminder", "✅ Notification shown for: $habitName with sound: $soundUri")

        } catch (e: Exception) {
            Log.e("HabitReminder", "❌ Failed to show notification: ${e.message}")
        }
    }

    private fun getAlarmSoundUri(context: Context): Uri {
        return try {
            val customSoundUri = Uri.parse("android.resource://${context.packageName}/${R.raw.alarm}")
            Log.d("HabitReminder", "Trying custom sound: $customSoundUri")
            customSoundUri
        } catch (e: Exception) {
            Log.w("HabitReminder", "Custom sound failed, using default alarm: ${e.message}")

            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM).also {
                Log.d("HabitReminder", "Using system alarm sound: $it")
            }
        }
    }

    private fun createNotificationChannel(context: Context, notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            notificationManager.deleteNotificationChannel(CHANNEL_ID)

            val soundUri = getAlarmSoundUri(context)
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                .build()

            val channel = NotificationChannel(
                CHANNEL_ID,
                "Habit Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for your daily habits with alarm sound"
                enableLights(true)
                lightColor = Color.RED
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 1000, 500, 1000)
                setSound(soundUri, audioAttributes)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                setBypassDnd(true)

                importance = NotificationManager.IMPORTANCE_HIGH
            }

            notificationManager.createNotificationChannel(channel)
            Log.d("HabitReminder", "🔊 Notification channel created with sound: $soundUri")
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

            Log.d("HabitReminder", "✅ Daily reminder rescheduled for: $habitName at $hour:$minute")

        } catch (e: Exception) {
            Log.e("HabitReminder", "❌ Error rescheduling: ${e.message}")
        }
    }
}