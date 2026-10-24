package com.example.myapplication.domain.repository

import com.example.myapplication.domain.model.AppUsageInfo
import kotlinx.coroutines.flow.Flow

interface ScreenTimeRepository {
    fun hasUsageStatsPermission(): Boolean
    fun hasOverlayPermission(): Boolean
    suspend fun getTodayAppUsageList(): List<AppUsageInfo>
    suspend fun getCurrentForegroundPackage(): String?
    fun observeTotalRestrictedUsageMillis(): Flow<Long>
}
