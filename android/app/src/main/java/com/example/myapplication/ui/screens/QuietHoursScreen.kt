package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.components.OkanTopBar
import com.example.myapplication.ui.model.QuietHour
import com.example.myapplication.ui.theme.OkanBackground
import com.example.myapplication.ui.theme.OkanCardGray
import com.example.myapplication.ui.theme.OkanSurface
import com.example.myapplication.ui.theme.OkanTextPrimary
import com.example.myapplication.ui.theme.OkanTextSecondary

@Composable
fun QuietHoursScreen(
    quietHours: List<QuietHour>,
    onToggleHour: (id: String) -> Unit,
    onAddHourClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OkanBackground)
    ) {
        OkanTopBar(
            canNavigateBack = true,
            onBackClick = onBackClick
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "設定 > 使わない時間帯",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = OkanTextPrimary
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 「時間を追加」ボタン
            Button(
                onClick = onAddHourClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = OkanSurface,
                    contentColor = OkanTextPrimary
                )
            ) {
                Text(
                    text = "時間を追加",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "登録済みの時間帯",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = OkanTextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                quietHours.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(OkanSurface)
                            .clickable { onToggleHour(item.id) }
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(OkanCardGray)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = OkanTextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Column(
                                modifier = Modifier.padding(start = 14.dp)
                            ) {
                                Text(
                                    text = item.timeRange,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OkanTextPrimary
                                )
                                Text(
                                    text = item.days,
                                    fontSize = 11.sp,
                                    color = OkanTextSecondary
                                )
                            }
                        }

                        if (item.isEnabled) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "有効",
                                tint = OkanTextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
