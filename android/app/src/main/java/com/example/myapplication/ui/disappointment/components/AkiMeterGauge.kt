package com.example.myapplication.ui.disappointment.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.domain.model.DisappointmentLevel
import com.example.myapplication.ui.theme.AkiLevel0Green
import com.example.myapplication.ui.theme.AkiLevel2Orange
import com.example.myapplication.ui.theme.AkiLevel3Red
import com.example.myapplication.ui.theme.AkiLevel4DarkPurple

/**
 * 呆れ度ゲージコンポーネント (AkiMeterGauge)
 * 0% 〜 100%+（限界突破）までの進捗をグラデーションバーで表現
 */
@Composable
fun AkiMeterGauge(
    level: DisappointmentLevel,
    usagePercentage: Float,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = usagePercentage.coerceIn(0f, 1.25f),
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "gauge_progress"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "親の呆れメーター",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Text(
                    text = level.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = level.color
                    )
                )
            }

            Text(
                text = "${(usagePercentage * 100).toInt()}%",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Black,
                    color = level.color,
                    fontSize = 22.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // プログレスバー本体
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(Color(0xFFE5E7EB))
        ) {
            val gaugeGradient = Brush.horizontalGradient(
                colors = listOf(
                    AkiLevel0Green,
                    AkiLevel2Orange,
                    AkiLevel3Red,
                    AkiLevel4DarkPurple
                )
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = (animatedProgress / 1.25f).coerceIn(0f, 1f))
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(gaugeGradient)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "0% (平常)", fontSize = 11.sp, color = Color.Gray)
            Text(text = "50% (ため息)", fontSize = 11.sp, color = Color.Gray)
            Text(text = "100% (制限)", fontSize = 11.sp, color = Color.Gray)
            Text(text = "125%+ (完全呆れ)", fontSize = 11.sp, color = Color.Gray)
        }
    }
}
