package com.example.myapplication.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.example.myapplication.data.repository.ScreenTimeRepositoryImpl
import com.example.myapplication.data.repository.SettingsRepositoryImpl
import com.example.myapplication.domain.model.DisappointmentLevel
import com.example.myapplication.domain.repository.ScreenTimeRepository
import com.example.myapplication.domain.repository.SettingsRepository
import com.example.myapplication.domain.usecase.EvaluateDisappointmentUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * バックグラウンド常時実行・アプリ監視サービス
 * UsageStatsManagerによるフォアグラウンドアプリ検知、
 * 段階的ペナルティ、オーバーレイ制御、連続プッシュ通知を司る。
 */
class AppMonitoringService : Service() {

    private lateinit var screenTimeRepository: ScreenTimeRepository
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var evaluateDisappointmentUseCase: EvaluateDisappointmentUseCase
    private lateinit var notificationHelper: NotificationHelper

    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private var monitoringJob: Job? = null
    private var lastContinuousNotificationTime: Long = 0
    private var isOverlayCurrentlyShown: Boolean = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        settingsRepository = SettingsRepositoryImpl(applicationContext)
        screenTimeRepository = ScreenTimeRepositoryImpl(applicationContext, settingsRepository)
        evaluateDisappointmentUseCase = EvaluateDisappointmentUseCase()
        notificationHelper = NotificationHelper(applicationContext)

        val initialNotification = notificationHelper.buildMonitoringNotification("利用時間を安全に監視中")
        startForeground(NotificationHelper.NOTIFICATION_ID_FOREGROUND_SERVICE, initialNotification)

        startMonitoringLoop()
    }

    private fun startMonitoringLoop() {
        monitoringJob?.cancel()
        monitoringJob = serviceScope.launch {
            while (isActive) {
                try {
                    val isActiveMonitoring = settingsRepository.isMonitoringActiveFlow.first()
                    if (!isActiveMonitoring) {
                        dismissAllInterventions()
                        delay(5000)
                        continue
                    }

                    val currentForegroundApp = screenTimeRepository.getCurrentForegroundPackage()
                    val blacklisted = settingsRepository.blacklistedPackagesFlow.first()
                    val userRole = settingsRepository.userRoleFlow.first()
                    val limitMillis = settingsRepository.limitTimeMillisFlow.first()
                    val customMessage = settingsRepository.customMessageFlow.first()

                    // 自アプリやホーム画面、ホワイトリストの場合は即座に介入を全解除
                    val isRestrictedAppRunning = currentForegroundApp != null &&
                            currentForegroundApp != packageName &&
                            blacklisted.contains(currentForegroundApp)

                    if (!isRestrictedAppRunning) {
                        dismissAllInterventions()
                    } else {
                        // 制限対象アプリがフォアグラウンドで動作中
                        val usageList = screenTimeRepository.getTodayAppUsageList()
                        val currentAppUsage = usageList.find { it.packageName == currentForegroundApp }
                        val usedMillis = currentAppUsage?.usedTimeMillis ?: 0L

                        val evaluation = evaluateDisappointmentUseCase(
                            totalUsedMillis = usedMillis,
                            limitMillis = limitMillis,
                            userRole = userRole,
                            customMessageOverride = customMessage
                        )

                        handleIntervention(
                            currentPackage = currentForegroundApp,
                            appName = currentAppUsage?.appName ?: "対象アプリ",
                            level = evaluation.level,
                            quote = evaluation.message.mainQuote,
                            subQuote = evaluation.message.subQuote,
                            penalty = evaluation.penaltyConfig
                        )
                    }
                } catch (_: Exception) {
                    // 例外によるクラッシュを防止し監視を継続
                }

                // 介入度合いに応じてポーリング頻度を動的に調整（バッテリー保護）
                val pollInterval = if (isOverlayCurrentlyShown) 1000L else 2500L
                delay(pollInterval)
            }
        }
    }

    private fun handleIntervention(
        currentPackage: String,
        appName: String,
        level: DisappointmentLevel,
        quote: String,
        subQuote: String,
        penalty: com.example.myapplication.domain.model.PenaltyConfig
    ) {
        val now = System.currentTimeMillis()

        // 1. 段階的・連続プッシュ通知
        if (penalty.continuousNotificationIntervalSeconds > 0) {
            val intervalMillis = penalty.continuousNotificationIntervalSeconds * 1000L
            if (now - lastContinuousNotificationTime >= intervalMillis) {
                lastContinuousNotificationTime = now
                val message = com.example.myapplication.domain.model.DisappointmentMessage(
                    level = level,
                    mainQuote = quote,
                    subQuote = subQuote,
                    expression = if (level == DisappointmentLevel.LEVEL_4_COLD_STARE) {
                        com.example.myapplication.domain.model.AvatarExpression.COLD_STARE
                    } else com.example.myapplication.domain.model.AvatarExpression.GLARE
                )
                notificationHelper.showDisappointmentNotification(
                    message = message,
                    appName = appName,
                    enableVibration = penalty.enableVibration
                )
            }
        }

        // 2. 強制オーバーレイ (SYSTEM_ALERT_WINDOW)
        if (penalty.showOverlay && screenTimeRepository.hasOverlayPermission()) {
            DisappointmentOverlayService.show(
                context = this,
                alpha = penalty.overlayAlpha,
                blockTouch = penalty.blockTouchEvents,
                quote = quote,
                subQuote = subQuote
            )
            isOverlayCurrentlyShown = true
        } else {
            if (isOverlayCurrentlyShown) {
                DisappointmentOverlayService.hide(this)
                isOverlayCurrentlyShown = false
            }
        }
    }

    private fun dismissAllInterventions() {
        if (isOverlayCurrentlyShown) {
            DisappointmentOverlayService.hide(this)
            isOverlayCurrentlyShown = false
        }
    }

    override fun onDestroy() {
        dismissAllInterventions()
        serviceScope.cancel()
        super.onDestroy()
    }
}
