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

enum class OkanPersonality(val label: String, val description: String) {
    SWEET("激アマ", "かなり子供に甘いお母さんを想定しています"),
    STANDARD("普通", "標準的なお母さんです"),
    STRICT("鬼", "かなり鬼なお母さんです…")
}

enum class MonitoringMode(val label: String, val description: String) {
    AFTER_LIMIT("経過後通知", "指定した時間が経過した後に通知・見守りを開始します"),
    DURING_LIMIT("時間内使用で通知", "指定した時間内でスマホを触っていたら即時通知します")
}

data class PresetSetting(
    val id: String,
    val title: String,
    val task: String,
    val minutes: Int,
    val personality: OkanPersonality,
    val mode: MonitoringMode
)

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
