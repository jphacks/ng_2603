package com.example.myapplication.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import com.example.myapplication.data.repository.ScreenTimeRepositoryImpl
import com.example.myapplication.data.repository.SettingsRepositoryImpl
import com.example.myapplication.domain.model.AvatarExpression
import com.example.myapplication.domain.model.DisappointmentLevel
import com.example.myapplication.domain.model.DisappointmentMessage
import com.example.myapplication.domain.model.UserRole
import com.example.myapplication.domain.repository.ScreenTimeRepository
import com.example.myapplication.domain.repository.SettingsRepository
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
 * - 「時間経過後に開始」/「期間中見守る」の2つのモードに対応
 * - 制限時間が来たら最初の通知を送信
 * - やめない場合だんだん通知間隔が短縮し頻度が増加（割合）
 * - ホワイトリスト登録アプリは通知対象から除外
 * - 親アカウントの場合は「子供が余計なアプリを開いた回数」を記録・通知
 */
class AppMonitoringService : Service() {

    private lateinit var screenTimeRepository: ScreenTimeRepository
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var notificationHelper: NotificationHelper

    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private var monitoringJob: Job? = null
    private var lastNotifiedPackage: String? = null
    private var lastNotificationTime: Long = 0
    private var currentViolationCount = 0
    private var hasSentTimeUpNotification = false

    private var launcherPackageName: String? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        settingsRepository = SettingsRepositoryImpl(applicationContext)
        screenTimeRepository = ScreenTimeRepositoryImpl(applicationContext, settingsRepository)
        notificationHelper = NotificationHelper(applicationContext)

        launcherPackageName = getLauncherPackage()

        val initialNotification = notificationHelper.buildMonitoringNotification("おかんが見守り中（約束を守ってね）")
        startForeground(NotificationHelper.NOTIFICATION_ID_FOREGROUND_SERVICE, initialNotification)

        startMonitoringLoop()
    }

    private fun getLauncherPackage(): String? {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val resolveInfo = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        return resolveInfo?.activityInfo?.packageName
    }

    private fun startMonitoringLoop() {
        monitoringJob?.cancel()
        monitoringJob = serviceScope.launch {
            while (isActive) {
                try {
                    val isActiveMonitoring = settingsRepository.isMonitoringActiveFlow.first()
                    if (!isActiveMonitoring) {
                        lastNotifiedPackage = null
                        currentViolationCount = 0
                        hasSentTimeUpNotification = false
                        delay(2000)
                        continue
                    }

                    val now = System.currentTimeMillis()
                    val startTime = settingsRepository.monitoringStartTimeFlow.first()
                    val limitMillis = settingsRepository.limitTimeMillisFlow.first()
                    val mode = settingsRepository.monitoringModeFlow.first()
                    val userRole = settingsRepository.userRoleFlow.first()
                    val whitelist = settingsRepository.whitelistPackagesFlow.first()
                    val directWhitelist = applicationContext.getSharedPreferences("guardian_settings_prefs", Context.MODE_PRIVATE)
                        .getStringSet("whitelist_packages", emptySet()).orEmpty()
                    val combinedWhitelist = whitelist + directWhitelist

                    val elapsed = now - startTime
                    val isLimitExceeded = (startTime > 0 && elapsed >= limitMillis)
                    val limitMinutes = (limitMillis / (60 * 1000)).toInt()

                    // 1. 制限時間が来たら最初の通知
                    if (isLimitExceeded && !hasSentTimeUpNotification && startTime > 0) {
                        hasSentTimeUpNotification = true
                        sendTimeUpNotification(limitMinutes, userRole)
                    }

                    // 2. モード別の監視判定
                    val shouldMonitorNow = when (mode) {
                        "DURING_LIMIT" -> !isLimitExceeded // 期間中見守る
                        else -> isLimitExceeded // 経過後に開始（デフォルト）
                    }

                    if (!shouldMonitorNow) {
                        delay(1500L)
                        continue
                    }

                    // フォアグラウンドアプリのチェック
                    val currentForegroundApp = screenTimeRepository.getCurrentForegroundPackage()
                    val customProhibition = settingsRepository.customMessageFlow.first().orEmpty()

                    // 自アプリ・ランチャー・ホワイトリスト許可アプリは除外
                    val isOtherAppRunning = currentForegroundApp != null &&
                            currentForegroundApp != packageName &&
                            currentForegroundApp != launcherPackageName &&
                            !currentForegroundApp.contains("launcher", ignoreCase = true) &&
                            !combinedWhitelist.contains(currentForegroundApp)

                    if (isOtherAppRunning) {
                        val appName = getAppName(currentForegroundApp!!)

                        // 通知間隔を違反回数に応じて割合的に短縮（通知頻度が段階的に急増）
                        // 1回目: 25秒 -> 2回目: 18秒 -> 3回目: 12秒 -> 4回目: 8秒 -> 5回目以降: 5秒
                        val intervalMillis = when {
                            currentViolationCount <= 1 -> 25000L
                            currentViolationCount == 2 -> 18000L
                            currentViolationCount == 3 -> 12000L
                            currentViolationCount == 4 -> 8000L
                            else -> 5000L
                        }

                        val shouldNotify = (currentForegroundApp != lastNotifiedPackage) ||
                                (now - lastNotificationTime >= intervalMillis)

                        if (shouldNotify) {
                            lastNotifiedPackage = currentForegroundApp
                            lastNotificationTime = now
                            currentViolationCount = settingsRepository.incrementViolationCount()

                            if (userRole == UserRole.PARENT) {
                                // 親アカウント: お子様が余計なアプリを開いた回数を通知
                                sendParentAlertNotification(appName, currentViolationCount)
                            } else {
                                // こども・個人: おかんの小言通知
                                sendOkanWarningNotification(
                                    appName = appName,
                                    taskName = customProhibition,
                                    limitMinutes = limitMinutes,
                                    violationCount = currentViolationCount,
                                    mode = mode
                                )
                            }
                        }
                    } else {
                        if (currentForegroundApp == packageName) {
                            lastNotifiedPackage = null
                        }
                    }
                } catch (_: Exception) {
                }

                delay(1200L)
            }
        }
    }

    /**
     * 制限時間に到達した瞬間の初回通知
     */
    private fun sendTimeUpNotification(limitMinutes: Int, userRole: UserRole) {
        val quote = if (userRole == UserRole.PARENT) {
            "お子様の設定時間（${limitMinutes}分）が経過しました！"
        } else {
            "約束の${limitMinutes}分が経ちました！スマホを置きなさい！"
        }

        val message = DisappointmentMessage(
            level = DisappointmentLevel.LEVEL_2_SIGHING,
            mainQuote = quote,
            subQuote = "時間になりました。これ以上使い続けるとおかんが怒ります！",
            expression = AvatarExpression.SIGH
        )

        notificationHelper.showDisappointmentNotification(
            message = message,
            appName = "タイマー",
            enableVibration = true
        )
    }

    /**
     * 親向け通知: 子供が余計なアプリを開いた回数を通知
     */
    private fun sendParentAlertNotification(appName: String, violationCount: Int) {
        val message = DisappointmentMessage(
            level = if (violationCount > 3) DisappointmentLevel.LEVEL_4_COLD_STARE else DisappointmentLevel.LEVEL_3_ANNOYED,
            mainQuote = "【親通知】お子様が「$appName」を開きました！（合計: ${violationCount}回目）",
            subQuote = "見守り時間中にお子様が他のアプリを操作しています。ご注意ください。",
            expression = AvatarExpression.GLARE
        )

        notificationHelper.showDisappointmentNotification(
            message = message,
            appName = appName,
            enableVibration = true
        )
    }

    /**
     * 違反継続時のお母さん（おかん）からの小言通知（回数に応じて激化）
     */
    private fun sendOkanWarningNotification(
        appName: String,
        taskName: String,
        limitMinutes: Int,
        violationCount: Int,
        mode: String
    ) {
        val taskContext = if (taskName.isNotBlank() && taskName != "指定なし") "「$taskName」はどうしたん！？" else ""

        val (quote, subQuote, level) = when {
            violationCount <= 1 -> {
                Triple(
                    if (mode == "DURING_LIMIT") "見守り中やのに「$appName」開いてるやん！" else "約束の${limitMinutes}分過ぎたよ！「$appName」閉めなさい！",
                    "$taskContext 今すぐスマホを置きなさい！",
                    DisappointmentLevel.LEVEL_2_SIGHING
                )
            }
            violationCount <= 3 -> {
                Triple(
                    "まだ「$appName」見てんの！？（${violationCount}回目やで！）",
                    "$taskContext おかんの言うこと聞こえへんの！？早くやめなさい！",
                    DisappointmentLevel.LEVEL_3_ANNOYED
                )
            }
            else -> {
                Triple(
                    "いい加減にしなさい！！（激怒・${violationCount}回目）",
                    "もう何も言いません。今すぐ画面を閉じなさい！！スマホ没収するよ！",
                    DisappointmentLevel.LEVEL_4_COLD_STARE
                )
            }
        }

        val message = DisappointmentMessage(
            level = level,
            mainQuote = quote,
            subQuote = subQuote,
            expression = when (level) {
                DisappointmentLevel.LEVEL_1_SUSPICIOUS -> AvatarExpression.DOUBT
                DisappointmentLevel.LEVEL_2_SIGHING -> AvatarExpression.SIGH
                DisappointmentLevel.LEVEL_3_ANNOYED -> AvatarExpression.GLARE
                else -> AvatarExpression.COLD_STARE
            }
        )

        notificationHelper.showDisappointmentNotification(
            message = message,
            appName = appName,
            enableVibration = true
        )
    }

    private fun getAppName(packageName: String): String {
        return try {
            val info = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(info).toString()
        } catch (_: Exception) {
            when {
                packageName.contains("youtube", ignoreCase = true) -> "YouTube"
                packageName.contains("chrome", ignoreCase = true) -> "Chrome"
                packageName.contains("twitter", ignoreCase = true) || packageName.contains("x", ignoreCase = true) -> "X"
                packageName.contains("instagram", ignoreCase = true) -> "Instagram"
                packageName.contains("line", ignoreCase = true) -> "LINE"
                else -> "アプリ"
            }
        }
    }

    override fun onDestroy() {
        monitoringJob?.cancel()
        serviceScope.cancel()
        super.onDestroy()
    }
}
