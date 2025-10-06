package com.javid.habitify.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.javid.habitify.model.FootstepData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FootstepViewModel : ViewModel() {

    private val _footstepData = MutableStateFlow(FootstepData())
    val footstepData: StateFlow<FootstepData> = _footstepData

    private val _statusMessage = MutableLiveData<String>()
    val statusMessage: LiveData<String> = _statusMessage

    val distance: Double
        get() = calculateDistance(_footstepData.value.stepCount)

    val calories: Int
        get() = calculateCalories(_footstepData.value.stepCount)

    val activeMinutes: Long
        get() = calculateActiveTime()

    val goalProgress: Float
        get() = _footstepData.value.stepCount.toFloat() / _footstepData.value.dailyGoal

    fun setDailyGoal(goal: Int) {
        if (goal > 0) {
            updateData { it.copy(dailyGoal = goal) }
            _statusMessage.value = "Daily goal set to $goal steps"
        }
    }

    fun startCounting() {
        if (!_footstepData.value.isCounting) {
            updateData {
                it.copy(
                    isCounting = true,
                    startTime = System.currentTimeMillis()
                )
            }
            _statusMessage.value = "Step counting started"
        }
    }

    fun stopCounting() {
        if (_footstepData.value.isCounting) {
            val currentData = _footstepData.value
            val activeTime = currentData.totalActiveTime +
                    (System.currentTimeMillis() - currentData.startTime)

            updateData {
                it.copy(
                    isCounting = false,
                    totalActiveTime = activeTime,
                    startTime = 0
                )
            }
            _statusMessage.value = "Step counting stopped"
        }
    }

    fun resetData() {
        updateData {
            FootstepData(
                dailyGoal = _footstepData.value.dailyGoal,
                lastSensorStepCount = _footstepData.value.lastSensorStepCount
            )
        }
        _statusMessage.value = "Step count reset"
    }

    fun updateStepCount(newStepCount: Int, fromSensor: Boolean = false) {
        updateData { current ->
            if (fromSensor) {
                if (current.lastSensorStepCount == 0) {
                    current.copy(
                        stepCount = newStepCount,
                        lastSensorStepCount = newStepCount
                    )
                } else {
                    val stepsSinceLast = newStepCount - current.lastSensorStepCount
                    if (stepsSinceLast > 0) {
                        current.copy(
                            stepCount = current.stepCount + stepsSinceLast,
                            lastSensorStepCount = newStepCount
                        )
                    } else {
                        current.copy(lastSensorStepCount = newStepCount)
                    }
                }
            } else {
                current.copy(stepCount = newStepCount)
            }
        }
    }

    fun incrementStep() {
        updateData { it.copy(stepCount = it.stepCount + 1) }
    }

    fun setLastSensorStepCount(count: Int) {
        updateData { it.copy(lastSensorStepCount = count) }
    }

    private fun updateData(update: (FootstepData) -> FootstepData) {
        _footstepData.value = update(_footstepData.value)
    }

    private fun calculateDistance(steps: Int): Double {
        val distanceMeters = steps * 0.762
        return distanceMeters / 1000
    }

    private fun calculateCalories(steps: Int): Int {
        return (steps * 0.04).toInt()
    }

    private fun calculateActiveTime(): Long {
        val currentData = _footstepData.value
        val totalTime = if (currentData.isCounting) {
            currentData.totalActiveTime + (System.currentTimeMillis() - currentData.startTime)
        } else {
            currentData.totalActiveTime
        }
        return totalTime / 60000
    }

    fun getStatusMessage(): String {
        val progress = goalProgress
        return when {
            _footstepData.value.stepCount == 0 -> "Let's start walking!"
            progress < 0.25 -> "Great start! Keep going!"
            progress < 0.5 -> "You're doing amazing!"
            progress < 0.75 -> "More than halfway there!"
            progress < 1.0 -> "Almost at your goal!"
            else -> "Goal achieved! 🎉"
        }
    }
}