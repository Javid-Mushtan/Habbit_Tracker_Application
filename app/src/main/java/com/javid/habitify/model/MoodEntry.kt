package com.javid.habitify.model

import java.text.SimpleDateFormat
import java.util.*

data class MoodEntry(
    val id: Long = System.currentTimeMillis(),
    val mood: String,
    val moodEmoji: String,
    val note: String = "",
    val date: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
    val time: String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
    val timestamp: Long = System.currentTimeMillis()
)