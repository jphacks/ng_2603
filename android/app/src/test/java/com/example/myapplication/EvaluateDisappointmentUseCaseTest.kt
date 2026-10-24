package com.example.myapplication

import com.example.myapplication.domain.model.AvatarExpression
import com.example.myapplication.domain.model.DisappointmentLevel
import com.example.myapplication.domain.model.UserRole
import com.example.myapplication.domain.usecase.EvaluateDisappointmentUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class EvaluateDisappointmentUseCaseTest {

    private lateinit var useCase: EvaluateDisappointmentUseCase

    @Before
    fun setUp() {
        useCase = EvaluateDisappointmentUseCase()
    }

    @Test
    fun `使用時間が制限の50パーセント未満の場合は平常レベルであること`() {
        val result = useCase(
            totalUsedMillis = 20 * 60 * 1000L,
            limitMillis = 60 * 60 * 1000L,
            userRole = UserRole.INDIVIDUAL
        )

        assertEquals(DisappointmentLevel.LEVEL_0_SERENE, result.level)
        assertEquals(AvatarExpression.SMILE, result.message.expression)
        assertFalse(result.penaltyConfig.showOverlay)
    }

    @Test
    fun `使用時間が制限の50パーセント以上75パーセント未満の場合は小言レベルであること`() {
        val result = useCase(
            totalUsedMillis = 35 * 60 * 1000L,
            limitMillis = 60 * 60 * 1000L,
            userRole = UserRole.INDIVIDUAL
        )

        assertEquals(DisappointmentLevel.LEVEL_1_SUSPICIOUS, result.level)
        assertEquals(AvatarExpression.DOUBT, result.message.expression)
    }

    @Test
    fun `使用時間が制限の75パーセント以上100パーセント未満の場合はため息レベルであること`() {
        val result = useCase(
            totalUsedMillis = 50 * 60 * 1000L,
            limitMillis = 60 * 60 * 1000L,
            userRole = UserRole.INDIVIDUAL
        )

        assertEquals(DisappointmentLevel.LEVEL_2_SIGHING, result.level)
        assertEquals(AvatarExpression.SIGH, result.message.expression)
    }

    @Test
    fun `使用時間が制限の100パーセントを超えた場合は限界寸前の強い呆れレベルであること`() {
        val result = useCase(
            totalUsedMillis = 65 * 60 * 1000L,
            limitMillis = 60 * 60 * 1000L,
            userRole = UserRole.INDIVIDUAL
        )

        assertEquals(DisappointmentLevel.LEVEL_3_ANNOYED, result.level)
        assertEquals(AvatarExpression.GLARE, result.message.expression)
        assertTrue(result.penaltyConfig.showOverlay)
    }

    @Test
    fun `使用時間が制限の125パーセントを突破した場合は完全呆れ果て無言冷視レベルであること`() {
        val result = useCase(
            totalUsedMillis = 80 * 60 * 1000L,
            limitMillis = 60 * 60 * 1000L,
            userRole = UserRole.INDIVIDUAL
        )

        assertEquals(DisappointmentLevel.LEVEL_4_COLD_STARE, result.level)
        assertEquals(AvatarExpression.COLD_STARE, result.message.expression)
        assertTrue(result.penaltyConfig.showOverlay)
        assertTrue(result.penaltyConfig.blockTouchEvents) // 物理タップ無効化
    }

    @Test
    fun `親カスタムメッセージが設定されている場合はレベル3以上で上書きされること`() {
        val customMsg = "早く宿題をしなさい！"
        val result = useCase(
            totalUsedMillis = 70 * 60 * 1000L,
            limitMillis = 60 * 60 * 1000L,
            userRole = UserRole.CHILD,
            customMessageOverride = customMsg
        )

        assertEquals(customMsg, result.message.mainQuote)
        assertTrue(result.message.isCustom)
    }
}
