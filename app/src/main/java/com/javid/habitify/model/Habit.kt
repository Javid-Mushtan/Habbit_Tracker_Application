package com.javid.habitify.model

data class Habit(
    val name: String,
    val schedule: String,
    var completed: Boolean = false,
    val id: Long = System.currentTimeMillis(),
    val reminderTime: String? = null,
    val isReminderEnabled: Boolean = false,
    val date: String = ""
)