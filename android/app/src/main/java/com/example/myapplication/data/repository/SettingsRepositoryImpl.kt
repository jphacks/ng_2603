package com.example.myapplication.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.myapplication.domain.model.UserRole
import com.example.myapplication.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepositoryImpl(
    context: Context
) : SettingsRepository {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("guardian_settings_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_USER_ROLE = "user_role"
        private const val KEY_LIMIT_TIME_MILLIS = "limit_time_millis"
        private const val KEY_CUSTOM_MESSAGE = "custom_message"
        private const val KEY_BLACKLISTED_PACKAGES = "blacklisted_packages"
        private const val KEY_IS_MONITORING_ACTIVE = "is_monitoring_active"

        private val DEFAULT_BLACKLIST = setOf(
            "com.google.android.youtube",
            "com.instagram.android",
            "com.twitter.android",
            "com.zhiliaoapp.musically" // TikTok
        )
    }

    private val _userRoleFlow = MutableStateFlow(loadUserRole())
    override val userRoleFlow: Flow<UserRole> = _userRoleFlow.asStateFlow()

    private val _limitTimeMillisFlow = MutableStateFlow(loadLimitMillis())
    override val limitTimeMillisFlow: Flow<Long> = _limitTimeMillisFlow.asStateFlow()

    private val _customMessageFlow = MutableStateFlow(loadCustomMessage())
    override val customMessageFlow: Flow<String?> = _customMessageFlow.asStateFlow()

    private val _blacklistedPackagesFlow = MutableStateFlow(loadBlacklistedPackages())
    override val blacklistedPackagesFlow: Flow<Set<String>> = _blacklistedPackagesFlow.asStateFlow()

    private val _isMonitoringActiveFlow = MutableStateFlow(loadIsMonitoringActive())
    override val isMonitoringActiveFlow: Flow<Boolean> = _isMonitoringActiveFlow.asStateFlow()

    private fun loadUserRole(): UserRole {
        val name = prefs.getString(KEY_USER_ROLE, UserRole.INDIVIDUAL.name)
        return try {
            UserRole.valueOf(name ?: UserRole.INDIVIDUAL.name)
        } catch (_: Exception) {
            UserRole.INDIVIDUAL
        }
    }

    private fun loadLimitMillis(): Long {
        // デフォルト: 60分 (60 * 60 * 1000)
        return prefs.getLong(KEY_LIMIT_TIME_MILLIS, 60L * 60 * 1000)
    }

    private fun loadCustomMessage(): String? {
        return prefs.getString(KEY_CUSTOM_MESSAGE, null)
    }

    private fun loadBlacklistedPackages(): Set<String> {
        return prefs.getStringSet(KEY_BLACKLISTED_PACKAGES, DEFAULT_BLACKLIST) ?: DEFAULT_BLACKLIST
    }

    private fun loadIsMonitoringActive(): Boolean {
        return prefs.getBoolean(KEY_IS_MONITORING_ACTIVE, true)
    }

    override suspend fun setUserRole(role: UserRole) {
        prefs.edit().putString(KEY_USER_ROLE, role.name).apply()
        _userRoleFlow.value = role
    }

    override suspend fun setLimitTimeMillis(limitMillis: Long) {
        prefs.edit().putLong(KEY_LIMIT_TIME_MILLIS, limitMillis).apply()
        _limitTimeMillisFlow.value = limitMillis
    }

    override suspend fun setCustomMessage(message: String?) {
        if (message != null) {
            prefs.edit().putString(KEY_CUSTOM_MESSAGE, message).apply()
        } else {
            prefs.edit().remove(KEY_CUSTOM_MESSAGE).apply()
        }
        _customMessageFlow.value = message
    }

    override suspend fun updateBlacklistedPackages(packages: Set<String>) {
        prefs.edit().putStringSet(KEY_BLACKLISTED_PACKAGES, packages).apply()
        _blacklistedPackagesFlow.value = packages
    }

    override suspend fun setMonitoringActive(active: Boolean) {
        prefs.edit().putBoolean(KEY_IS_MONITORING_ACTIVE, active).apply()
        _isMonitoringActiveFlow.value = active
    }
}
