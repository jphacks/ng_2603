package com.example.myapplication.domain.usecase

import kotlin.random.Random

data class MathProblem(
    val question: String,
    val answer: Int
)

/**
 * 甘え防止機能の解除フリクション（摩擦）制御ユースケース
 * 衝動的な制限解除を防ぐため、計算問題やディレイのルールを司る
 */
class UnlockFrictionUseCase {

    /**
     * ランダムな2桁＋2桁×1桁等の適度な難易度の計算問題を生成する
     */
    fun generateMathProblem(): MathProblem {
        val a = Random.nextInt(23, 89)
        val b = Random.nextInt(14, 58)
        val isMultiply = Random.nextBoolean()

        return if (isMultiply) {
            val multA = Random.nextInt(12, 35)
            val multB = Random.nextInt(4, 9)
            val answer = multA * multB + a
            MathProblem(question = "($multA × $multB) + $a = ?", answer = answer)
        } else {
            val c = Random.nextInt(3, 9)
            val answer = a + b + c
            MathProblem(question = "$a + $b + $c = ?", answer = answer)
        }
    }

    /**
     * ユーザーの回答を検証
     */
    fun verifyAnswer(problem: MathProblem, userAnswer: String): Boolean {
        val parsed = userAnswer.trim().toIntOrNull() ?: return false
        return parsed == problem.answer
    }
}
