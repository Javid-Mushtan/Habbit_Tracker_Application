package com.javid.habitify.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat

class HabitReminderService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("HabitReminder", "Foreground service started")
        startForeground(NOTIFICATION_ID, createNotification())
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        Log.d("HabitReminder", "Foreground service destroyed")
    }

    private fun createNotification(): Notification {
        createNotificationChannel()

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Habitify Reminder Service")
            .setContentText("Keeping your reminders active")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Habit Reminder Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps habit reminders running"
                setShowBadge(false)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val CHANNEL_ID = "habit_reminder_service"
        private const val NOTIFICATION_ID = 1234

        fun startService(context: Context) {
            try {
                val intent = Intent(context, HabitReminderService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
                Log.d("HabitReminder", "Foreground service started successfully")
            } catch (e: Exception) {
                Log.e("HabitReminder", "Failed to start foreground service: ${e.message}")
            }
        }

        fun stopService(context: Context) {
            try {
                val intent = Intent(context, HabitReminderService::class.java)
                context.stopService(intent)
                Log.d("HabitReminder", "Foreground service stopped")
            } catch (e: Exception) {
                Log.e("HabitReminder", "Failed to stop foreground service: ${e.message}")
            }
        }
    }
}