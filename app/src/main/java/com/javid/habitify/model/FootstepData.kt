package com.javid.habitify.model

data class FootstepData(
    val stepCount: Int = 0,
    val dailyGoal: Int = 10000,
    val isCounting: Boolean = false,
    val startTime: Long = 0,
    val totalActiveTime: Long = 0,
    val lastSensorStepCount: Int = 0
)