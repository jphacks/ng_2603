package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
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
    var currentScreen by remember { mutableStateOf(Screen.WELCOME) }
    var currentTab by remember { mutableStateOf(TabItem.HOME) }
    var isMonitoring by remember { mutableStateOf(false) }
    var userRole by remember { mutableStateOf(UserRole.INDIVIDUAL) }

    // アプリの状態
    var currentProhibition by remember { mutableStateOf("料理中にスマホいじっちゃダメ！") }
    var currentDurationMinutes by remember { mutableIntStateOf(30) }
    var currentPersonality by remember { mutableStateOf(OkanPersonality.STANDARD) }

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
                // 初回フローからのQR画面であればWELCOMEに戻す
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

    val showBottomBar = currentScreen != Screen.WELCOME && currentScreen != Screen.QR_CODE

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
                        onPrepareMonitoringClick = {
                            currentScreen = Screen.START_MONITORING
                        },
                        onStopMonitoringClick = {
                            isMonitoring = false
                        },
                        onQrClick = {
                            currentScreen = Screen.QR_CODE
                        }
                    )
                }

                Screen.START_MONITORING -> {
                    StartMonitoringScreen(
                        onStartMonitoringClick = { prohibition, minutes, personality ->
                            currentProhibition = prohibition
                            currentDurationMinutes = minutes
                            currentPersonality = personality
                            isMonitoring = true
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
                                isMonitoring = false
                                currentScreen = Screen.HOME
                                currentTab = TabItem.HOME
                            } else {
                                // 子供アカウントは見守り中画面へ遷移！
                                isMonitoring = true
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
}

@Preview(showBackground = true)
@Composable
fun OkanAppPreview() {
    MyApplicationTheme {
        OkanApp()
    }
}
