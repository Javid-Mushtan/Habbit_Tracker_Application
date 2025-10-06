package com.javid.habitify.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.javid.habitify.R
import com.javid.habitify.model.HeartRateData
import com.javid.habitify.model.HeartRateStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Date

class HeartRateViewModel : ViewModel() {

    private val _heartRateData = MutableStateFlow(HeartRateData())
    val heartRateData: StateFlow<HeartRateData> = _heartRateData

    private val _statusMessage = MutableLiveData<String>()
    val statusMessage: LiveData<String> = _statusMessage

    private val _heartRateStatus = MutableLiveData(HeartRateStatus.READY)
    val heartRateStatus: LiveData<HeartRateStatus> = _heartRateStatus

    fun startMeasurement() {
        updateData { current ->
            current.copy(
                isMeasuring = true,
                currentBPM = 0,
                measurementStatus = "Place finger on sensor...",
                measurementTime = System.currentTimeMillis()
            )
        }
        _heartRateStatus.value = HeartRateStatus.MEASURING
        _statusMessage.value = "Heart rate measurement started"
    }

    fun stopMeasurement() {
        updateData { current ->
            current.copy(
                isMeasuring = false,
                measurementStatus = "Measurement stopped"
            )
        }
        _heartRateStatus.value = HeartRateStatus.READY
        _statusMessage.value = "Measurement stopped"
    }

    fun updateHeartRate(bpm: Int) {
        updateData { current ->
            val newHistory = current.heartRateHistory + bpm
            val average = if (newHistory.isNotEmpty()) newHistory.average().toInt() else 0
            val min = newHistory.minOrNull() ?: 0
            val max = newHistory.maxOrNull() ?: 0

            current.copy(
                currentBPM = bpm,
                heartRateHistory = newHistory.takeLast(10), // Keep last 10 readings
                averageBPM = average,
                minBPM = min,
                maxBPM = max,
                measurementStatus = "Heart rate: $bpm BPM",
                measurementTime = System.currentTimeMillis()
            )
        }
    }

    fun saveMeasurement() {
        val currentData = _heartRateData.value
        if (currentData.currentBPM > 0) {
            _heartRateStatus.value = HeartRateStatus.FINISHED
            _statusMessage.value = "Heart rate ${currentData.currentBPM} BPM saved"

            saveToHistory(currentData.currentBPM)
        }
    }

    fun resetMeasurement() {
        updateData {
            HeartRateData(
                measurementStatus = "Ready to measure"
            )
        }
        _heartRateStatus.value = HeartRateStatus.READY
        _statusMessage.value = "Measurement reset"
    }

    fun setError(errorMessage: String) {
        updateData { current ->
            current.copy(
                isMeasuring = false,
                measurementStatus = errorMessage
            )
        }
        _heartRateStatus.value = HeartRateStatus.ERROR
        _statusMessage.value = errorMessage
    }

    private fun saveToHistory(bpm: Int) {
        updateData { current ->
            val newHistory = current.heartRateHistory + bpm
            val average = if (newHistory.isNotEmpty()) newHistory.average().toInt() else 0
            val min = newHistory.minOrNull() ?: 0
            val max = newHistory.maxOrNull() ?: 0

            current.copy(
                heartRateHistory = newHistory.takeLast(20),
                averageBPM = average,
                minBPM = min,
                maxBPM = max
            )
        }
    }

    private fun updateData(update: (HeartRateData) -> HeartRateData) {
        _heartRateData.value = update(_heartRateData.value)
    }

    fun getHeartRateZone(bpm: Int): String {
        return when (bpm) {
            in 0..60 -> "Resting"
            in 61..100 -> "Normal"
            in 101..140 -> "Exercise"
            in 141..170 -> "High Intensity"
            else -> "Very High"
        }
    }

    fun getStatusColor(bpm: Int): Int {
        return when (bpm) {
            in 0..60 -> R.color.heart_rate_resting
            in 61..100 -> R.color.heart_rate_normal
            in 101..140 -> R.color.heart_rate_exercise
            in 141..170 -> R.color.heart_rate_high
            else -> R.color.heart_rate_very_high
        }
    }
}