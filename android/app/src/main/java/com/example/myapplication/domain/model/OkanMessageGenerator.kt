package com.example.myapplication.domain.model

import com.example.myapplication.ui.model.OkanPersonality

/**
 * お母さん（おかん）の性格モード・経過時間・アプリ名に応じた通知メッセージ生成器
 * - 激アマ: 標準・王道お母さんでやさしくなだめる
 * - 標準: 関西おかん風ちょい厳しめツッコミ
 * - 鬼: 丁寧だけど目が笑っていない怖いお母さんでめちゃくちゃ厳しい
 * - 感情などのタグは付けず、セリフそのものがダイレクトに通知として表示される
 */
object OkanMessageGenerator {

    /**
     * 制限時間到達（タイムアップ）時の初回通知メッセージ
     */
    fun createTimeUpMessage(
        personality: OkanPersonality,
        taskName: String,
        limitMinutes: Int
    ): DisappointmentMessage {
        val hasTask = taskName.isNotBlank() && taskName != "指定なし"
        val taskContextSweet = if (hasTask) "今は「$taskName」をする時間よ。" else ""
        val taskContextStandard = if (hasTask) "今は「$taskName」する時間やろ！" else ""
        val taskContextStrict = if (hasTask) "今は「$taskName」をなさる時間のはずですよ。" else ""

        val (mainQuote, subQuote) = when (personality) {
            OkanPersonality.SWEET -> {
                "約束の${limitMinutes}分経ったわよ。" to
                "『ちょっと見るだけ』ってお約束だったでしょ？ $taskContextSweet 目を休めてスマホを置こうね。"
            }
            OkanPersonality.STANDARD -> {
                "ほら！約束の${limitMinutes}分過ぎてるで！" to
                "『ちょっとだけ』って言うて何分経ったん？ $taskContextStandard 画面ばっかり見てんとスマホ置きなさい！"
            }
            OkanPersonality.STRICT -> {
                "お約束の${limitMinutes}分が経過いたしました。" to
                "『少しだけ』とおっしゃっていましたよね？ $taskContextStrict 直ちに画面を閉じて机に置きなさい。"
            }
        }

        return DisappointmentMessage(
            level = DisappointmentLevel.LEVEL_1_SUSPICIOUS,
            mainQuote = mainQuote,
            subQuote = subQuote,
            expression = AvatarExpression.DOUBT
        )
    }

    /**
     * アプリ使用・違反時の小言通知メッセージ（回数に応じて段階的にエスカレート）
     */
    fun createWarningMessage(
        personality: OkanPersonality,
        appName: String,
        taskName: String,
        elapsedMinutes: Int,
        violationCount: Int,
        mode: String
    ): DisappointmentMessage {
        val hasTask = taskName.isNotBlank() && taskName != "指定なし"

        val (mainQuote, subQuote, level) = when (personality) {
            OkanPersonality.SWEET -> {
                // 激アマ: 標準王道お母さんでやさしくなだめる
                when {
                    violationCount <= 1 -> {
                        val task = if (hasTask) "今は「$taskName」をする時間でしょ？" else "やるべきことがあるんじゃない？"
                        Triple(
                            "なに「$appName」触ってるの！ダメでしょ？",
                            "『ちょっと見るだけ』って言ったの何分前？ $task 一回スマホを置こうね。",
                            DisappointmentLevel.LEVEL_1_SUSPICIOUS
                        )
                    }
                    violationCount <= 3 -> {
                        val task = if (hasTask) "「$taskName」はどうなったの？" else "ちゃんとやるべきこと進んでる？"
                        Triple(
                            "画面近すぎ！もう${elapsedMinutes}分も見てるわよ？",
                            "ずっと「$appName」触ってたら目が悪くなっちゃう。$task 休憩にしようね？",
                            DisappointmentLevel.LEVEL_2_SIGHING
                        )
                    }
                    else -> {
                        val task = if (hasTask) "今は「$taskName」をする時間よ！" else "これ以上見ちゃダメよ。"
                        Triple(
                            "お母さん、もう心配で見てられないわ…",
                            "何回言ったらやめられるの？ $task 「$appName」を閉じて、今すぐスマホを置きなさいね。",
                            DisappointmentLevel.LEVEL_3_ANNOYED
                        )
                    }
                }
            }

            OkanPersonality.STANDARD -> {
                // 標準: 関西おかん風ちょい厳しめ
                when {
                    violationCount <= 1 -> {
                        val task = if (hasTask) "いまは「$taskName」をする時間やろ！" else "やることあんのんちゃうん？"
                        Triple(
                            "なに「$appName」触ってんの！！ダメでしょ！",
                            "『ちょっとだけ見る』って言うたの何分前よ！ $task 早よスマホ置きなさい！",
                            DisappointmentLevel.LEVEL_2_SIGHING
                        )
                    }
                    violationCount <= 3 -> {
                        val task = if (hasTask) "「$taskName」放ったらかして何してんの！" else "いつまで触る気ぃ？"
                        Triple(
                            "画面近すぎ！もう${elapsedMinutes}分もスマホ触ってるで！",
                            "まだ「$appName」見てんの！？（${violationCount}回目やで！） $task 目ぇ悪なるわ！",
                            DisappointmentLevel.LEVEL_3_ANNOYED
                        )
                    }
                    else -> {
                        val task = if (hasTask) "「$taskName」終わるまでスマホ触ったらアカン！" else "もう許さへんで！"
                        Triple(
                            "いい加減にしなさい！充電器隠すで！！",
                            "おかんの言うこと聞こえへんの！？ $task 今すぐ「$appName」閉じてスマホ没収やで！！",
                            DisappointmentLevel.LEVEL_4_COLD_STARE
                        )
                    }
                }
            }

            OkanPersonality.STRICT -> {
                // 鬼: 丁寧だけど目が笑っていない怖いお母さんでめちゃくちゃ厳しい
                when {
                    violationCount <= 1 -> {
                        val task = if (hasTask) "今は「$taskName」をする時間のはずですよね？" else "何か大切なことを忘れていませんか？"
                        Triple(
                            "あら、なに「$appName」をお触りになっているのかしら？",
                            "『ほんの少しだけ確認する』とおっしゃったのは何分前でしたか？ $task ダメですよ。",
                            DisappointmentLevel.LEVEL_2_SIGHING
                        )
                    }
                    violationCount <= 3 -> {
                        val task = if (hasTask) "「$taskName」はもう完全に放棄されたわけですね。" else "お約束を破るのが随分とお上手になられましたね。"
                        Triple(
                            "随分と画面とお顔がお近いですこと。もう${elapsedMinutes}分ですよ？",
                            "まだ「$appName」をご覧ですか。（${violationCount}度目の注意です） $task 私の言葉、理解できますか？",
                            DisappointmentLevel.LEVEL_3_ANNOYED
                        )
                    }
                    else -> {
                        val task = if (hasTask) "「$taskName」を終えるまで、二度とスマホはお渡しいたしません。" else "これ以上の猶予はありません。"
                        Triple(
                            "……今すぐ、その「$appName」を閉じなさい。",
                            "私の言っている意味が分からないようですね。$task 次の瞬間に手放さなければ、端末を解約いたします。",
                            DisappointmentLevel.LEVEL_4_COLD_STARE
                        )
                    }
                }
            }
        }

        return DisappointmentMessage(
            level = level,
            mainQuote = mainQuote,
            subQuote = subQuote,
            expression = when (level) {
                DisappointmentLevel.LEVEL_1_SUSPICIOUS -> AvatarExpression.DOUBT
                DisappointmentLevel.LEVEL_2_SIGHING -> AvatarExpression.SIGH
                DisappointmentLevel.LEVEL_3_ANNOYED -> AvatarExpression.GLARE
                else -> AvatarExpression.COLD_STARE
            }
        )
    }
}
