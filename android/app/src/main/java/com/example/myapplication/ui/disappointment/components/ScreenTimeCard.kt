package com.example.myapplication.ui.disappointment.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.domain.model.AppUsageInfo
import com.example.myapplication.domain.repository.HourlyScreenTime
import com.example.myapplication.ui.theme.OkanDanger
import com.example.myapplication.ui.theme.OkanPrimary
import com.example.myapplication.ui.theme.OkanPrimaryContainer
import com.example.myapplication.ui.theme.OkanSurface
import com.example.myapplication.ui.theme.OkanTextPrimary
import com.example.myapplication.ui.theme.OkanTextSecondary

/**
 * 測定されたスクリーンタイムと1時間ごとの縦積みグラフ表示カード
 * - 通知を無視した回数
 * - 見守り時間内の他事の時間
 * - 1時間ごとの縦積み棒グラフ（通常利用時間 + 他事時間）
 * （※「アプリ別スクリーンタイム」は非表示）
 */
@Composable
fun ScreenTimeCard(
    apps: List<AppUsageInfo> = emptyList(),
    hourlyData: List<HourlyScreenTime> = emptyList(),
    ignoredNotificationCount: Int = 0,
    totalDistractionMinutes: Int = 0,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = OkanSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = "本日のスクリーンタイム測定",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = OkanTextPrimary
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 重要な2つの指標バッジ（無視した回数 & 見守り内の他事時間）
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 指標1: 通知を無視した回数
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFF0F0))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.NotificationsOff,
                                contentDescription = null,
                                tint = OkanDanger,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "通知無視回数",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = OkanTextSecondary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${ignoredNotificationCount}回",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = OkanDanger
                        )
                    }
                }

                // 指標2: 見守り時間内の他事の時間
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(OkanPrimaryContainer)
                        .padding(12.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = OkanPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "見守り内他事",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = OkanTextSecondary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${totalDistractionMinutes}分",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = OkanPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 1時間ごとの縦積みグラフタイトル
            Text(
                text = "1時間ごとの利用推移（縦積みグラフ）",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = OkanTextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(OkanPrimary))
                Spacer(modifier = Modifier.width(4.dp))
                Text("通常使用", fontSize = 10.sp, color = OkanTextSecondary)
                Spacer(modifier = Modifier.width(12.dp))
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(OkanDanger))
                Spacer(modifier = Modifier.width(4.dp))
                Text("他事（監視対象）", fontSize = 10.sp, color = OkanTextSecondary)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 縦積み棒グラフ Canvas
            HourlyStackedBarChart(
                hourlyData = if (hourlyData.isNotEmpty()) hourlyData else getDummyHourlyData(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

/**
 * 1時間ごとの縦積みバーチャート
 */
@Composable
private fun HourlyStackedBarChart(
    hourlyData: List<HourlyScreenTime>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val totalBars = 24
        val maxMinutes = 60f // 1時間は最大60分
        val barWidth = (size.width / totalBars) * 0.65f
        val stepX = size.width / totalBars

        // 基準線
        drawLine(
            color = Color(0xFFEEEEEE),
            start = Offset(0f, size.height - 18.dp.toPx()),
            end = Offset(size.width, size.height - 18.dp.toPx()),
            strokeWidth = 1.dp.toPx()
        )

        val chartHeight = size.height - 24.dp.toPx()

        for (h in 0 until totalBars) {
            val item = hourlyData.firstOrNull { it.hour == h }
            val totalMins = item?.totalMinutes?.toFloat()?.coerceIn(0f, 60f) ?: 0f
            val distractMins = item?.distractionMinutes?.toFloat()?.coerceIn(0f, totalMins) ?: 0f
            val normalMins = (totalMins - distractMins).coerceAtLeast(0f)

            val x = h * stepX + (stepX - barWidth) / 2f
            val baseY = size.height - 18.dp.toPx()

            if (totalMins > 0) {
                // 1. 通常使用部分（Primary色）
                val normalHeight = (normalMins / maxMinutes) * chartHeight
                drawRoundRect(
                    color = OkanPrimary,
                    topLeft = Offset(x, baseY - normalHeight),
                    size = Size(barWidth, normalHeight),
                    cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                )

                // 2. その上に他事時間（縦積み・赤色）
                val distractHeight = (distractMins / maxMinutes) * chartHeight
                if (distractHeight > 0) {
                    drawRoundRect(
                        color = OkanDanger,
                        topLeft = Offset(x, baseY - normalHeight - distractHeight),
                        size = Size(barWidth, distractHeight),
                        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                    )
                }
            } else {
                // 背景のうっすらダミー線
                drawRoundRect(
                    color = Color(0xFFF3F4F8),
                    topLeft = Offset(x, baseY - 4.dp.toPx()),
                    size = Size(barWidth, 4.dp.toPx()),
                    cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                )
            }
        }
    }
}

private fun getDummyHourlyData(): List<HourlyScreenTime> {
    return (0..23).map { h ->
        val total = when (h) {
            in 7..8 -> 20
            12 -> 35
            13 -> 15
            in 17..18 -> 30
            in 19..21 -> 45
            else -> 0
        }
        val distract = (total * 0.45).toInt()
        HourlyScreenTime(h, total, distract)
    }
}
