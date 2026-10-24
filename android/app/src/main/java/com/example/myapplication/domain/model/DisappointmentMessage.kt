package com.example.myapplication.domain.model

import androidx.compose.runtime.Immutable

/**
 * 段階的メッセージモデル
 */
@Immutable
data class DisappointmentMessage(
    val level: DisappointmentLevel,
    val mainQuote: String,       // 親のメインセリフ
    val subQuote: String,        // ため息・補足テキスト
    val expression: AvatarExpression,
    val isCustom: Boolean = false
) {
    companion object {
        fun getDefault(level: DisappointmentLevel, role: UserRole): DisappointmentMessage {
            return when (role) {
                UserRole.INDIVIDUAL -> getIndividualDefault(level)
                UserRole.CHILD, UserRole.PARENT -> getPairDefault(level)
            }
        }

        private fun getIndividualDefault(level: DisappointmentLevel): DisappointmentMessage {
            return when (level) {
                DisappointmentLevel.LEVEL_0_SERENE -> DisappointmentMessage(
                    level = level,
                    mainQuote = "今日も計画通り頑張ってるね！",
                    subQuote = "（まだ平和な空気が流れています）",
                    expression = AvatarExpression.SMILE
                )
                DisappointmentLevel.LEVEL_1_SUSPICIOUS -> DisappointmentMessage(
                    level = level,
                    mainQuote = "……ねえ、もう30分経ったけど、それ何用件？",
                    subQuote = "「ちょっと見るだけ」って言ってたよね…？",
                    expression = AvatarExpression.DOUBT
                )
                DisappointmentLevel.LEVEL_2_SIGHING -> DisappointmentMessage(
                    level = level,
                    mainQuote = "はぁ……（クソデカため息）。まだ触ってるの？",
                    subQuote = "あなたの貴重な人生が、タイムラインと共に流れていく音が聞こえるよ。",
                    expression = AvatarExpression.SIGH
                )
                DisappointmentLevel.LEVEL_3_ANNOYED -> DisappointmentMessage(
                    level = level,
                    mainQuote = "いい加減にしなさい！画面しか見ない気！？",
                    subQuote = "私の声、聞こえてる？現実世界に戻ってきなさい！",
                    expression = AvatarExpression.GLARE
                )
                DisappointmentLevel.LEVEL_4_COLD_STARE -> DisappointmentMessage(
                    level = level,
                    mainQuote = "………………。",
                    subQuote = "（もう何も言いません。ただただ冷たい無言の視線が突き刺さっています）",
                    expression = AvatarExpression.COLD_STARE
                )
            }
        }

        private fun getPairDefault(level: DisappointmentLevel): DisappointmentMessage {
            return when (level) {
                DisappointmentLevel.LEVEL_0_SERENE -> DisappointmentMessage(
                    level = level,
                    mainQuote = "時間を守って使えていて偉いね。",
                    subQuote = "今のペースをキープしよう！",
                    expression = AvatarExpression.SMILE
                )
                DisappointmentLevel.LEVEL_1_SUSPICIOUS -> DisappointmentMessage(
                    level = level,
                    mainQuote = "そろそろ区切りのいい時間だよ。",
                    subQuote = "目を休めて、お茶でも飲まない？",
                    expression = AvatarExpression.DOUBT
                )
                DisappointmentLevel.LEVEL_2_SIGHING -> DisappointmentMessage(
                    level = level,
                    mainQuote = "約束の時間に近づいてるよ。大丈夫？",
                    subQuote = "次のやることの準備を始めようね。",
                    expression = AvatarExpression.SIGH
                )
                DisappointmentLevel.LEVEL_3_ANNOYED -> DisappointmentMessage(
                    level = level,
                    mainQuote = "約束の制限時間を過ぎました！",
                    subQuote = "一度スマホを置いて、画面を閉じましょう。",
                    expression = AvatarExpression.GLARE
                )
                DisappointmentLevel.LEVEL_4_COLD_STARE -> DisappointmentMessage(
                    level = level,
                    mainQuote = "スマホの利用時間は終了しました。",
                    subQuote = "（親御さんとのルールを守りましょう。画面がロックされます）",
                    expression = AvatarExpression.COLD_STARE
                )
            }
        }
    }
}
