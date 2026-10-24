package com.example.myapplication.domain.repository

import com.example.myapplication.domain.model.UserRole
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val userRoleFlow: Flow<UserRole>
    val limitTimeMillisFlow: Flow<Long>
    val customMessageFlow: Flow<String?>
    val blacklistedPackagesFlow: Flow<Set<String>>
    val isMonitoringActiveFlow: Flow<Boolean>
    val monitoringStartTimeFlow: Flow<Long>
    val personalityFlow: Flow<String>

    suspend fun setUserRole(role: UserRole)
    suspend fun setLimitTimeMillis(limitMillis: Long)
    suspend fun setCustomMessage(message: String?)
    suspend fun updateBlacklistedPackages(packages: Set<String>)
    suspend fun setMonitoringActive(active: Boolean)
    suspend fun setMonitoringStartTime(startTimeMillis: Long)
    suspend fun setPersonality(personality: String)
}
