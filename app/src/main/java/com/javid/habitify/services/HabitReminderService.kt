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
        startForeground(NOTIFICATION_ID,createNotification())
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotification(): Notification {

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Habbit Reminder Service",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Keeps habit reminders running"
                setShowBadge(true)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Habitify Running")
            .setContentText("Keeping your reminders active")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(false)
            .build()
    }

    companion object {
        private const val CHANNEL_ID = "habit_service_channel"
        private const val NOTIFICATION_ID = 1001

        fun startService(context: Context) {
            val intent = Intent(context, HabitReminderService::class.java)
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context : Context){
            val intent = Intent(context, HabitReminderService::class.java)
            context.stopService(intent)
        }
    }
}