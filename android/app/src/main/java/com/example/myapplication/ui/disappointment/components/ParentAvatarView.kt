package com.example.myapplication.ui.disappointment.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.myapplication.domain.model.AvatarExpression
import com.example.myapplication.domain.model.DisappointmentLevel

/**
 * 親のアバター描画コンポーネント
 * 呆れ度合いと表情に応じた視線やまゆ毛、ため息、無言の凝視アニメーションを描画する
 */
@Composable
fun ParentAvatarView(
    expression: AvatarExpression,
    level: DisappointmentLevel,
    modifier: Modifier = Modifier
) {
    // レベル4（無言の冷視）時の微小な揺れ・まばたきアニメーション
    val infiniteTransition = rememberInfiniteTransition(label = "stare_animation")
    val eyePupilShift by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pupil_shift"
    )

    // ため息時の肩の上下アニメーション
    val sighOffsetY = remember { Animatable(0f) }
    LaunchedEffect(expression) {
        if (expression == AvatarExpression.SIGH) {
            sighOffsetY.animateTo(
                targetValue = 8f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
        } else {
            sighOffsetY.snapTo(0f)
        }
    }

    Box(
        modifier = modifier.size(160.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(150.dp)) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val center = Offset(canvasWidth / 2, canvasHeight / 2 + sighOffsetY.value)
            val faceRadius = canvasWidth * 0.42f

            // 1. 背景のオーラ／後光（呆れレベルに応じて不穏な円）
            val auraColor = level.color.copy(alpha = 0.18f)
            drawCircle(
                color = auraColor,
                radius = faceRadius * 1.15f,
                center = center
            )

            // 2. 輪郭（顔のベース）
            val faceColor = when (expression) {
                AvatarExpression.COLD_STARE -> Color(0xFFE2E4EB) // やや青ざめた肌色
                AvatarExpression.GLARE -> Color(0xFFFFECE5)
                else -> Color(0xFFFFF4E8)
            }
            drawCircle(
                color = faceColor,
                radius = faceRadius,
                center = center
            )
            drawCircle(
                color = Color(0xFF333333),
                radius = faceRadius,
                center = center,
                style = Stroke(width = 4.dp.toPx())
            )

            // 3. 髪型（お母さん・お父さんの親しみやすいパーマ／短髪風）
            drawArc(
                color = Color(0xFF3D322A),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(center.x - faceRadius, center.y - faceRadius * 1.05f),
                size = Size(faceRadius * 2, faceRadius * 1.2f)
            )

            val eyeY = center.y - faceRadius * 0.1f
            val eyeSpacing = faceRadius * 0.45f

            // 4. まゆ毛（感情の要）
            val browY = eyeY - 18.dp.toPx()
            when (expression) {
                AvatarExpression.SMILE -> {
                    // 穏やかなアーチ
                    drawLine(
                        color = Color(0xFF333333),
                        start = Offset(center.x - eyeSpacing - 12.dp.toPx(), browY + 4.dp.toPx()),
                        end = Offset(center.x - eyeSpacing + 12.dp.toPx(), browY),
                        strokeWidth = 3.5.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = Color(0xFF333333),
                        start = Offset(center.x + eyeSpacing - 12.dp.toPx(), browY),
                        end = Offset(center.x + eyeSpacing + 12.dp.toPx(), browY + 4.dp.toPx()),
                        strokeWidth = 3.5.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
                AvatarExpression.DOUBT, AvatarExpression.SIGH -> {
                    // 片眉上げ・困り眉
                    drawLine(
                        color = Color(0xFF333333),
                        start = Offset(center.x - eyeSpacing - 12.dp.toPx(), browY),
                        end = Offset(center.x - eyeSpacing + 12.dp.toPx(), browY + 8.dp.toPx()),
                        strokeWidth = 4.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = Color(0xFF333333),
                        start = Offset(center.x + eyeSpacing - 12.dp.toPx(), browY + 8.dp.toPx()),
                        end = Offset(center.x + eyeSpacing + 12.dp.toPx(), browY),
                        strokeWidth = 4.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
                AvatarExpression.GLARE, AvatarExpression.COLD_STARE -> {
                    // 鋭い八の字・水平の硬い眉
                    val angleOffset = if (expression == AvatarExpression.COLD_STARE) 2.dp.toPx() else 10.dp.toPx()
                    drawLine(
                        color = Color(0xFF222222),
                        start = Offset(center.x - eyeSpacing - 14.dp.toPx(), browY),
                        end = Offset(center.x - eyeSpacing + 12.dp.toPx(), browY + angleOffset),
                        strokeWidth = 5.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = Color(0xFF222222),
                        start = Offset(center.x + eyeSpacing - 12.dp.toPx(), browY + angleOffset),
                        end = Offset(center.x + eyeSpacing + 14.dp.toPx(), browY),
                        strokeWidth = 5.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }

            // 5. 目（視線の演出）
            when (expression) {
                AvatarExpression.SMILE -> {
                    // ニコニコ三日月目
                    drawArc(
                        color = Color(0xFF333333),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(center.x - eyeSpacing - 10.dp.toPx(), eyeY - 8.dp.toPx()),
                        size = Size(20.dp.toPx(), 16.dp.toPx()),
                        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = Color(0xFF333333),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(center.x + eyeSpacing - 10.dp.toPx(), eyeY - 8.dp.toPx()),
                        size = Size(20.dp.toPx(), 16.dp.toPx()),
                        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                AvatarExpression.COLD_STARE -> {
                    // 完全な点目＋虚無の視線（瞳が少し動く）
                    drawCircle(
                        color = Color(0xFF111111),
                        radius = 4.5.dp.toPx(),
                        center = Offset(center.x - eyeSpacing + eyePupilShift, eyeY)
                    )
                    drawCircle(
                        color = Color(0xFF111111),
                        radius = 4.5.dp.toPx(),
                        center = Offset(center.x + eyeSpacing + eyePupilShift, eyeY)
                    )
                }
                else -> {
                    // 通常〜じと目
                    val eyeRadius = if (expression == AvatarExpression.GLARE) 6.dp.toPx() else 7.5.dp.toPx()
                    drawCircle(
                        color = Color(0xFF222222),
                        radius = eyeRadius,
                        center = Offset(center.x - eyeSpacing, eyeY)
                    )
                    drawCircle(
                        color = Color(0xFF222222),
                        radius = eyeRadius,
                        center = Offset(center.x + eyeSpacing, eyeY)
                    )
                    // ハイライト（光）
                    if (expression != AvatarExpression.GLARE) {
                        drawCircle(
                            color = Color.White,
                            radius = 2.dp.toPx(),
                            center = Offset(center.x - eyeSpacing - 2.dp.toPx(), eyeY - 2.dp.toPx())
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 2.dp.toPx(),
                            center = Offset(center.x + eyeSpacing - 2.dp.toPx(), eyeY - 2.dp.toPx())
                        )
                    }
                }
            }

            // 6. 口元
            val mouthY = center.y + faceRadius * 0.45f
            when (expression) {
                AvatarExpression.SMILE -> {
                    drawArc(
                        color = Color(0xFF333333),
                        startAngle = 0f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(center.x - 14.dp.toPx(), mouthY - 6.dp.toPx()),
                        size = Size(28.dp.toPx(), 14.dp.toPx()),
                        style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                AvatarExpression.SIGH -> {
                    // 丸く開けたため息口 (O字)
                    drawOval(
                        color = Color(0xFF555555),
                        topLeft = Offset(center.x - 7.dp.toPx(), mouthY - 4.dp.toPx()),
                        size = Size(14.dp.toPx(), 18.dp.toPx())
                    )
                }
                AvatarExpression.COLD_STARE, AvatarExpression.GLARE -> {
                    // 真っ直ぐなへの字口
                    drawLine(
                        color = Color(0xFF222222),
                        start = Offset(center.x - 16.dp.toPx(), mouthY + 2.dp.toPx()),
                        end = Offset(center.x + 16.dp.toPx(), mouthY + 2.dp.toPx()),
                        strokeWidth = 3.5.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
                else -> {
                    // 少し不満げな口
                    drawLine(
                        color = Color(0xFF444444),
                        start = Offset(center.x - 10.dp.toPx(), mouthY),
                        end = Offset(center.x + 10.dp.toPx(), mouthY),
                        strokeWidth = 3.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }
}
