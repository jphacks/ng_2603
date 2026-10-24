package com.example.myapplication.domain.usecase

import com.example.myapplication.domain.model.DisappointmentLevel
import com.example.myapplication.domain.model.DisappointmentMessage
import com.example.myapplication.domain.model.PenaltyConfig
import com.example.myapplication.domain.model.UserRole

data class EvaluationResult(
    val level: DisappointmentLevel,
    val message: DisappointmentMessage,
    val penaltyConfig: PenaltyConfig,
    val usagePercentage: Float
)

/**
 * 使用時間と設定から、現在の呆れ度合い・演出メッセージ・介入設定を総合判定するユースケース
 */
class EvaluateDisappointmentUseCase {

    operator fun invoke(
        totalUsedMillis: Long,
        limitMillis: Long,
        userRole: UserRole,
        customMessageOverride: String? = null
    ): EvaluationResult {
        val percentage = if (limitMillis > 0) {
            totalUsedMillis.toFloat() / limitMillis
        } else {
            0f
        }

        val level = DisappointmentLevel.fromUsagePercentage(percentage)
        val defaultMessage = DisappointmentMessage.getDefault(level, userRole)

        // 親側でカスタマイズされたメッセージがある場合のフォールバック適用
        val finalMessage = if (!customMessageOverride.isNullOrBlank() && level >= DisappointmentLevel.LEVEL_3_ANNOYED) {
            defaultMessage.copy(mainQuote = customMessageOverride, isCustom = true)
        } else {
            defaultMessage
        }

        val penaltyConfig = PenaltyConfig.createForLevel(level)

        return EvaluationResult(
            level = level,
            message = finalMessage,
            penaltyConfig = penaltyConfig,
            usagePercentage = percentage
        )
    }
}
