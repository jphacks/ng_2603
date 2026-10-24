package com.example.myapplication.domain.model

import androidx.compose.runtime.Immutable

/**
 * 嫌がらせ・ペナルティ設定
 */
@Immutable
data class PenaltyConfig(
    val level: DisappointmentLevel,
    val showOverlay: Boolean,
    val overlayAlpha: Float,            // 不透明度 (0.2f〜0.95f)
    val blockTouchEvents: Boolean,      // タップ無効化（物理的操作妨害）
    val continuousNotificationIntervalSeconds: Long, // 連続通知間隔 (秒)
    val enableVibration: Boolean
) {
    companion object {
        fun createForLevel(level: DisappointmentLevel): PenaltyConfig {
            return when (level) {
                DisappointmentLevel.LEVEL_0_SERENE -> PenaltyConfig(
                    level = level,
                    showOverlay = false,
                    overlayAlpha = 0f,
                    blockTouchEvents = false,
                    continuousNotificationIntervalSeconds = 0,
                    enableVibration = false
                )
                DisappointmentLevel.LEVEL_1_SUSPICIOUS -> PenaltyConfig(
                    level = level,
                    showOverlay = false,
                    overlayAlpha = 0f,
                    blockTouchEvents = false,
                    continuousNotificationIntervalSeconds = 0,
                    enableVibration = false
                )
                DisappointmentLevel.LEVEL_2_SIGHING -> PenaltyConfig(
                    level = level,
                    showOverlay = false,
                    overlayAlpha = 0.3f,
                    blockTouchEvents = false,
                    continuousNotificationIntervalSeconds = 300, // 5分おき
                    enableVibration = false
                )
                DisappointmentLevel.LEVEL_3_ANNOYED -> PenaltyConfig(
                    level = level,
                    showOverlay = true,
                    overlayAlpha = 0.65f,
                    blockTouchEvents = false,
                    continuousNotificationIntervalSeconds = 60, // 1分おき
                    enableVibration = true
                )
                DisappointmentLevel.LEVEL_4_COLD_STARE -> PenaltyConfig(
                    level = level,
                    showOverlay = true,
                    overlayAlpha = 0.95f,
                    blockTouchEvents = true, // タップ完全無効化で操作を物理的に邪魔する
                    continuousNotificationIntervalSeconds = 20, // 20秒おきの強烈プッシュ通知
                    enableVibration = true
                )
            }
        }
    }
}
