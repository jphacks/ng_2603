package com.example.myapplication.ui.model

enum class TabItem {
    HOME,
    MY_PRESETS,
    SETTINGS
}

enum class Screen {
    WELCOME,
    HOME,
    START_MONITORING,
    MY_PRESETS,
    SETTINGS,
    SETTINGS_APP,
    SETTINGS_QUIET_HOURS,
    SETTINGS_NOTIFICATION,
    SETTINGS_SOUND,
    QR_CODE
}

enum class UserRole(val title: String, val description: String) {
    CHILD("こども", "お子様のスマホに入れる場合"),
    PARENT("親", "お子様のスマホを管理する場合"),
    INDIVIDUAL("個人管理", "自分自身のスマホを管理する場合")
}

enum class OkanPersonality(val label: String) {
    SWEET("甘口"),
    STANDARD("標準"),
    STRICT("鬼")
}

data class AppNotificationTarget(
    val id: String,
    val name: String,
    val description: String,
    val isEnabled: Boolean
)

data class QuietHour(
    val id: String,
    val timeRange: String,
    val days: String,
    val isEnabled: Boolean
)

data class SoundOption(
    val id: String,
    val title: String,
    val subtitle: String
)
