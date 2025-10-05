package com.javid.habitify.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class WaterLog(
    val id: Long = System.currentTimeMillis(),
    val amount : Int,
    val time: String,
    val date: String = SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).format(Date())
) {
    companion object {
        fun getTodayDate() : String {
            return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        }
    }
}
