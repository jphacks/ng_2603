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
import org.json.JSONArray

/**
 * バックグラウンド常時実行・アプリ監視サービス
 * - 「時間経過後に開始」/「期間中見守る」の2つのモードに対応
 * - アプリ設定一覧の連動:
 *     ・オンのアプリ: 使用中通知を送らない（許可アプリ）
 *     ・オフのアプリ: 使用中通知を送信する（監視対象）
 * - 制限時間が来たら最初の通知を送信
 * - やめない場合だんだん通知間隔が短縮し頻度が増加（割合）
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

    private data class ConfiguredAppTarget(
        val name: String,
        val packageName: String,
        val isEnabled: Boolean
    )

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

    /**
     * アプリ設定画面で保存された最新のアプリ設定リスト（オン/オフ）を読み取る
     */
    private fun loadConfiguredApps(): List<ConfiguredAppTarget> {
        val prefsOkan = applicationContext.getSharedPreferences("okan_app_ui_state", Context.MODE_PRIVATE)
        val prefsGuardian = applicationContext.getSharedPreferences("guardian_settings_prefs", Context.MODE_PRIVATE)
        val jsonStr = prefsOkan.getString("saved_apps_json", null)
            ?: prefsGuardian.getString("saved_apps_json", null)
            ?: return emptyList()

        return try {
            val array = JSONArray(jsonStr)
            val list = mutableListOf<ConfiguredAppTarget>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    ConfiguredAppTarget(
                        name = obj.optString("name", ""),
                        packageName = obj.optString("desc", ""),
                        isEnabled = obj.optBoolean("enabled", false)
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    /**
     * 特定のアプリが現在通知対象（違反アプリ）かどうかを判定する
     * - オン（isEnabled = true）に設定されているアプリ: 通知は送らない (false)
     * - オフ（isEnabled = false）に設定されているアプリ: 通知を送信する (true)
     */
    private fun shouldNotifyForApp(
        currentPackage: String,
        appName: String,
        configuredApps: List<ConfiguredAppTarget>,
        combinedWhitelist: Set<String>
    ): Boolean {
        // 自アプリ・ランチャー・システムUIは除外（通知しない）
        if (currentPackage == packageName ||
            currentPackage == launcherPackageName ||
            currentPackage.contains("launcher", ignoreCase = true) ||
            currentPackage.contains("systemui", ignoreCase = true)
        ) {
            return false
        }

        // 1. アプリ設定一覧（configuredApps）とマッチング
        val matched = configuredApps.firstOrNull { app ->
            val pkg = app.packageName.trim()
            val name = app.name.trim()

            (pkg.isNotBlank() && currentPackage.equals(pkg, ignoreCase = true)) ||
            (pkg.isNotBlank() && currentPackage.contains(pkg, ignoreCase = true)) ||
            (name.isNotBlank() && (appName.equals(name, ignoreCase = true) ||
                    appName.contains(name, ignoreCase = true) ||
                    name.contains(appName, ignoreCase = true)))
        }

        if (matched != null) {
            // アプリ設定でオン（isEnabled == true）のアプリ: 通知は送らない (false)
            // アプリ設定でオフ（isEnabled == false）のアプリ: 通知を送信する (true)
            return !matched.isEnabled
        }

        // 2. 設定一覧にないアプリ: ホワイトリストに含まれていれば通知しない
        if (combinedWhitelist.any { currentPackage.contains(it, ignoreCase = true) }) {
            return false
        }

        // 未設定の一般アプリは通知対象
        return true
    }

    private fun startMonitoringLoop() {
        monitoringJob?.cancel()
        monitoringJob = serviceScope.launch {
            while (isActive) {
                var loopDelay = 1200L
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

                    // 「時間内使用で通知」モードの場合は高速検知
                    if (mode == "DURING_LIMIT") {
                        loopDelay = 800L
                    }

                    // 1. 制限時間が来たら最初の通知（経過後通知モード時）
                    if (isLimitExceeded && !hasSentTimeUpNotification && startTime > 0) {
                        hasSentTimeUpNotification = true
                        sendTimeUpNotification(limitMinutes, userRole)
                    }

                    // 2. モード別の監視判定
                    val shouldMonitorNow = when (mode) {
                        "DURING_LIMIT" -> !isLimitExceeded // 選択した時間内でスマホを触っていたら即時通知
                        else -> isLimitExceeded // 経過後に開始（デフォルト）
                    }

                    if (!shouldMonitorNow) {
                        delay(1500L)
                        continue
                    }

                    // フォアグラウンドアプリのチェック
                    val currentForegroundApp = screenTimeRepository.getCurrentForegroundPackage()
                    val customProhibition = settingsRepository.customMessageFlow.first().orEmpty()

                    if (currentForegroundApp != null) {
                        val appName = getAppName(currentForegroundApp)
                        val configuredApps = loadConfiguredApps()

                        // オンに設定しているアプリを使っているときは通知は送らない、
                        // オフに設定しているアプリを使っているときは通知を送信する
                        val shouldNotifyThisApp = shouldNotifyForApp(
                            currentPackage = currentForegroundApp,
                            appName = appName,
                            configuredApps = configuredApps,
                            combinedWhitelist = combinedWhitelist
                        )

                        if (shouldNotifyThisApp) {
                            // 通知間隔の制御
                            // - 「時間内使用で通知」: 時間内でスマホを触っていたら即時通知 & 継続使用時も数秒間隔で連続通知
                            // - 「経過後通知」: だんだん通知が増えていく割合的短縮（25s -> 18s -> 12s -> 8s -> 5s）
                            val intervalMillis = when {
                                mode == "DURING_LIMIT" -> {
                                    when {
                                        currentViolationCount <= 1 -> 5000L
                                        currentViolationCount == 2 -> 4000L
                                        else -> 3000L
                                    }
                                }
                                else -> {
                                    when {
                                        currentViolationCount <= 1 -> 25000L
                                        currentViolationCount == 2 -> 18000L
                                        currentViolationCount == 3 -> 12000L
                                        currentViolationCount == 4 -> 8000L
                                        else -> 5000L
                                    }
                                }
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
                            if (currentForegroundApp == packageName || currentForegroundApp == launcherPackageName) {
                                lastNotifiedPackage = null
                            }
                        }
                    }
                } catch (_: Exception) {
                }

                delay(loopDelay)
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
     * 親向け通知: 子供が余計なアプリを開いた回数を通知（回数に応じて深刻度激化）
     */
    private fun sendParentAlertNotification(appName: String, violationCount: Int) {
        val (quote, subQuote, level) = when {
            violationCount <= 1 -> Triple(
                "【親通知】お子様が「$appName」を開きました",
                "見守り中にお子様が他のアプリを操作し始めました。",
                DisappointmentLevel.LEVEL_1_SUSPICIOUS
            )
            violationCount in 2..3 -> Triple(
                "【親通知】お子様が通知を無視して「$appName」を使用中（${violationCount}回目）",
                "警告通知が届いているにもかかわらずアプリの操作を継続しています。",
                DisappointmentLevel.LEVEL_3_ANNOYED
            )
            else -> Triple(
                "【親警告】お子様が通知を${violationCount}回連続で無視しています！",
                "「$appName」の使いすぎが続いています。直接お声がけをご検討ください。",
                DisappointmentLevel.LEVEL_4_COLD_STARE
            )
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

    /**
     * 違反継続時のお母さん（おかん）からの小言通知
     * - 通知無視回数（違反回数）が増えるにつれて口調が段階的に激化
     */
    private fun sendOkanWarningNotification(
        appName: String,
        taskName: String,
        limitMinutes: Int,
        violationCount: Int,
        mode: String
    ) {
        val taskContext = if (taskName.isNotBlank() && taskName != "指定なし") "「$taskName」はどうしたん！？" else ""

        val (quote, subQuote, level) = when (violationCount) {
            1 -> {
                Triple(
                    if (mode == "DURING_LIMIT") "ちょっと！見守り中やのに「$appName」開いてるやん！" else "約束の${limitMinutes}分過ぎたよ！「$appName」閉めなさい！",
                    "$taskContext 約束したやろ？今すぐスマホを置きなさい！",
                    DisappointmentLevel.LEVEL_1_SUSPICIOUS
                )
            }
            2 -> {
                Triple(
                    "通知見えてるよね？まだ「$appName」触ってんの！？（無視1回目）",
                    "$taskContext おかんの小言、聞こえてるやろ！早く画面を閉じなさい！",
                    DisappointmentLevel.LEVEL_2_SIGHING
                )
            }
            3 -> {
                Triple(
                    "ええ加減にしなさい！まだ「$appName」見てるやん！（無視2回目）",
                    "$taskContext おかん本気で怒るで！？今すぐスマホ置きなさい！！",
                    DisappointmentLevel.LEVEL_3_ANNOYED
                )
            }
            4 -> {
                Triple(
                    "何回言わせんの！？今すぐ「$appName」やめんかい！！（無視3回目）",
                    "$taskContext 何べん言ったらわかるん！？スマホばっかり見てんとやることやり！！",
                    DisappointmentLevel.LEVEL_3_ANNOYED
                )
            }
            5 -> {
                Triple(
                    "【警告】もう我慢の限界やで！！（無視4回目）",
                    "次無視したらスマホ本気で没収するからね！今すぐ机に置きなさい！！",
                    DisappointmentLevel.LEVEL_4_COLD_STARE
                )
            }
            else -> {
                Triple(
                    "【激怒】ええ加減にせぇよ！！（${violationCount}回も無視！！）",
                    "もうおかん知らんからね！今すぐ電源切りなさい！！スマホ没収や！！",
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
