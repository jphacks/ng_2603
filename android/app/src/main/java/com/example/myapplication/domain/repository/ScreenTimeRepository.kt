package com.example.myapplication.domain.repository

import com.example.myapplication.domain.model.AppUsageInfo
import kotlinx.coroutines.flow.Flow

data class HourlyScreenTime(
    val hour: Int,
    val totalMinutes: Int,
    val distractionMinutes: Int
)

interface ScreenTimeRepository {
    fun hasUsageStatsPermission(): Boolean
    fun hasOverlayPermission(): Boolean
    suspend fun getTodayAppUsageList(): List<AppUsageInfo>
    suspend fun getHourlyScreenTime(): List<HourlyScreenTime>
    suspend fun getCurrentForegroundPackage(): String?
    fun observeTotalRestrictedUsageMillis(): Flow<Long>
}
