package com.javid.habitify.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.javid.habitify.services.WaterResetService

class WaterResetReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("WaterResetReceiver", "🔔 Daily reset receiver triggered")

        try {
            WaterResetService.startService(context)

            WaterResetService.scheduleDailyReset(context)

            Log.d("WaterResetReceiver", "✅ Reset processed and rescheduled")

        } catch (e: Exception) {
            Log.e("WaterResetReceiver", "❌ Error in reset receiver: ${e.message}")
            e.printStackTrace()
        }
    }
}