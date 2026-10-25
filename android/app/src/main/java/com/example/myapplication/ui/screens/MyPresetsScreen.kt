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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.myapplication.ui.components.OkanTopBar
import com.example.myapplication.ui.model.MonitoringMode
import com.example.myapplication.ui.model.OkanPersonality
import com.example.myapplication.ui.model.PresetSetting
import com.example.myapplication.ui.theme.OkanBackground
import com.example.myapplication.ui.theme.OkanBorder
import com.example.myapplication.ui.theme.OkanCardGray
import com.example.myapplication.ui.theme.OkanPrimary
import com.example.myapplication.ui.theme.OkanPrimaryContainer
import com.example.myapplication.ui.theme.OkanSurface
import com.example.myapplication.ui.theme.OkanTextPrimary
import com.example.myapplication.ui.theme.OkanTextSecondary

@Composable
fun MyPresetsScreen(
    presets: List<PresetSetting>,
    onSavePreset: (preset: PresetSetting) -> Unit
) {
    val scrollState = rememberScrollState()
    var editingPreset by remember { mutableStateOf<PresetSetting?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OkanBackground)
    ) {
        OkanTopBar(title = "マイ設定")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "マイ設定登録",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = OkanTextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "よく使う見守り設定をあらかじめ登録・保存しておけます。",
                fontSize = 13.sp,
                color = OkanTextSecondary
            )

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                presets.forEach { preset ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(OkanSurface)
                            .border(1.dp, OkanBorder, RoundedCornerShape(16.dp))
                            .clickable { editingPreset = preset }
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(OkanPrimaryContainer)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Bookmark,
                                        contentDescription = null,
                                        tint = OkanPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Column(modifier = Modifier.padding(start = 14.dp)) {
                                    Text(
                                        text = preset.title,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = OkanTextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = "やること: ${preset.task.ifBlank { "未設定" }} (${preset.minutes}分)",
                                        fontSize = 12.sp,
                                        color = OkanTextSecondary
                                    )
                                    Text(
                                        text = "性格: ${preset.personality.label} / モード: ${preset.mode.label}",
                                        fontSize = 11.sp,
                                        color = OkanPrimary
                                    )
                                }
                            }

                            Button(
                                onClick = { editingPreset = preset },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = OkanCardGray,
                                    contentColor = OkanTextPrimary
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.size(4.dp))
                                Text("登録", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }

    // マイセット設定・登録ダイアログ
    editingPreset?.let { target ->
        var title by remember { mutableStateOf(target.title) }
        var task by remember { mutableStateOf(target.task) }
        var minutes by remember { mutableIntStateOf(target.minutes) }
        var personality by remember { mutableStateOf(target.personality) }
        var mode by remember { mutableStateOf(target.mode) }

        AlertDialog(
            onDismissRequest = { editingPreset = null },
            title = {
                Text("${target.title} の設定を登録", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("セット名", fontSize = 12.sp, color = OkanTextSecondary)
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("今から何をするの?", fontSize = 12.sp, color = OkanTextSecondary)
                    OutlinedTextField(
                        value = task,
                        onValueChange = { task = it },
                        placeholder = { Text("例: 勉強、読書、料理") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("制限時間（分）", fontSize = 12.sp, color = OkanTextSecondary)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(0, 15, 30, 45, 60).forEach { m ->
                            val isSelected = minutes == m
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) OkanPrimaryContainer else OkanCardGray)
                                    .clickable { minutes = m }
                            ) {
                                Text(
                                    text = "${m}分",
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) OkanPrimary else OkanTextPrimary
                                )
                            }
                        }
                    }

                    Text("おかんの性格", fontSize = 12.sp, color = OkanTextSecondary)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OkanPersonality.values().forEach { p ->
                            val isSelected = personality == p
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) OkanPrimaryContainer else OkanCardGray)
                                    .clickable { personality = p }
                            ) {
                                Text(
                                    text = p.label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) OkanPrimary else OkanTextPrimary
                                )
                            }
                        }
                    }

                    Text("見守りモード", fontSize = 12.sp, color = OkanTextSecondary)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MonitoringMode.values().forEach { m ->
                            val isSelected = mode == m
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) OkanPrimaryContainer else OkanCardGray)
                                    .clickable { mode = m }
                            ) {
                                Text(
                                    text = m.label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) OkanPrimary else OkanTextPrimary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSavePreset(
                            target.copy(
                                title = title,
                                task = task,
                                minutes = minutes,
                                personality = personality,
                                mode = mode
                            )
                        )
                        editingPreset = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OkanPrimary)
                ) {
                    Text("登録する")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingPreset = null }) {
                    Text("キャンセル")
                }
            }
        )
    }
}
