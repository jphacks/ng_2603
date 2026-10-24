package com.example.myapplication.ui.disappointment

import androidx.compose.runtime.Immutable
import com.example.myapplication.domain.model.AppUsageInfo
import com.example.myapplication.domain.model.DisappointmentLevel
import com.example.myapplication.domain.model.DisappointmentMessage
import com.example.myapplication.domain.model.UserRole
import com.example.myapplication.domain.usecase.MathProblem

@Immutable
data class MainUiState(
    val role: UserRole = UserRole.INDIVIDUAL,
    val disappointmentLevel: DisappointmentLevel = DisappointmentLevel.LEVEL_0_SERENE,
    val message: DisappointmentMessage = DisappointmentMessage.getDefault(
        DisappointmentLevel.LEVEL_0_SERENE,
        UserRole.INDIVIDUAL
    ),
    val totalUsedMillis: Long = 0L,
    val limitMillis: Long = 60L * 60 * 1000,
    val usagePercentage: Float = 0f,
    val topApps: List<AppUsageInfo> = emptyList(),
    val hasUsagePermission: Boolean = false,
    val hasOverlayPermission: Boolean = false,
    val showWarningBanner: Boolean = false,
    val isMonitoringActive: Boolean = true,
    val isUnlockTaskVisible: Boolean = false,
    val currentMathProblem: MathProblem? = null
) {
    val totalUsedMinutes: Int
        get() = (totalUsedMillis / (1000 * 60)).toInt()

    val limitMinutes: Int
        get() = (limitMillis / (1000 * 60)).toInt()
}
