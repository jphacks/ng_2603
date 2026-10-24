package com.example.myapplication.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class AppUsageInfo(
    val packageName: String,
    val appName: String,
    val usedTimeMillis: Long,
    val limitTimeMillis: Long,
    val isRestricted: Boolean
) {
    val usedMinutes: Int
        get() = (usedTimeMillis / (1000 * 60)).toInt()

    val usagePercentage: Float
        get() = if (limitTimeMillis > 0) {
            (usedTimeMillis.toFloat() / limitTimeMillis).coerceIn(0f, 2f)
        } else 0f
}
