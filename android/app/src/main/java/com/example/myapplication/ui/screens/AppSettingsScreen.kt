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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.components.OkanTopBar
import com.example.myapplication.ui.model.AppNotificationTarget
import com.example.myapplication.ui.theme.OkanBackground
import com.example.myapplication.ui.theme.OkanBorder
import com.example.myapplication.ui.theme.OkanCardGray
import com.example.myapplication.ui.theme.OkanPrimary
import com.example.myapplication.ui.theme.OkanSurface
import com.example.myapplication.ui.theme.OkanTextPrimary
import com.example.myapplication.ui.theme.OkanTextSecondary

@Composable
fun AppSettingsScreen(
    apps: List<AppNotificationTarget>,
    onToggleApp: (id: String) -> Unit,
    onSelectAll: () -> Unit,
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
                text = "設定 > アプリの設定",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = OkanTextPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "一覧",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = OkanTextPrimary
                )
                TextButton(onClick = onSelectAll) {
                    Text(
                        text = "すべて選択 / 解除",
                        fontSize = 12.sp,
                        color = OkanTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                apps.forEach { app ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(OkanSurface)
                            .clickable { onToggleApp(app.id) }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            // アプリアイコン代わりの枠
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(OkanCardGray)
                            ) {
                                Text(
                                    text = app.name.take(1),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OkanTextPrimary
                                )
                            }

                            Column(
                                modifier = Modifier.padding(start = 14.dp)
                            ) {
                                Text(
                                    text = app.name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = OkanTextPrimary
                                )
                                Text(
                                    text = app.description,
                                    fontSize = 11.sp,
                                    color = OkanTextSecondary
                                )
                            }
                        }

                        if (app.isEnabled) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "選択済み",
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
