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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.myapplication.ui.theme.OkanPrimary
import com.example.myapplication.ui.theme.OkanSurface
import com.example.myapplication.ui.theme.OkanTextPrimary
import com.example.myapplication.ui.theme.OkanTextSecondary

@Composable
fun QuietHoursScreen(
    quietHours: List<QuietHour>,
    onToggleHour: (id: String) -> Unit,
    onUpdateHour: (id: String, timeRange: String, days: String) -> Unit = { _, _, _ -> },
    onAddHourClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    var editingHour by remember { mutableStateOf<QuietHour?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OkanBackground)
    ) {
        OkanTopBar(
            title = "使わない時間帯",
            canNavigateBack = true,
            onBackClick = onBackClick
        )

        // 画面に収まらない場合もスクロール可能
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "設定 > 使わない時間帯",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = OkanTextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "登録されている時間・曜日の間は常時見守りモードが作動します。",
                fontSize = 12.sp,
                color = OkanTextSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

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
                    text = "＋ 時間を追加",
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
                            // チェックボックス以外を触った場合は時間設定画面（ダイアログ）を開く
                            .clickable { editingHour = item }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
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

                        // 右端にチェックボックス（チェックボックスを触った場合はトグル）
                        Checkbox(
                            checked = item.isEnabled,
                            onCheckedChange = { onToggleHour(item.id) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = OkanPrimary
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // 時間設定画面（編集ダイアログ）
    editingHour?.let { target ->
        var editTimeRange by remember { mutableStateOf(target.timeRange) }
        var editDays by remember { mutableStateOf(target.days) }

        AlertDialog(
            onDismissRequest = { editingHour = null },
            title = {
                Text("使わない時間帯の設定", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("時間帯（例: 22:00 - 07:00）", fontSize = 12.sp, color = OkanTextSecondary)
                    OutlinedTextField(
                        value = editTimeRange,
                        onValueChange = { editTimeRange = it },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("適用曜日（例: 毎日、月〜金、土日など）", fontSize = 12.sp, color = OkanTextSecondary)
                    OutlinedTextField(
                        value = editDays,
                        onValueChange = { editDays = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateHour(target.id, editTimeRange, editDays)
                        editingHour = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OkanPrimary)
                ) {
                    Text("保存")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingHour = null }) {
                    Text("キャンセル")
                }
            }
        )
    }
}
