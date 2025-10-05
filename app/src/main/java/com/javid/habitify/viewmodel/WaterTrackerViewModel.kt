package com.javid.habitify.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.javid.habitify.model.WaterLog
import com.javid.habitify.utils.PrefsManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.javid.habitify.services.WaterReminderService
import java.text.SimpleDateFormat
import java.util.*

class WaterTrackerViewModel : ViewModel() {

    private val gson = Gson()
    private var prefsManager: PrefsManager? = null

    private val _dailyGoal = MutableLiveData<Int>()
    private val _currentIntake = MutableLiveData<Int>()
    private val _todayLogs = MutableLiveData<List<WaterLog>>()
    private val _isGoalCompleted = MutableLiveData<Boolean>()

    val dailyGoal: LiveData<Int> get() = _dailyGoal
    val currentIntake: LiveData<Int> get() = _currentIntake
    val todayLogs: LiveData<List<WaterLog>> get() = _todayLogs
    val isGoalCompleted: LiveData<Boolean> get() = _isGoalCompleted

    fun initialize(prefsManager: PrefsManager) {
        this.prefsManager = prefsManager
        loadData()
    }

    private fun loadData() {
        val today = WaterLog.getTodayDate()

        val savedGoal = prefsManager?.getUserPreference("water_daily_goal", "2000")
        _dailyGoal.value = savedGoal?.toIntOrNull() ?: 2000

        val savedLogsJson = prefsManager?.getUserPreference("water_logs_$today", "")
        val logs = if (savedLogsJson.isNullOrEmpty()) {
            emptyList()
        } else {
            try {
                val type = object : TypeToken<List<WaterLog>>() {}.type
                gson.fromJson<List<WaterLog>>(savedLogsJson, type) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }

        _todayLogs.value = logs
        _currentIntake.value = logs.sumOf { it.amount }
        _isGoalCompleted.value = (_currentIntake.value ?: 0) >= (_dailyGoal.value ?: 0)

        // Check if we need to reset data (new day)
        if (shouldResetData()) {
            clearTodayData()
        }
    }

    fun setDailyGoal(goal: Int) {
        if (goal > 0) {
            _dailyGoal.value = goal
            prefsManager?.setUserPreference("water_daily_goal", goal.toString())
            checkGoalCompletion()
        }
    }

    fun addWaterLog(amount: Int) {
        val currentLogs = _todayLogs.value?.toMutableList() ?: mutableListOf()
        val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

        val newLog = WaterLog(amount = amount, time = currentTime)
        currentLogs.add(newLog)

        _todayLogs.value = currentLogs
        _currentIntake.value = currentLogs.sumOf { it.amount }

        saveTodayLogs()
        checkGoalCompletion()
    }

    private fun checkGoalCompletion() {
        val completed = (_currentIntake.value ?: 0) >= (_dailyGoal.value ?: 0)
        _isGoalCompleted.value = completed

        if (completed){
            prefsManager?.setUserPreference("last_goal_completion", WaterLog.getTodayDate())
        }
    }

    private fun saveTodayLogs() {
        val today = WaterLog.getTodayDate()
        val logsJson = gson.toJson(_todayLogs.value)
        prefsManager?.setUserPreference("water_logs_$today", logsJson)
    }

    fun clearTodayData() {
        val today = WaterLog.getTodayDate()
        _todayLogs.value = emptyList()
        _currentIntake.value = 0
        _isGoalCompleted.value = false

        prefsManager?.removeUserPreference("water_logs_$today")
        prefsManager?.removeUserPreference("last_goal_completion")
    }

    private fun shouldResetData(): Boolean {
        val lastCompletion = prefsManager?.getUserPreference("last_goal_completion", "")
        val today = WaterLog.getTodayDate()
        return lastCompletion != today
    }

    fun getTodayDate(): String {
        return WaterLog.getTodayDate()
    }
    fun getCurrentProgress(): Float {
        val goal = _dailyGoal.value ?: 2000
        val intake = _currentIntake.value ?: 0
        return if (goal > 0) intake.toFloat() / goal else 0f
    }
    fun checkAndScheduleReminders(context : Context) {
        val remaining = (dailyGoal.value ?: 2000) - (currentIntake.value ?: 0)

        if (remaining > 0) {
            WaterReminderService.scheduleReminders(context)
            Log.d("WaterTracker", "⏰ Scheduled hourly reminders - Remaining: $remaining ml")
        } else {
            WaterReminderService.cancelAllReminders(context)
            Log.d("WaterTracker", "✅ Goal completed - Cancelled reminders")
        }
    }

    fun cancelReminders(context : Context) {
        WaterReminderService.cancelAllReminders(context)
        Log.d("WaterTracker", "❌ Manually cancelled water reminders")
    }

    fun areRemindersActive(context : Context): Boolean {
        return WaterReminderService.areRemindersActive(context)
    }
}