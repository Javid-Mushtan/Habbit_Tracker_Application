package com.javid.habitify.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.javid.habitify.R
import com.javid.habitify.utils.PrefsManager
import com.javid.habitify.model.WaterLog
import com.javid.habitify.receivers.WaterResetReceiver
import java.util.*

class WaterResetService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("WaterResetService", "🚀 Starting daily water reset service")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
        startForeground(NOTIFICATION_ID, createNotification())

        performDailyReset()
        stopSelf()

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun performDailyReset() {
        try {
            val prefsManager = PrefsManager(this)
            val today = WaterLog.getTodayDate()

            Log.d("WaterResetService", "🔄 Performing daily reset for date: $today")

            val allPreferences = prefsManager.getAllPreferences()
            var clearedCount = 0

            allPreferences.forEach { (key, value) ->
                if (key.startsWith("water_logs_") && !key.endsWith(today)) {
                    prefsManager.removePreference(key)
                    clearedCount++
                    Log.d("WaterResetService", "🗑️ Cleared old water data: $key")
                }
            }

            val lastCompletion = prefsManager.getUserPreference("last_goal_completion", "")
            if (lastCompletion.isNotEmpty() && lastCompletion != today) {
                prefsManager.removeUserPreference("last_goal_completion")
                Log.d("WaterResetService", "🔄 Reset goal completion flag")
            }

            Log.d("WaterResetService", "✅ Daily water reset completed. Cleared $clearedCount old entries")

        } catch (e: Exception) {
            Log.e("WaterResetService", "❌ Error in daily reset: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Water Tracker Service",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Handles daily water tracking resets"
                setShowBadge(true)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Water Tracker")
            .setContentText("Resetting daily water data...")
            .setSmallIcon(R.drawable.ic_water_drop)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
            .setAutoCancel(true)
            .build()
    }

    companion object {
        private const val CHANNEL_ID = "water_reset_service_channel"
        private const val NOTIFICATION_ID = 1002

        fun startService(context: Context) {
            try {
                val intent = Intent(context, WaterResetService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
                Log.d("WaterResetService", "🏁 Service started successfully")
            } catch (e: Exception) {
                Log.e("WaterResetService", "❌ Failed to start service: ${e.message}")
            }
        }

        fun stopService(context: Context) {
            try {
                val intent = Intent(context, WaterResetService::class.java)
                context.stopService(intent)
                Log.d("WaterResetService", "🛑 Service stopped")
            } catch (e: Exception) {
                Log.e("WaterResetService", "❌ Failed to stop service: ${e.message}")
            }
        }

        fun scheduleDailyReset(context: Context) {
            try {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val intent = Intent(context, WaterResetReceiver::class.java)

                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val calendar = Calendar.getInstance().apply {
                    timeInMillis = System.currentTimeMillis()
                    add(Calendar.DAY_OF_YEAR, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                Log.d("WaterResetService", "📅 Scheduling next reset for: ${calendar.time}")

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                }

                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
                )

                Log.d("WaterResetService", "✅ Daily reset scheduled successfully")

            } catch (e: SecurityException) {
                Log.e("WaterResetService", "🔒 Security exception: ${e.message}")
            } catch (e: Exception) {
                Log.e("WaterResetService", "❌ Failed to schedule daily reset: ${e.message}")
            }
        }

        fun cancelDailyReset(context: Context) {
            try {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val intent = Intent(context, WaterResetReceiver::class.java)

                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
                )

                pendingIntent?.let {
                    alarmManager.cancel(it)
                    it.cancel()
                    Log.d("WaterResetService", "❌ Daily reset cancelled")
                }
            } catch (e: Exception) {
                Log.e("WaterResetService", "❌ Failed to cancel daily reset: ${e.message}")
            }
        }
    }
}