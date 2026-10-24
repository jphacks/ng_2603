package com.example.myapplication

import android.Manifest
import android.content.Context
import android.content.Intent
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
import com.example.myapplication.service.AppMonitoringService
import com.example.myapplication.service.NotificationHelper
import com.example.myapplication.ui.components.OkanBottomBar
import com.example.myapplication.ui.model.AppNotificationTarget
import com.example.myapplication.ui.model.OkanPersonality
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
import kotlinx.coroutines.launch

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

    var currentScreen by remember { mutableStateOf(Screen.WELCOME) }
    var currentTab by remember { mutableStateOf(TabItem.HOME) }
    var isMonitoring by remember { mutableStateOf(false) }
    var userRole by remember { mutableStateOf(UserRole.INDIVIDUAL) }

    // アプリの状態
    var currentProhibition by remember { mutableStateOf("料理中にスマホいじっちゃダメ！") }
    var currentDurationMinutes by remember { mutableIntStateOf(30) }
    var currentPersonality by remember { mutableStateOf(OkanPersonality.STANDARD) }

    // 権限ダイアログの表示管理
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

    // アプリがバックグラウンドに回った瞬間（他アプリやホームを開いたとき）の即時おかん通知
    DisposableEffect(lifecycleOwner, isMonitoring, currentProhibition) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP && isMonitoring) {
                // 他のアプリを開いたと検知！即座にお母さん通知を発火
                val quote = when (currentPersonality) {
                    OkanPersonality.SWEET -> "もう、スマホ置いてやるべきこと頑張って〜！"
                    OkanPersonality.STRICT -> "こら！！今すぐスマホを置きなさい！！"
                    OkanPersonality.STANDARD -> "こら！「$currentProhibition」って約束したでしょ！"
                }
                notificationHelper.showDisappointmentNotification(
                    message = DisappointmentMessage(
                        level = DisappointmentLevel.LEVEL_2_SIGHING,
                        mainQuote = quote,
                        subQuote = "お母さん全部お見通しなんやからね！約束の時間まで我慢しなさい！",
                        expression = AvatarExpression.GLARE
                    ),
                    appName = "スマートフォン",
                    enableVibration = true
                )
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // 見守り開始ヘルパー関数
    fun startMonitoringService(prohibition: String, minutes: Int, personality: OkanPersonality) {
        currentProhibition = prohibition
        currentDurationMinutes = minutes
        currentPersonality = personality
        isMonitoring = true

        coroutineScope.launch {
            settingsRepository.setMonitoringActive(true)
            settingsRepository.setCustomMessage(prohibition)
            settingsRepository.setLimitTimeMillis(minutes * 60 * 1000L)
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

            // 使用統計権限がなければダイアログを促す
            if (!screenTimeRepository.hasUsageStatsPermission()) {
                showUsagePermissionDialog = true
            }
        }
    }

    // 見守り停止ヘルパー関数
    fun stopMonitoringService() {
        isMonitoring = false
        coroutineScope.launch {
            settingsRepository.setMonitoringActive(false)
            val intent = Intent(context, AppMonitoringService::class.java)
            context.stopService(intent)
        }
    }

    // アプリ通知設定リスト
    var apps by remember {
        mutableStateOf(
            listOf(
                AppNotificationTarget("1", "Google", "Googleアカウント設定", true),
                AppNotificationTarget("2", "X", "デフォルト設定", true),
                AppNotificationTarget("3", "Instagram", "Instagramアカウント設定", true)
            )
        )
    }

    // 使わない時間帯リスト
    var quietHours by remember {
        mutableStateOf(
            listOf(
                QuietHour("1", "00:00 - 07:00", "毎日", true),
                QuietHour("2", "10:00 - 15:00", "月、火、水、木、金", true)
            )
        )
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

    // 戻るボタンのハンドリング
    BackHandler(enabled = currentScreen != Screen.HOME && currentScreen != Screen.WELCOME) {
        when (currentScreen) {
            Screen.START_MONITORING -> currentScreen = Screen.HOME
            Screen.QR_CODE -> {
                currentScreen = Screen.WELCOME
            }
            Screen.SETTINGS_APP, Screen.SETTINGS_QUIET_HOURS, Screen.SETTINGS_NOTIFICATION -> {
                currentScreen = Screen.SETTINGS
            }
            Screen.SETTINGS_SOUND -> currentScreen = Screen.SETTINGS_NOTIFICATION
            else -> {
                currentScreen = Screen.HOME
                currentTab = TabItem.HOME
            }
        }
    }

    // ボトムバーの表示制御
    // ウェルカム、QRコード、見守り設定画面、または子供見守り中画面ではボトムバーを非表示にして全画面を活用！
    val showBottomBar = currentScreen != Screen.WELCOME &&
            currentScreen != Screen.QR_CODE &&
            currentScreen != Screen.START_MONITORING &&
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
                                    // 個人管理ならホームにとぶ
                                    isMonitoring = false
                                    currentScreen = Screen.HOME
                                    currentTab = TabItem.HOME
                                }
                                UserRole.PARENT, UserRole.CHILD -> {
                                    // 親またはこどもならQRコード画面へ
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
                        onStopMonitoringClick = {
                            stopMonitoringService()
                        },
                        onQrClick = {
                            currentScreen = Screen.QR_CODE
                        }
                    )
                }

                Screen.START_MONITORING -> {
                    StartMonitoringScreen(
                        onStartMonitoringClick = { prohibition, minutes, personality ->
                            startMonitoringService(prohibition, minutes, personality)
                            currentScreen = Screen.HOME
                        },
                        onBackClick = {
                            currentScreen = Screen.HOME
                        }
                    )
                }

                Screen.MY_PRESETS -> {
                    MyPresetsScreen(
                        onPresetClick = { index ->
                            currentProhibition = "マイセット $index の禁止事項"
                            currentScreen = Screen.START_MONITORING
                        }
                    )
                }

                Screen.QR_CODE -> {
                    QrScreen(
                        userRole = userRole,
                        onPairingCompleted = {
                            if (userRole == UserRole.PARENT) {
                                // 親アカウントは、ホーム画面にとぶ
                                isMonitoring = false
                                currentScreen = Screen.HOME
                                currentTab = TabItem.HOME
                            } else {
                                // 子供アカウントは、見守り中画面にとぶ
                                startMonitoringService(
                                    prohibition = "お母さんとの約束を守ろう！",
                                    minutes = 30,
                                    personality = OkanPersonality.STANDARD
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
                            apps = apps.map { if (it.id == id) it.copy(isEnabled = !it.isEnabled) else it }
                        },
                        onSelectAll = {
                            val allEnabled = apps.all { it.isEnabled }
                            apps = apps.map { it.copy(isEnabled = !allEnabled) }
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
                            quietHours = quietHours.map { if (it.id == id) it.copy(isEnabled = !it.isEnabled) else it }
                        },
                        onAddHourClick = {
                            quietHours = quietHours + QuietHour(
                                id = (quietHours.size + 1).toString(),
                                timeRange = "18:00 - 21:00",
                                days = "土、日",
                                isEnabled = true
                            )
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

    // 使用状況へのアクセス権限誘導ダイアログ
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

@Preview(showBackground = true)
@Composable
fun OkanAppPreview() {
    MyApplicationTheme {
        OkanApp()
    }
}
