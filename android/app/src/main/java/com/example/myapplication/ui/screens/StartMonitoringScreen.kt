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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import com.example.myapplication.ui.model.OkanPersonality
import com.example.myapplication.ui.theme.OkanBackground
import com.example.myapplication.ui.theme.OkanBorder
import com.example.myapplication.ui.theme.OkanCardGray
import com.example.myapplication.ui.theme.OkanPrimary
import com.example.myapplication.ui.theme.OkanPrimaryContainer
import com.example.myapplication.ui.theme.OkanSurface
import com.example.myapplication.ui.theme.OkanTextPrimary
import com.example.myapplication.ui.theme.OkanTextSecondary

@Composable
fun StartMonitoringScreen(
    onStartMonitoringClick: (prohibition: String, minutes: Int, personality: OkanPersonality) -> Unit,
    onBackClick: () -> Unit
) {
    var prohibitionText by remember { mutableStateOf("料理中にスマホいじっちゃダメ！") }
    var selectedHour by remember { mutableIntStateOf(0) }
    var selectedMinute by remember { mutableIntStateOf(30) }
    var selectedPersonality by remember { mutableStateOf(OkanPersonality.STANDARD) }

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

        // スクロール可能なフォーム領域（weight(1f) で画面下部ボタンと分離）
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
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

            Spacer(modifier = Modifier.height(20.dp))

            // 項目1: あなたに今やって欲しくないことは何？
            Text(
                text = "あなたに今やって欲しくないことは何？",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = OkanTextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = prohibitionText,
                onValueChange = { prohibitionText = it },
                placeholder = { Text("例: 料理中にスマホいじっちゃダメ！") },
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

            // 項目2: 制限時間セット（ドラムロール・ダイヤル式）
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

            // ダイヤルピッカーコンポーネント（上下ダイヤル式）
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
                            color = OkanTextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // 画面下部に常に固定される「見守り開始」ボタンエリア（絶対に隠れない）
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = OkanSurface,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Button(
                    onClick = {
                        val totalMinutes = (selectedHour * 60 + selectedMinute).coerceAtLeast(1)
                        onStartMonitoringClick(prohibitionText, totalMinutes, selectedPersonality)
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
            }
        }
    }
}
