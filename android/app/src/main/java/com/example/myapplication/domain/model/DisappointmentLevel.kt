package com.example.myapplication.domain.model

import androidx.compose.ui.graphics.Color
import com.example.myapplication.ui.theme.AkiLevel0Green
import com.example.myapplication.ui.theme.AkiLevel1Yellow
import com.example.myapplication.ui.theme.AkiLevel2Orange
import com.example.myapplication.ui.theme.AkiLevel3Red
import com.example.myapplication.ui.theme.AkiLevel4DarkPurple

/**
 * 呆れ度合いの段階（0〜4）
 * 時間経過や制限到達度に応じて推移する
 */
enum class DisappointmentLevel(
    val levelNumber: Int,
    val title: String,
    val color: Color,
    val progress: Float
) {
    LEVEL_0_SERENE(
        levelNumber = 0,
        title = "平常・見守り",
        color = AkiLevel0Green,
        progress = 0.0f
    ),
    LEVEL_1_SUSPICIOUS(
        levelNumber = 1,
        title = "少し怪しむ・小言",
        color = AkiLevel1Yellow,
        progress = 0.25f
    ),
    LEVEL_2_SIGHING(
        levelNumber = 2,
        title = "深いため息・呆れ初め",
        color = AkiLevel2Orange,
        progress = 0.50f
    ),
    LEVEL_3_ANNOYED(
        levelNumber = 3,
        title = "限界寸前・強い呆れ",
        color = AkiLevel3Red,
        progress = 0.75f
    ),
    LEVEL_4_COLD_STARE(
        levelNumber = 4,
        title = "完全呆れ果て・無言の冷視",
        color = AkiLevel4DarkPurple,
        progress = 1.0f
    );

    companion object {
        fun fromUsagePercentage(percentage: Float): DisappointmentLevel {
            return when {
                percentage < 0.5f -> LEVEL_0_SERENE
                percentage < 0.75f -> LEVEL_1_SUSPICIOUS
                percentage < 1.0f -> LEVEL_2_SIGHING
                percentage < 1.25f -> LEVEL_3_ANNOYED
                else -> LEVEL_4_COLD_STARE
            }
        }
    }
}
