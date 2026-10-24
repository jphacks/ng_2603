package com.example.myapplication.ui.disappointment

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repository.ScreenTimeRepositoryImpl
import com.example.myapplication.data.repository.SettingsRepositoryImpl
import com.example.myapplication.domain.model.DisappointmentLevel
import com.example.myapplication.domain.model.UserRole
import com.example.myapplication.domain.repository.ScreenTimeRepository
import com.example.myapplication.domain.repository.SettingsRepository
import com.example.myapplication.domain.usecase.EvaluateDisappointmentUseCase
import com.example.myapplication.domain.usecase.MathProblem
import com.example.myapplication.domain.usecase.UnlockFrictionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private data class SettingsSnapshot(
    val role: UserRole,
    val limitMillis: Long,
    val customMessage: String?,
    val isMonitoring: Boolean
)

class MainViewModel(
    private val screenTimeRepository: ScreenTimeRepository,
    private val settingsRepository: SettingsRepository,
    private val evaluateDisappointmentUseCase: EvaluateDisappointmentUseCase,
    private val unlockFrictionUseCase: UnlockFrictionUseCase
) : ViewModel() {

    private val isUnlockDialogVisible = MutableStateFlow(false)
    private val currentProblem = MutableStateFlow<MathProblem?>(null)

    // 設定系のFlowを合成
    private val settingsSnapshotFlow = combine(
        settingsRepository.userRoleFlow,
        settingsRepository.limitTimeMillisFlow,
        settingsRepository.customMessageFlow,
        settingsRepository.isMonitoringActiveFlow
    ) { role, limitMillis, customMessage, isMonitoring ->
        SettingsSnapshot(role, limitMillis, customMessage, isMonitoring)
    }

    // 監視データと合成し、単一のUiStateにマッピング
    val uiState: StateFlow<MainUiState> = combine(
        settingsSnapshotFlow,
        screenTimeRepository.observeTotalRestrictedUsageMillis(),
        isUnlockDialogVisible,
        currentProblem
    ) { settings, usedMillis, isDialogShown, problem ->
        val evaluation = evaluateDisappointmentUseCase(
            totalUsedMillis = usedMillis,
            limitMillis = settings.limitMillis,
            userRole = settings.role,
            customMessageOverride = settings.customMessage
        )

        val usageList = screenTimeRepository.getTodayAppUsageList()
        val hasUsagePerm = screenTimeRepository.hasUsageStatsPermission()
        val hasOverlayPerm = screenTimeRepository.hasOverlayPermission()

        MainUiState(
            role = settings.role,
            disappointmentLevel = evaluation.level,
            message = evaluation.message,
            totalUsedMillis = usedMillis,
            limitMillis = settings.limitMillis,
            usagePercentage = evaluation.usagePercentage,
            topApps = usageList,
            hasUsagePermission = hasUsagePerm,
            hasOverlayPermission = hasOverlayPerm,
            showWarningBanner = evaluation.level >= DisappointmentLevel.LEVEL_1_SUSPICIOUS,
            isMonitoringActive = settings.isMonitoring,
            isUnlockTaskVisible = isDialogShown,
            currentMathProblem = problem
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainUiState()
    )

    /**
     * 動作モード（個人 / 親 / 子）の切り替え
     */
    fun switchRole(newRole: UserRole) {
        viewModelScope.launch {
            settingsRepository.setUserRole(newRole)
        }
    }

    /**
     * 監視のON/OFF切り替え
     */
    fun toggleMonitoring(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setMonitoringActive(enabled)
        }
    }

    /**
     * 親モード専用：即時発動ボタン（嫌がらせオーバーレイを強制発動）
     */
    fun triggerImmediateIntervention() {
        viewModelScope.launch {
            settingsRepository.setLimitTimeMillis(1) // 制限時間を強制的に最小化して即時発動
        }
    }

    /**
     * 甘え防止タスク（計算問題）の開始
     */
    fun startUnlockChallenge() {
        currentProblem.value = unlockFrictionUseCase.generateMathProblem()
        isUnlockDialogVisible.value = true
    }

    fun dismissUnlockDialog() {
        isUnlockDialogVisible.value = false
        currentProblem.value = null
    }

    /**
     * 解除成功時の処理（制限時間を一時延長）
     */
    fun onFrictionTaskSucceeded() {
        viewModelScope.launch {
            // 一時的に制限を30分延長
            val currentLimit = uiState.value.limitMillis
            settingsRepository.setLimitTimeMillis(currentLimit + 30 * 60 * 1000L)
            dismissUnlockDialog()
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val settingsRepo = SettingsRepositoryImpl(context.applicationContext)
            val screenTimeRepo = ScreenTimeRepositoryImpl(context.applicationContext, settingsRepo)
            val evalUseCase = EvaluateDisappointmentUseCase()
            val frictionUseCase = UnlockFrictionUseCase()
            return MainViewModel(
                screenTimeRepository = screenTimeRepo,
                settingsRepository = settingsRepo,
                evaluateDisappointmentUseCase = evalUseCase,
                unlockFrictionUseCase = frictionUseCase
            ) as T
        }
    }
}
