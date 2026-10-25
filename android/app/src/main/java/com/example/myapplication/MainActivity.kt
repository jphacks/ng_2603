package com.example.myapplication

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.myapplication.data.repository.ScreenTimeRepositoryImpl
import com.example.myapplication.data.repository.SettingsRepositoryImpl
import com.example.myapplication.domain.model.AvatarExpression
import com.example.myapplication.domain.model.DisappointmentLevel
import com.example.myapplication.domain.model.DisappointmentMessage
import com.example.myapplication.domain.model.OkanMessageGenerator
import com.example.myapplication.service.AppMonitoringService
import com.example.myapplication.service.NotificationHelper
import com.example.myapplication.ui.components.OkanBottomBar
import com.example.myapplication.ui.model.AppNotificationTarget
import com.example.myapplication.ui.model.MonitoringMode
import com.example.myapplication.ui.model.OkanPersonality
import com.example.myapplication.ui.model.PresetSetting
import com.example.myapplication.ui.model.QuietHour
import com.example.myapplication.ui.model.Screen
import com.example.myapplication.ui.model.SoundOption
import com.example.myapplication.ui.model.TabItem
import com.example.myapplication.ui.model.UserRole
import com.example.myapplication.ui.screens.AppSettingsScreen
import com.example.myapplication.ui.screens.HomeScreen
import com.example.myapplication.ui.screens.MyPresetsScreen
import com.example.myapplication.ui.screens.NotificationSettingsScreen
import com.example.myapplication.ui.screens.QrScreen
import com.example.myapplication.ui.screens.QuietHoursScreen
import com.example.myapplication.ui.screens.SettingsScreen
import com.example.myapplication.ui.screens.SoundSettingsScreen
import com.example.myapplication.ui.screens.StartMonitoringScreen
import com.example.myapplication.ui.screens.WelcomeScreen
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                OkanApp()
            }
        }
    }
}

@Composable
fun OkanApp() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    val settingsRepository = remember { SettingsRepositoryImpl(context) }
    val screenTimeRepository = remember { ScreenTimeRepositoryImpl(context, settingsRepository) }
    val notificationHelper = remember { NotificationHelper(context) }

    // 画面・状態の永続化用 SharedPreferences
    val appStatePrefs = remember {
        context.getSharedPreferences("okan_app_ui_state", Context.MODE_PRIVATE)
    }

    // 永続化から復元
    val savedScreenName = appStatePrefs.getString("saved_screen", Screen.WELCOME.name)
    val initialScreen = try { Screen.valueOf(savedScreenName ?: Screen.WELCOME.name) } catch (_: Exception) { Screen.WELCOME }

    val savedRoleName = appStatePrefs.getString("saved_role", UserRole.INDIVIDUAL.name)
    val initialRole = try { UserRole.valueOf(savedRoleName ?: UserRole.INDIVIDUAL.name) } catch (_: Exception) { UserRole.INDIVIDUAL }

    val savedPersonalityName = appStatePrefs.getString("saved_personality", OkanPersonality.STANDARD.name)
    val initialPersonality = try { OkanPersonality.valueOf(savedPersonalityName ?: OkanPersonality.STANDARD.name) } catch (_: Exception) { OkanPersonality.STANDARD }

    var currentScreen by remember { mutableStateOf(initialScreen) }
    var currentTab by remember { mutableStateOf(TabItem.HOME) }
    var isMonitoring by remember { mutableStateOf(appStatePrefs.getBoolean("is_monitoring", false)) }
    var userRole by remember { mutableStateOf(initialRole) }

    var currentProhibition by remember {
        mutableStateOf(appStatePrefs.getString("current_prohibition", "") ?: "")
    }
    var currentDurationMinutes by remember {
        mutableIntStateOf(appStatePrefs.getInt("duration_minutes", 0))
    }
    var currentPersonality by remember { mutableStateOf(initialPersonality) }
    var currentMode by remember { mutableStateOf(MonitoringMode.AFTER_LIMIT) }
    var monitoringStartTime by remember {
        mutableLongStateOf(appStatePrefs.getLong("monitoring_start_time", 0L))
    }

    // 状態の永続化
    fun saveUiState() {
        appStatePrefs.edit()
            .putString("saved_screen", currentScreen.name)
            .putString("saved_role", userRole.name)
            .putString("saved_personality", currentPersonality.name)
            .putBoolean("is_monitoring", isMonitoring)
            .putString("current_prohibition", currentProhibition)
            .putInt("duration_minutes", currentDurationMinutes)
            .putLong("monitoring_start_time", monitoringStartTime)
            .apply()
    }

    LaunchedEffect(currentScreen, isMonitoring, userRole, currentProhibition, currentDurationMinutes, currentPersonality, monitoringStartTime) {
        saveUiState()
    }

    // アプリ通知設定リスト（SharedPreferencesから永続化読み込み）
    var apps by remember {
        mutableStateOf(loadAppsFromPrefs(context, appStatePrefs))
    }

    // ホワイトリスト変更をリポジトリへ反映
    fun syncWhitelist(appList: List<AppNotificationTarget>) {
        val whitelistPkgs = appList.filter { it.isEnabled }.map { it.description }.toSet()
        coroutineScope.launch {
            settingsRepository.updateWhitelistPackages(whitelistPkgs)
        }
    }

    // アプリ設定の更新（SharedPreferencesへ即時保存 ＋ サービスへ同期）
    fun updateApps(newApps: List<AppNotificationTarget>) {
        apps = newApps
        saveAppsToPrefs(context, appStatePrefs, newApps)
        syncWhitelist(newApps)
    }

    // マイセットリスト（SharedPreferencesから永続化読み込み）
    var presets by remember {
        mutableStateOf(loadPresetsFromPrefs(appStatePrefs))
    }

    fun updatePresets(newPresets: List<PresetSetting>) {
        presets = newPresets
        savePresetsToPrefs(appStatePrefs, newPresets)
    }

    // 使わない時間帯リスト（SharedPreferencesから永続化読み込み）
    var quietHours by remember {
        mutableStateOf(loadQuietHoursFromPrefs(appStatePrefs))
    }

    fun updateQuietHours(newHours: List<QuietHour>) {
        quietHours = newHours
        saveQuietHoursToPrefs(appStatePrefs, newHours)
    }

    // 起動時に保存された状態を復元 & ホワイトリストの初期同期
    LaunchedEffect(Unit) {
        val savedLimit = settingsRepository.limitTimeMillisFlow.first()
        if (savedLimit >= 0) {
            currentDurationMinutes = (savedLimit / (60 * 1000)).toInt()
        }
        val savedMsg = settingsRepository.customMessageFlow.first()
        if (!savedMsg.isNullOrBlank()) {
            currentProhibition = savedMsg
        }
        val savedActiveInRepo = settingsRepository.isMonitoringActiveFlow.first()
        val savedActiveInPrefs = appStatePrefs.getBoolean("is_monitoring", false)
        isMonitoring = savedActiveInRepo && savedActiveInPrefs
        monitoringStartTime = if (isMonitoring) settingsRepository.monitoringStartTimeFlow.first() else 0L

        // 現在のホワイトリストをサービス側に同期
        syncWhitelist(apps)
    }

    var showUsagePermissionDialog by remember { mutableStateOf(false) }

    // 通知権限リクエストランチャー (Android 13+)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(permission)
            }
        }
    }

    // アプリがバックグラウンドに移動した際のフォールバック通知
    DisposableEffect(lifecycleOwner, isMonitoring, currentProhibition, monitoringStartTime, currentDurationMinutes) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP && isMonitoring) {
                val now = System.currentTimeMillis()
                val limitMillis = currentDurationMinutes * 60 * 1000L
                val elapsed = now - monitoringStartTime

                if (monitoringStartTime > 0 && elapsed >= limitMillis) {
                    val message = OkanMessageGenerator.createTimeUpMessage(
                        personality = currentPersonality,
                        taskName = currentProhibition,
                        limitMinutes = currentDurationMinutes
                    )
                    notificationHelper.showDisappointmentNotification(
                        message = message,
                        appName = "スマートフォン",
                        enableVibration = true
                    )
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // 見守り開始ヘルパー関数
    fun startMonitoring(
        prohibition: String,
        minutes: Int,
        personality: OkanPersonality,
        mode: MonitoringMode = MonitoringMode.AFTER_LIMIT
    ) {
        val now = System.currentTimeMillis()
        currentProhibition = prohibition
        currentDurationMinutes = minutes
        currentPersonality = personality
        currentMode = mode
        isMonitoring = true
        monitoringStartTime = now
        saveUiState()

        coroutineScope.launch {
            settingsRepository.setMonitoringActive(true)
            settingsRepository.setCustomMessage(prohibition)
            settingsRepository.setLimitTimeMillis(minutes * 60 * 1000L)
            settingsRepository.setMonitoringStartTime(now)
            settingsRepository.setPersonality(personality.name)
            settingsRepository.setMonitoringMode(mode.name)
            settingsRepository.setUserRole(
                when (userRole) {
                    UserRole.INDIVIDUAL -> com.example.myapplication.domain.model.UserRole.INDIVIDUAL
                    UserRole.PARENT -> com.example.myapplication.domain.model.UserRole.PARENT
                    UserRole.CHILD -> com.example.myapplication.domain.model.UserRole.CHILD
                }
            )

            // 常駐監視サービスの開始
            val intent = Intent(context, AppMonitoringService::class.java)
            try {
                ContextCompat.startForegroundService(context, intent)
            } catch (_: Exception) {
            }

            if (!screenTimeRepository.hasUsageStatsPermission()) {
                showUsagePermissionDialog = true
            }
        }
    }

    // 強制停止ボタン押下時: バックグラウンドサービス停止 + アプリ完全終了（プロセス終了）
    fun stopMonitoringAndExitApp() {
        isMonitoring = false
        monitoringStartTime = 0L
        currentScreen = Screen.HOME
        currentTab = TabItem.HOME

        appStatePrefs.edit()
            .putString("saved_screen", Screen.HOME.name)
            .putString("saved_role", userRole.name)
            .putBoolean("is_monitoring", false)
            .putLong("monitoring_start_time", 0L)
            .commit()

        coroutineScope.launch {
            settingsRepository.setMonitoringActive(false)
            settingsRepository.setMonitoringStartTime(0L)
            val intent = Intent(context, AppMonitoringService::class.java)
            context.stopService(intent)

            notificationHelper.cancelAll()

            (context as? Activity)?.finishAffinity()
            android.os.Process.killProcess(android.os.Process.myPid())
        }
    }

    // 通常の停止
    fun stopMonitoring() {
        isMonitoring = false
        monitoringStartTime = 0L
        saveUiState()

        coroutineScope.launch {
            settingsRepository.setMonitoringActive(false)
            settingsRepository.setMonitoringStartTime(0L)
            val intent = Intent(context, AppMonitoringService::class.java)
            context.stopService(intent)
        }
    }

    // 使わない時間帯に該当する場合、常時見守りモードを自動起動
    LaunchedEffect(quietHours) {
        val now = Calendar.getInstance()
        val currentDay = when (now.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SUNDAY -> "日"
            Calendar.MONDAY -> "月"
            Calendar.TUESDAY -> "火"
            Calendar.WEDNESDAY -> "水"
            Calendar.THURSDAY -> "木"
            Calendar.FRIDAY -> "金"
            Calendar.SATURDAY -> "土"
            else -> ""
        }
        val currentMinOfDay = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)

        val isQuietHourNow = quietHours.any { qh ->
            if (!qh.isEnabled) return@any false
            val dayMatch = qh.days.contains("毎日") || qh.days.contains(currentDay)
            if (!dayMatch) return@any false

            val parts = qh.timeRange.split("-").map { it.trim() }
            if (parts.size == 2) {
                val startParts = parts[0].split(":").mapNotNull { it.toIntOrNull() }
                val endParts = parts[1].split(":").mapNotNull { it.toIntOrNull() }
                if (startParts.size == 2 && endParts.size == 2) {
                    val sMin = startParts[0] * 60 + startParts[1]
                    val eMin = endParts[0] * 60 + endParts[1]
                    if (sMin <= eMin) currentMinOfDay in sMin..eMin else (currentMinOfDay >= sMin || currentMinOfDay <= eMin)
                } else false
            } else false
        }

        if (isQuietHourNow && !isMonitoring) {
            startMonitoring(
                prohibition = "使わない時間帯（常時見守り）",
                minutes = 0,
                personality = currentPersonality,
                mode = MonitoringMode.AFTER_LIMIT
            )
        }
    }

    // 通知音リスト
    val soundOptions = remember {
        listOf(
            SoundOption("default", "デフォルト", "システムのデフォルト"),
            SoundOption("chime", "チャイム", "優しいチャイム"),
            SoundOption("piano1", "ピアノ", "軽やかなピアノ"),
            SoundOption("piano2", "ピアノ", "落ち着いたピアノ"),
            SoundOption("alarm", "アラーム", "注意を促すアラーム")
        )
    }
    var selectedSoundId by remember { mutableStateOf("default") }

    BackHandler(enabled = currentScreen != Screen.HOME && currentScreen != Screen.WELCOME) {
        when (currentScreen) {
            Screen.START_MONITORING -> currentScreen = Screen.HOME
            Screen.QR_CODE -> currentScreen = Screen.WELCOME
            Screen.SETTINGS_APP, Screen.SETTINGS_QUIET_HOURS, Screen.SETTINGS_NOTIFICATION -> currentScreen = Screen.SETTINGS
            Screen.SETTINGS_SOUND -> currentScreen = Screen.SETTINGS_NOTIFICATION
            else -> {
                currentScreen = Screen.HOME
                currentTab = TabItem.HOME
            }
        }
    }

    val showBottomBar = currentScreen != Screen.WELCOME &&
            currentScreen != Screen.QR_CODE &&
            !(userRole == UserRole.CHILD && isMonitoring)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                OkanBottomBar(
                    currentTab = currentTab,
                    onTabSelected = { tab ->
                        currentTab = tab
                        currentScreen = when (tab) {
                            TabItem.HOME -> Screen.HOME
                            TabItem.MY_PRESETS -> Screen.MY_PRESETS
                            TabItem.SETTINGS -> Screen.SETTINGS
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                Screen.WELCOME -> {
                    WelcomeScreen(
                        onRoleSelected = { role ->
                            userRole = role
                            when (role) {
                                UserRole.INDIVIDUAL -> {
                                    isMonitoring = false
                                    currentScreen = Screen.HOME
                                    currentTab = TabItem.HOME
                                }
                                UserRole.PARENT, UserRole.CHILD -> {
                                    currentScreen = Screen.QR_CODE
                                }
                            }
                        }
                    )
                }

                Screen.HOME -> {
                    HomeScreen(
                        isMonitoring = isMonitoring,
                        userRole = userRole,
                        durationMinutes = currentDurationMinutes,
                        onPrepareMonitoringClick = {
                            currentScreen = Screen.START_MONITORING
                        },
                        onFinishMonitoringClick = {
                            stopMonitoring()
                            currentScreen = Screen.HOME
                        },
                        onStopMonitoringClick = {
                            stopMonitoringAndExitApp()
                        },
                        onResetStatusClick = {
                            stopMonitoring()
                            currentScreen = Screen.WELCOME
                        }
                    )
                }

                Screen.START_MONITORING -> {
                    StartMonitoringScreen(
                        initialProhibition = currentProhibition,
                        initialMinutes = currentDurationMinutes,
                        initialPersonality = currentPersonality,
                        initialMode = currentMode,
                        onStartMonitoringClick = { prohibition, minutes, personality, mode ->
                            startMonitoring(prohibition, minutes, personality, mode)
                            currentScreen = Screen.HOME
                        },
                        onBackClick = {
                            currentScreen = Screen.HOME
                        }
                    )
                }

                Screen.MY_PRESETS -> {
                    MyPresetsScreen(
                        presets = presets,
                        onSavePreset = { updatedPreset ->
                            val updated = presets.map { if (it.id == updatedPreset.id) updatedPreset else it }
                            updatePresets(updated)
                        }
                    )
                }

                Screen.QR_CODE -> {
                    QrScreen(
                        userRole = userRole,
                        onPairingCompleted = {
                            if (userRole == UserRole.PARENT) {
                                isMonitoring = false
                                currentScreen = Screen.HOME
                                currentTab = TabItem.HOME
                            } else {
                                startMonitoring(
                                    prohibition = currentProhibition.ifBlank { "こども見守り" },
                                    minutes = currentDurationMinutes,
                                    personality = currentPersonality,
                                    mode = currentMode
                                )
                                currentScreen = Screen.HOME
                                currentTab = TabItem.HOME
                            }
                        },
                        onBackClick = {
                            currentScreen = Screen.WELCOME
                        }
                    )
                }

                Screen.SETTINGS -> {
                    SettingsScreen(
                        onAppSettingsClick = {
                            currentScreen = Screen.SETTINGS_APP
                        },
                        onQuietHoursClick = {
                            currentScreen = Screen.SETTINGS_QUIET_HOURS
                        },
                        onNotificationClick = {
                            currentScreen = Screen.SETTINGS_NOTIFICATION
                        }
                    )
                }

                Screen.SETTINGS_APP -> {
                    AppSettingsScreen(
                        apps = apps,
                        onToggleApp = { id ->
                            val updated = apps.map { if (it.id == id) it.copy(isEnabled = !it.isEnabled) else it }
                            updateApps(updated)
                        },
                        onAddApp = { name, pkg ->
                            val newApp = AppNotificationTarget(
                                id = (apps.size + 1).toString(),
                                name = name,
                                description = pkg,
                                isEnabled = true
                            )
                            updateApps(apps + newApp)
                        },
                        onRemoveApp = { id ->
                            val updated = apps.filter { it.id != id }
                            updateApps(updated)
                        },
                        onBackClick = {
                            currentScreen = Screen.SETTINGS
                        }
                    )
                }

                Screen.SETTINGS_QUIET_HOURS -> {
                    QuietHoursScreen(
                        quietHours = quietHours,
                        onToggleHour = { id ->
                            val updated = quietHours.map { if (it.id == id) it.copy(isEnabled = !it.isEnabled) else it }
                            updateQuietHours(updated)
                        },
                        onUpdateHour = { id, newRange, newDays ->
                            val updated = quietHours.map {
                                if (it.id == id) it.copy(timeRange = newRange, days = newDays) else it
                            }
                            updateQuietHours(updated)
                        },
                        onAddHourClick = {
                            val updated = quietHours + QuietHour(
                                id = (quietHours.size + 1).toString(),
                                timeRange = "18:00 - 21:00",
                                days = "土、日",
                                isEnabled = true
                            )
                            updateQuietHours(updated)
                        },
                        onBackClick = {
                            currentScreen = Screen.SETTINGS
                        }
                    )
                }

                Screen.SETTINGS_NOTIFICATION -> {
                    val currentTitle = soundOptions.firstOrNull { it.id == selectedSoundId }?.title ?: "デフォルト"
                    NotificationSettingsScreen(
                        currentSoundTitle = currentTitle,
                        onSoundSettingClick = {
                            currentScreen = Screen.SETTINGS_SOUND
                        },
                        onBackClick = {
                            currentScreen = Screen.SETTINGS
                        }
                    )
                }

                Screen.SETTINGS_SOUND -> {
                    SoundSettingsScreen(
                        soundOptions = soundOptions,
                        selectedSoundId = selectedSoundId,
                        onSelectSound = { id ->
                            selectedSoundId = id
                        },
                        onBackClick = {
                            currentScreen = Screen.SETTINGS_NOTIFICATION
                        }
                    )
                }
            }
        }
    }

    if (showUsagePermissionDialog) {
        AlertDialog(
            onDismissRequest = { showUsagePermissionDialog = false },
            title = {
                Text(text = "他のアプリ監視の許可", fontWeight = FontWeight.Bold)
            },
            text = {
                Text(text = "他のアプリ（YouTubeやSNSなど）を開いたときにおかんが叱るために、「使用状況へのアクセス」権限を許可してください。")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showUsagePermissionDialog = false
                        try {
                            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                                data = Uri.parse("package:${context.packageName}")
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            val fallbackIntent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(fallbackIntent)
                        }
                    }
                ) {
                    Text("設定を開く")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUsagePermissionDialog = false }) {
                    Text("あとで")
                }
            }
        )
    }
}

// --- 永続化用シリアライザ／デシリアライザ ---

private fun saveAppsToPrefs(context: Context, prefs: SharedPreferences, apps: List<AppNotificationTarget>) {
    try {
        val array = JSONArray()
        for (app in apps) {
            val obj = JSONObject()
            obj.put("id", app.id)
            obj.put("name", app.name)
            obj.put("desc", app.description)
            obj.put("enabled", app.isEnabled)
            array.put(obj)
        }
        val json = array.toString()
        prefs.edit().putString("saved_apps_json", json).commit()
        context.getSharedPreferences("guardian_settings_prefs", Context.MODE_PRIVATE)
            .edit().putString("saved_apps_json", json).commit()
    } catch (_: Exception) {
    }
}

private fun loadAppsFromPrefs(context: Context, prefs: SharedPreferences): List<AppNotificationTarget> {
    val jsonStr = prefs.getString("saved_apps_json", null)
        ?: context.getSharedPreferences("guardian_settings_prefs", Context.MODE_PRIVATE)
            .getString("saved_apps_json", null)
        ?: return defaultAppsList()
    return try {
        val array = JSONArray(jsonStr)
        val list = mutableListOf<AppNotificationTarget>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                AppNotificationTarget(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    description = obj.getString("desc"),
                    isEnabled = obj.getBoolean("enabled")
                )
            )
        }
        if (list.isEmpty()) defaultAppsList() else list
    } catch (_: Exception) {
        defaultAppsList()
    }
}

private fun defaultAppsList(): List<AppNotificationTarget> {
    return listOf(
        AppNotificationTarget("1", "計算機", "com.android.calculator2", false),
        AppNotificationTarget("2", "カレンダー", "com.android.calendar", false),
        AppNotificationTarget("3", "電話", "com.android.dialer", false),
        AppNotificationTarget("4", "YouTube", "com.google.android.youtube", true),
        AppNotificationTarget("5", "X (Twitter)", "com.twitter.android", true),
        AppNotificationTarget("6", "Instagram", "com.instagram.android", true)
    )
}

private fun savePresetsToPrefs(prefs: SharedPreferences, presets: List<PresetSetting>) {
    try {
        val array = JSONArray()
        for (p in presets) {
            val obj = JSONObject()
            obj.put("id", p.id)
            obj.put("title", p.title)
            obj.put("task", p.task)
            obj.put("minutes", p.minutes)
            obj.put("personality", p.personality.name)
            obj.put("mode", p.mode.name)
            array.put(obj)
        }
        prefs.edit().putString("saved_presets_json", array.toString()).commit()
    } catch (_: Exception) {
    }
}

private fun loadPresetsFromPrefs(prefs: SharedPreferences): List<PresetSetting> {
    val jsonStr = prefs.getString("saved_presets_json", null) ?: return defaultPresetsList()
    return try {
        val array = JSONArray(jsonStr)
        val list = mutableListOf<PresetSetting>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                PresetSetting(
                    id = obj.getString("id"),
                    title = obj.getString("title"),
                    task = obj.getString("task"),
                    minutes = obj.getInt("minutes"),
                    personality = OkanPersonality.valueOf(obj.getString("personality")),
                    mode = MonitoringMode.valueOf(obj.getString("mode"))
                )
            )
        }
        if (list.isEmpty()) defaultPresetsList() else list
    } catch (_: Exception) {
        defaultPresetsList()
    }
}

private fun defaultPresetsList(): List<PresetSetting> {
    return listOf(
        PresetSetting("1", "マイセット1（勉強）", "勉強", 45, OkanPersonality.STANDARD, MonitoringMode.AFTER_LIMIT),
        PresetSetting("2", "マイセット2（読書）", "読書", 30, OkanPersonality.SWEET, MonitoringMode.AFTER_LIMIT),
        PresetSetting("3", "マイセット3（集中仕事）", "仕事", 60, OkanPersonality.STRICT, MonitoringMode.DURING_LIMIT),
        PresetSetting("4", "マイセット4（就寝前）", "スマホを見ない", 0, OkanPersonality.STRICT, MonitoringMode.AFTER_LIMIT),
        PresetSetting("5", "マイセット5（休憩）", "休憩", 15, OkanPersonality.STANDARD, MonitoringMode.AFTER_LIMIT)
    )
}

private fun saveQuietHoursToPrefs(prefs: SharedPreferences, hours: List<QuietHour>) {
    try {
        val array = JSONArray()
        for (h in hours) {
            val obj = JSONObject()
            obj.put("id", h.id)
            obj.put("range", h.timeRange)
            obj.put("days", h.days)
            obj.put("enabled", h.isEnabled)
            array.put(obj)
        }
        prefs.edit().putString("saved_quiet_hours_json", array.toString()).commit()
    } catch (_: Exception) {
    }
}

private fun loadQuietHoursFromPrefs(prefs: SharedPreferences): List<QuietHour> {
    val jsonStr = prefs.getString("saved_quiet_hours_json", null) ?: return defaultQuietHoursList()
    return try {
        val array = JSONArray(jsonStr)
        val list = mutableListOf<QuietHour>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                QuietHour(
                    id = obj.getString("id"),
                    timeRange = obj.getString("range"),
                    days = obj.getString("days"),
                    isEnabled = obj.getBoolean("enabled")
                )
            )
        }
        if (list.isEmpty()) defaultQuietHoursList() else list
    } catch (_: Exception) {
        defaultQuietHoursList()
    }
}

private fun defaultQuietHoursList(): List<QuietHour> {
    return listOf(
        QuietHour("1", "00:00 - 07:00", "毎日", true),
        QuietHour("2", "10:00 - 15:00", "月、火、水、木、金", true)
    )
}

@Preview(showBackground = true)
@Composable
fun OkanAppPreview() {
    MyApplicationTheme {
        OkanApp()
    }
}
