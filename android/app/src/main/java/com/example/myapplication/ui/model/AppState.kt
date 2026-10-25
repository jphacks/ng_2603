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
    SWEET("激アマ", "標準・王道のお母さん。やさしくなだめるように諭してくれます"),
    STANDARD("標準", "関西おかん風。ちょい厳しめのリアルなツッコミで現実に戻します"),
    STRICT("鬼", "丁寧だけど目が笑っていないお母さん。笑顔の奥に冷徹な怒りが宿る超厳しいお母さんです")
}

enum class MonitoringMode(val label: String, val description: String) {
    AFTER_LIMIT("経過後に開始", "指定した時間が経過した後に通知・見守りを開始します"),
    DURING_LIMIT("期間中見守る", "指定した時間の間ずっと余計なアプリの使用を見守ります")
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
