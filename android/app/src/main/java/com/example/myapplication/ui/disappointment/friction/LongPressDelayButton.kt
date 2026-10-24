package com.example.myapplication.ui.disappointment.friction

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * 衝動的解除を防ぐ「30秒長押しディレイ」ボタン
 * 途中で指を離すと即座に進捗がリセットされ、冷静さを取り戻す時間を強制する
 */
@Composable
fun LongPressDelayButton(
    requiredSeconds: Int = 30,
    onCompleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    var currentProgress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            val stepInterval = 100L
            val totalSteps = (requiredSeconds * 1000L) / stepInterval
            var currentStep = 0
            while (isPressed && currentStep < totalSteps) {
                delay(stepInterval)
                currentStep++
                currentProgress = currentStep.toFloat() / totalSteps
            }
            if (isPressed && currentProgress >= 1.0f) {
                onCompleted()
                isPressed = false
                currentProgress = 0f
            }
        } else {
            currentProgress = 0f
        }
    }

    val animatedProgress by animateFloatAsState(
        targetValue = currentProgress,
        label = "button_progress"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFE2E8F0))
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    }
                )
            },
        contentAlignment = Alignment.CenterStart
    ) {
        // 充填プログレス
        Box(
            modifier = Modifier
                .fillMaxWidth(animatedProgress)
                .height(56.dp)
                .background(Color(0xFF4F46E5))
        )

        // テキスト表示
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            val remainingSec = (requiredSeconds * (1f - currentProgress)).toInt() + 1
            val label = if (isPressed) {
                "そのまま長押し中... (${remainingSec}秒)"
            } else {
                "制限を一時解除（${requiredSeconds}秒長押し）"
            }

            Text(
                text = label,
                color = if (animatedProgress > 0.45f) Color.White else Color(0xFF1E293B),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
