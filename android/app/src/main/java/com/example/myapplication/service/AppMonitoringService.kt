package com.example.myapplication.service

import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import com.example.myapplication.data.repository.ScreenTimeRepositoryImpl
import com.example.myapplication.data.repository.SettingsRepositoryImpl
import com.example.myapplication.domain.model.AvatarExpression
import com.example.myapplication.domain.model.DisappointmentLevel
import com.example.myapplication.domain.model.DisappointmentMessage
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
 * このアプリ以外のアプリ（YouTube, SNS, ゲーム, ブラウザ等）を開いたときに
 * お母さんからの小言・警告通知を自動的に送信する。
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

    // ランチャー（ホーム画面）のパッケージ名
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
                        delay(3000)
                        continue
                    }

                    val currentForegroundApp = screenTimeRepository.getCurrentForegroundPackage()
                    val customProhibition = settingsRepository.customMessageFlow.first() ?: "スマホいじっちゃダメ！"

                    // 自アプリおよびホーム画面（ランチャー）以外を開いた場合を「他のアプリを開いた」と判定
                    val isOtherAppRunning = currentForegroundApp != null &&
                            currentForegroundApp != packageName &&
                            currentForegroundApp != launcherPackageName &&
                            !currentForegroundApp.contains("launcher", ignoreCase = true)

                    if (isOtherAppRunning) {
                        val now = System.currentTimeMillis()
                        val appName = getAppName(currentForegroundApp!!)

                        // 他のアプリに切り替わった瞬間、または前回の通知から10秒以上経過している場合に発火
                        val shouldNotify = (currentForegroundApp != lastNotifiedPackage) ||
                                (now - lastNotificationTime >= 10000L)

                        if (shouldNotify) {
                            lastNotifiedPackage = currentForegroundApp
                            lastNotificationTime = now
                            currentViolationCount++

                            sendOkanWarningNotification(
                                appName = appName,
                                prohibition = customProhibition,
                                violationCount = currentViolationCount
                            )
                        }
                    } else {
                        if (currentForegroundApp == packageName) {
                            // 自アプリに戻ってきた
                            lastNotifiedPackage = null
                        }
                    }
                } catch (_: Exception) {
                    // 監視を継続
                }

                delay(1500L)
            }
        }
    }

    /**
     * お母さん（おかん）らしい説教・小言の通知を送信
     */
    private fun sendOkanWarningNotification(
        appName: String,
        prohibition: String,
        violationCount: Int
    ) {
        val (quote, subQuote, level) = when {
            violationCount <= 1 -> {
                Triple(
                    "こら！「$appName」開いてるでしょ！",
                    "「$prohibition」って約束したやろ！今すぐスマホ置きなさい！",
                    DisappointmentLevel.LEVEL_1_SUSPICIOUS
                )
            }
            violationCount <= 3 -> {
                Triple(
                    "まだ「$appName」見てんの！？",
                    "お母さん全部見てるんやからね！約束の時間まで我慢しなさい！",
                    DisappointmentLevel.LEVEL_2_SIGHING
                )
            }
            violationCount <= 5 -> {
                Triple(
                    "いい加減にしなさい！！",
                    "「$prohibition」って言ったのに何回開く気！？スマホ没収するよ！",
                    DisappointmentLevel.LEVEL_3_ANNOYED
                )
            }
            else -> {
                Triple(
                    "………………（激怒）",
                    "もう何も言いません。今すぐ現実に戻りなさい！！",
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
