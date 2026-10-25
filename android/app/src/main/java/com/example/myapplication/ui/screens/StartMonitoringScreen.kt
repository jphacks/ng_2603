package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.example.myapplication.ui.theme.OkanCardGray
import com.example.myapplication.ui.theme.OkanPrimary
import com.example.myapplication.ui.theme.OkanPrimaryContainer
import com.example.myapplication.ui.theme.OkanSurface
import com.example.myapplication.ui.theme.OkanTabInactive
import com.example.myapplication.ui.theme.OkanTextPrimary
import com.example.myapplication.ui.theme.OkanTextSecondary

/**
 * 見守り開始設定画面 (Figma完全準拠UI)
 * - タブ: 「経過後通知」（初期選択） / 「時間内使用で通知」
 * - 項目1: 「あなたがやらなくてはいけないことは何？」（初期文なし・プレースホルダー「任意でご記入ください」）
 * - 項目2: 「何時間やるの？」（ダイヤル式ピッカー・1分単位・4つの等幅クイック選択）
 * - 項目3: 「お母さんのくどさ（通知）」（激アマ / 普通 / 鬼 の角丸ボタン + 説明文）
 * - ボタン: 「見守り開始」
 */
@Composable
fun StartMonitoringScreen(
    initialProhibition: String = "",
    initialMinutes: Int = 30,
    initialPersonality: OkanPersonality = OkanPersonality.STANDARD,
    initialMode: MonitoringMode = MonitoringMode.AFTER_LIMIT,
    onStartMonitoringClick: (prohibition: String, minutes: Int, personality: OkanPersonality, mode: MonitoringMode) -> Unit,
    onBackClick: () -> Unit = {}
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
        // トップバー: 「おかん通知」、右側三点メニューアイコン
        OkanTopBar(
            title = "おかん通知",
            canNavigateBack = false
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            // 大見出し
            Text(
                text = "見守り開始設定",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = OkanTextPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // タブ切り替え: 「経過後通知」 / 「時間内使用で通知」
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                MonitoringMode.values().forEach { mode ->
                    val isSelected = selectedMode == mode
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                selectedMode = mode
                            },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = mode.label,
                            fontSize = 15.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) OkanPrimary else OkanTabInactive,
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                        // 選択中インジケータ（下線バー）
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .background(
                                    color = if (isSelected) OkanPrimary else Color.Transparent,
                                    shape = RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp)
                                )
                        )
                    }
                }
            }
            // タブ下部の薄いボーダー線
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(OkanBorder.copy(alpha = 0.6f))
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 項目1: あなたがやらなくてはいけないことは何？
            Text(
                text = "あなたがやらなくてはいけないことは何？",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = OkanTextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = prohibitionText,
                onValueChange = { prohibitionText = it },
                placeholder = {
                    Text(
                        text = "任意でご記入ください",
                        fontSize = 14.sp,
                        color = OkanTextSecondary.copy(alpha = 0.5f)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OkanPrimary,
                    unfocusedBorderColor = OkanBorder,
                    focusedContainerColor = OkanSurface,
                    unfocusedContainerColor = OkanSurface
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 項目2: 何時間やるの？
            Text(
                text = "何時間やるの？",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = OkanTextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ダイヤルカード（白背景・角丸・枠線）
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(OkanSurface)
                    .border(1.dp, OkanBorder, RoundedCornerShape(16.dp))
                    .padding(vertical = 12.dp)
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

            // 項目3: お母さんのくどさ（通知）
            Text(
                text = "お母さんのくどさ（通知）",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = OkanTextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 3連の独立ボタン（激アマ / 普通 / 鬼）
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OkanPersonality.values().forEach { personality ->
                    val isSelected = selectedPersonality == personality
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) OkanPrimaryContainer else OkanSurface)
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) OkanPrimary else OkanBorder,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                selectedPersonality = personality
                            }
                    ) {
                        Text(
                            text = personality.label,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) OkanPrimary else OkanTextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 選択された性格の説明文（小さい文字で表示）
            Text(
                text = selectedPersonality.description,
                fontSize = 12.sp,
                color = OkanTextSecondary,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // 見守り開始ボタン
            Button(
                onClick = {
                    val totalMinutes = selectedHour * 60 + selectedMinute
                    onStartMonitoringClick(prohibitionText, totalMinutes, selectedPersonality, selectedMode)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = OkanCardGray,
                    contentColor = OkanTextPrimary
                )
            ) {
                Text(
                    text = "見守り開始",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
