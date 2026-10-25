package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.components.DialTimePicker
import com.example.myapplication.ui.components.OkanTopBar
import com.example.myapplication.ui.model.MonitoringMode
import com.example.myapplication.ui.model.OkanPersonality
import com.example.myapplication.ui.theme.OkanBackground
import com.example.myapplication.ui.theme.OkanBorder
import com.example.myapplication.ui.theme.OkanPrimary
import com.example.myapplication.ui.theme.OkanPrimaryContainer
import com.example.myapplication.ui.theme.OkanSurface
import com.example.myapplication.ui.theme.OkanTextPrimary
import com.example.myapplication.ui.theme.OkanTextSecondary

@Composable
fun StartMonitoringScreen(
    initialProhibition: String = "",
    initialMinutes: Int = 30,
    initialPersonality: OkanPersonality = OkanPersonality.STANDARD,
    initialMode: MonitoringMode = MonitoringMode.AFTER_LIMIT,
    onStartMonitoringClick: (prohibition: String, minutes: Int, personality: OkanPersonality, mode: MonitoringMode) -> Unit,
    onBackClick: () -> Unit
) {
    var prohibitionText by remember { mutableStateOf(initialProhibition) }
    var selectedHour by remember { mutableIntStateOf(initialMinutes / 60) }
    var selectedMinute by remember { mutableIntStateOf(initialMinutes % 60) }
    var selectedPersonality by remember { mutableStateOf(initialPersonality) }
    var selectedMode by remember { mutableStateOf(initialMode) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OkanBackground)
    ) {
        // 上部バー
        OkanTopBar(
            title = "見守り設定",
            canNavigateBack = true,
            onBackClick = onBackClick
        )

        // スクロール可能な全体領域
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "見守り開始設定",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = OkanTextPrimary
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 見守り方式のタブ分け（経過後に始めるのか / その期間見守ってもらうのか）
            Text(
                text = "見守りモード",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = OkanTextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, OkanBorder, RoundedCornerShape(14.dp))
                    .background(OkanSurface)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                MonitoringMode.values().forEach { mode ->
                    val isSelected = selectedMode == mode
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) OkanPrimaryContainer else Color.Transparent)
                            .clickable { selectedMode = mode }
                    ) {
                        Text(
                            text = mode.label,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) OkanPrimary else OkanTextPrimary
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = selectedMode.description,
                fontSize = 11.sp,
                color = OkanTextSecondary,
                modifier = Modifier.padding(start = 4.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 項目1: 今から何をするの?（初期文なし、すぐ入力可能）
            Text(
                text = "今から何をするの?",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = OkanTextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = prohibitionText,
                onValueChange = { prohibitionText = it },
                placeholder = { Text("例: 勉強、読書、料理、仕事など") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OkanPrimary,
                    unfocusedBorderColor = OkanBorder,
                    focusedContainerColor = OkanSurface,
                    unfocusedContainerColor = OkanSurface
                ),
                singleLine = false,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 項目2: 制限時間セット（ドラムロール・ダイヤル式、0分可能）
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "制限時間セット",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = OkanTextPrimary
                )
                Text(
                    text = "合計: ${selectedHour * 60 + selectedMinute}分",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = OkanPrimary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, OkanBorder, RoundedCornerShape(16.dp))
                    .background(OkanSurface)
                    .padding(vertical = 10.dp)
            ) {
                DialTimePicker(
                    selectedHour = selectedHour,
                    selectedMinute = selectedMinute,
                    onTimeChanged = { hour, minute ->
                        selectedHour = hour
                        selectedMinute = minute
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 項目3: おかんのタイプ（性格）
            Text(
                text = "おかんのタイプ（性格）",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = OkanTextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))

            // 3つのセグメントボタン
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, OkanBorder, RoundedCornerShape(14.dp))
                    .background(OkanSurface)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                OkanPersonality.values().forEach { personality ->
                    val isSelected = selectedPersonality == personality
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) OkanPrimaryContainer else Color.Transparent)
                            .clickable { selectedPersonality = personality }
                    ) {
                        Text(
                            text = personality.label,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) OkanPrimary else OkanTextPrimary
                        )
                    }
                }
            }

            // おかんタイプの説明文（小さな余白に小さい文字で表示）
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = selectedPersonality.description,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = OkanTextSecondary,
                modifier = Modifier.padding(horizontal = 6.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 見守り開始ボタン（0分も可能、coerceAtLeast(0)）
            Button(
                onClick = {
                    val totalMinutes = (selectedHour * 60 + selectedMinute).coerceAtLeast(0)
                    val effectiveProhibition = if (prohibitionText.isBlank()) "指定なし" else prohibitionText
                    onStartMonitoringClick(effectiveProhibition, totalMinutes, selectedPersonality, selectedMode)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = OkanPrimary,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "見守り開始",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}
