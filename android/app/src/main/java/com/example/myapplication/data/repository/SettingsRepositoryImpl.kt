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
        private const val KEY_WHITELIST_PACKAGES = "whitelist_packages"
        private const val KEY_IS_MONITORING_ACTIVE = "is_monitoring_active"
        private const val KEY_MONITORING_START_TIME = "monitoring_start_time"
        private const val KEY_PERSONALITY = "okan_personality"
        private const val KEY_MONITORING_MODE = "monitoring_mode"
        private const val KEY_VIOLATION_COUNT = "violation_count"

        private val DEFAULT_BLACKLIST = setOf(
            "com.google.android.youtube",
            "com.instagram.android",
            "com.twitter.android",
            "com.zhiliaoapp.musically" // TikTok
        )

        private val DEFAULT_WHITELIST = setOf(
            "com.android.calculator2",
            "com.google.android.calculator",
            "com.android.calendar",
            "com.google.android.calendar"
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

    private val _whitelistPackagesFlow = MutableStateFlow(loadWhitelistPackages())
    override val whitelistPackagesFlow: Flow<Set<String>> = _whitelistPackagesFlow.asStateFlow()

    private val _isMonitoringActiveFlow = MutableStateFlow(loadIsMonitoringActive())
    override val isMonitoringActiveFlow: Flow<Boolean> = _isMonitoringActiveFlow.asStateFlow()

    private val _monitoringStartTimeFlow = MutableStateFlow(loadMonitoringStartTime())
    override val monitoringStartTimeFlow: Flow<Long> = _monitoringStartTimeFlow.asStateFlow()

    private val _personalityFlow = MutableStateFlow(loadPersonality())
    override val personalityFlow: Flow<String> = _personalityFlow.asStateFlow()

    private val _monitoringModeFlow = MutableStateFlow(loadMonitoringMode())
    override val monitoringModeFlow: Flow<String> = _monitoringModeFlow.asStateFlow()

    private val _violationCountFlow = MutableStateFlow(loadViolationCount())
    override val violationCountFlow: Flow<Int> = _violationCountFlow.asStateFlow()

    private fun loadUserRole(): UserRole {
        val name = prefs.getString(KEY_USER_ROLE, UserRole.INDIVIDUAL.name)
        return try {
            UserRole.valueOf(name ?: UserRole.INDIVIDUAL.name)
        } catch (_: Exception) {
            UserRole.INDIVIDUAL
        }
    }

    private fun loadLimitMillis(): Long {
        return prefs.getLong(KEY_LIMIT_TIME_MILLIS, 30L * 60 * 1000)
    }

    private fun loadCustomMessage(): String? {
        return prefs.getString(KEY_CUSTOM_MESSAGE, "")
    }

    private fun loadBlacklistedPackages(): Set<String> {
        return prefs.getStringSet(KEY_BLACKLISTED_PACKAGES, DEFAULT_BLACKLIST) ?: DEFAULT_BLACKLIST
    }

    private fun loadWhitelistPackages(): Set<String> {
        return prefs.getStringSet(KEY_WHITELIST_PACKAGES, DEFAULT_WHITELIST) ?: DEFAULT_WHITELIST
    }

    private fun loadIsMonitoringActive(): Boolean {
        return prefs.getBoolean(KEY_IS_MONITORING_ACTIVE, false)
    }

    private fun loadMonitoringStartTime(): Long {
        return prefs.getLong(KEY_MONITORING_START_TIME, 0L)
    }

    private fun loadPersonality(): String {
        return prefs.getString(KEY_PERSONALITY, "STANDARD") ?: "STANDARD"
    }

    private fun loadMonitoringMode(): String {
        return prefs.getString(KEY_MONITORING_MODE, "AFTER_LIMIT") ?: "AFTER_LIMIT"
    }

    private fun loadViolationCount(): Int {
        return prefs.getInt(KEY_VIOLATION_COUNT, 0)
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
        prefs.edit().putString(KEY_CUSTOM_MESSAGE, message).apply()
        _customMessageFlow.value = message
    }

    override suspend fun updateBlacklistedPackages(packages: Set<String>) {
        prefs.edit().putStringSet(KEY_BLACKLISTED_PACKAGES, packages).apply()
        _blacklistedPackagesFlow.value = packages
    }

    override suspend fun updateWhitelistPackages(packages: Set<String>) {
        prefs.edit().putStringSet(KEY_WHITELIST_PACKAGES, packages).apply()
        _whitelistPackagesFlow.value = packages
    }

    override suspend fun setMonitoringActive(active: Boolean) {
        prefs.edit().putBoolean(KEY_IS_MONITORING_ACTIVE, active).commit()
        _isMonitoringActiveFlow.value = active
    }

    override suspend fun setMonitoringStartTime(startTimeMillis: Long) {
        prefs.edit().putLong(KEY_MONITORING_START_TIME, startTimeMillis).commit()
        _monitoringStartTimeFlow.value = startTimeMillis
    }

    override suspend fun setPersonality(personality: String) {
        prefs.edit().putString(KEY_PERSONALITY, personality).apply()
        _personalityFlow.value = personality
    }

    override suspend fun setMonitoringMode(mode: String) {
        prefs.edit().putString(KEY_MONITORING_MODE, mode).apply()
        _monitoringModeFlow.value = mode
    }

    override suspend fun incrementViolationCount(): Int {
        val next = _violationCountFlow.value + 1
        prefs.edit().putInt(KEY_VIOLATION_COUNT, next).apply()
        _violationCountFlow.value = next
        return next
    }

    override suspend fun resetViolationCount() {
        prefs.edit().putInt(KEY_VIOLATION_COUNT, 0).apply()
        _violationCountFlow.value = 0
    }
}
