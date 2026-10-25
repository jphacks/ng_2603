package com.example.myapplication.ui.screens

import android.content.pm.ApplicationInfo
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.components.OkanTopBar
import com.example.myapplication.ui.model.AppNotificationTarget
import com.example.myapplication.ui.theme.OkanBackground
import com.example.myapplication.ui.theme.OkanCardGray
import com.example.myapplication.ui.theme.OkanDanger
import com.example.myapplication.ui.theme.OkanPrimary
import com.example.myapplication.ui.theme.OkanSurface
import com.example.myapplication.ui.theme.OkanTextPrimary
import com.example.myapplication.ui.theme.OkanTextSecondary

@Composable
fun AppSettingsScreen(
    apps: List<AppNotificationTarget>,
    onToggleApp: (id: String) -> Unit,
    onAddApp: (name: String, packageName: String) -> Unit = { _, _ -> },
    onRemoveApp: (id: String) -> Unit = {},
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OkanBackground)
    ) {
        OkanTopBar(
            title = "アプリの設定",
            canNavigateBack = true,
            onBackClick = onBackClick
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "設定 > アプリの設定（ホワイトリスト）",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = OkanTextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "ホワイトリスト制：スイッチがONのアプリは、見守り時間中も通知が来ない（使用許可）アプリとして設定されます。",
                fontSize = 12.sp,
                color = OkanTextSecondary,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 「アプリを追加」ボタン
            Button(
                onClick = { showAddDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = OkanPrimary,
                    contentColor = OkanSurface
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "アプリを追加",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "登録済みアプリ一覧 (${apps.size}件)",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = OkanTextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (apps.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(OkanSurface)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "アプリが登録されていません。\n「アプリを追加」から登録してください。",
                            fontSize = 13.sp,
                            color = OkanTextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    apps.forEach { app ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(OkanSurface)
                                .padding(horizontal = 16.dp, vertical = 12.dp),
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
                                    Text(
                                        text = app.name.take(1),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = OkanTextPrimary
                                    )
                                }

                                Column(
                                    modifier = Modifier.padding(start = 12.dp)
                                ) {
                                    Text(
                                        text = app.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = OkanTextPrimary
                                    )
                                    Text(
                                        text = if (app.isEnabled) "通知なし（許可中）" else "通知あり（監視対象）",
                                        fontSize = 11.sp,
                                        color = if (app.isEnabled) OkanPrimary else OkanTextSecondary
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Switch(
                                    checked = app.isEnabled,
                                    onCheckedChange = { onToggleApp(app.id) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = OkanSurface,
                                        checkedTrackColor = OkanPrimary
                                    )
                                )

                                IconButton(
                                    onClick = { onRemoveApp(app.id) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "削除",
                                        tint = OkanDanger.copy(alpha = 0.7f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // アプリ追加ダイアログ
    if (showAddDialog) {
        var newAppName by remember { mutableStateOf("") }
        var newPackageName by remember { mutableStateOf("") }
        var isManualMode by remember { mutableStateOf(false) }

        // インストール済みアプリアイテム一覧を取得
        val installedApps = remember {
            val pm = context.packageManager
            pm.getInstalledApplications(0)
                .filter { (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 || it.packageName.contains("chrome") || it.packageName.contains("youtube") }
                .map { appInfo ->
                    val label = pm.getApplicationLabel(appInfo).toString()
                    label to appInfo.packageName
                }
                .sortedBy { it.first }
        }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(text = "ホワイトリストにアプリを追加", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (!isManualMode && installedApps.isNotEmpty()) {
                        Text(text = "インストール済みアプリから選択:", fontSize = 12.sp, color = OkanTextSecondary)
                        LazyColumn(modifier = Modifier.height(200.dp)) {
                            items(installedApps) { (name, pkg) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            newAppName = name
                                            newPackageName = pkg
                                        }
                                        .background(if (newPackageName == pkg) OkanCardGray else OkanSurface)
                                        .padding(vertical = 8.dp, horizontal = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = name, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                        TextButton(onClick = { isManualMode = true }) {
                            Text("手動で名前を入力する", fontSize = 12.sp)
                        }
                    } else {
                        OutlinedTextField(
                            value = newAppName,
                            onValueChange = {
                                newAppName = it
                                if (newPackageName.isBlank()) newPackageName = "custom.${it.lowercase()}"
                            },
                            label = { Text("アプリ名（例: 計算機、LINE）") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = newPackageName,
                            onValueChange = { newPackageName = it },
                            label = { Text("パッケージ名（例: com.example.app）") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (installedApps.isNotEmpty()) {
                            TextButton(onClick = { isManualMode = false }) {
                                Text("一覧から選ぶ", fontSize = 12.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newAppName.isNotBlank()) {
                            onAddApp(newAppName, newPackageName.ifBlank { "app.${newAppName.lowercase()}" })
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OkanPrimary)
                ) {
                    Text("追加する")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("キャンセル")
                }
            }
        )
    }
}
