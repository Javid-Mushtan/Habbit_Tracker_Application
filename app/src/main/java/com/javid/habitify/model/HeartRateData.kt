package com.javid.habitify.model

data class HeartRateData(
    val currentBPM: Int = 0,
    val isMeasuring: Boolean = false,
    val measurementTime: Long = 0,
    val measurementStatus: String = "Ready to measure",
    val heartRateHistory: List<Int> = emptyList(),
    val averageBPM: Int = 0,
    val minBPM: Int = 0,
    val maxBPM: Int = 0
)