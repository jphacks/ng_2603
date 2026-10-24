package com.example.myapplication

import com.example.myapplication.domain.usecase.MathProblem
import com.example.myapplication.domain.usecase.UnlockFrictionUseCase
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UnlockFrictionUseCaseTest {

    private lateinit var useCase: UnlockFrictionUseCase

    @Before
    fun setUp() {
        useCase = UnlockFrictionUseCase()
    }

    @Test
    fun `計算問題が正しく生成されること`() {
        val problem = useCase.generateMathProblem()
        assertNotNull(problem.question)
        assertTrue(problem.question.contains("?"))
        assertTrue(problem.answer > 0)
    }

    @Test
    fun `正しい回答の場合はtrueを返すこと`() {
        val problem = MathProblem(question = "10 + 20 = ?", answer = 30)
        val isCorrect = useCase.verifyAnswer(problem, "30")
        assertTrue(isCorrect)
    }

    @Test
    fun `誤った回答の場合はfalseを返すこと`() {
        val problem = MathProblem(question = "10 + 20 = ?", answer = 30)
        val isCorrect = useCase.verifyAnswer(problem, "29")
        assertFalse(isCorrect)
    }

    @Test
    fun `不正な文字列入力の場合にクラッシュせずfalseを返すこと`() {
        val problem = MathProblem(question = "10 + 20 = ?", answer = 30)
        val isCorrect = useCase.verifyAnswer(problem, "abc")
        assertFalse(isCorrect)
    }
}
