package com.example.myapplication.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import com.example.myapplication.ui.components.OkanTopBar
import com.example.myapplication.ui.model.UserRole
import com.example.myapplication.ui.theme.OkanBackground
import com.example.myapplication.ui.theme.OkanCardGray
import com.example.myapplication.ui.theme.OkanDanger
import com.example.myapplication.ui.theme.OkanPrimary
import com.example.myapplication.ui.theme.OkanPrimaryContainer
import com.example.myapplication.ui.theme.OkanSurface
import com.example.myapplication.ui.theme.OkanTextPrimary
import com.example.myapplication.ui.theme.OkanTextSecondary

@Composable
fun HomeScreen(
    isMonitoring: Boolean,
    userRole: UserRole = UserRole.INDIVIDUAL,
    durationMinutes: Int = 30,
    onPrepareMonitoringClick: () -> Unit,
    onStopMonitoringClick: () -> Unit,
    onQrClick: () -> Unit = {}
) {
    var showStopDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OkanBackground)
    ) {
        OkanTopBar(
            title = when {
                isMonitoring && userRole == UserRole.CHILD -> "見守り中（こども）"
                isMonitoring -> "見守り中"
                userRole == UserRole.PARENT -> "おかん通知（親）"
                userRole == UserRole.CHILD -> "おかん通知（こども）"
                else -> "おかん通知"
            },
            onMoreClick = onQrClick
        )

        if (!isMonitoring) {
            // 通常の待機状態ホーム（親 or 個人管理）
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ホーム",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = OkanTextPrimary
                    )

                    // ロールバッジ
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(OkanPrimaryContainer)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = userRole.title,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = OkanPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 中央の大きな状態カード（Figma画面2）
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(OkanCardGray)
                        .padding(24.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(OkanPrimaryContainer)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "待機中",
                                tint = OkanPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "ここにレベル（進行度）等",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = OkanTextSecondary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (userRole == UserRole.PARENT) {
                                "連携端末: お子様のスマホ 連携中"
                            } else {
                                "準備ができたら見守りを開始しましょう"
                            },
                            fontSize = 12.sp,
                            color = OkanTextSecondary.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 「見守り開始準備」ボタン
                Button(
                    onClick = onPrepareMonitoringClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OkanCardGray,
                        contentColor = OkanTextPrimary
                    )
                ) {
                    Text(
                        text = "見守り開始準備",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        } else {
            // 見守り実行中 / 警告状態（子供アカウント遷移先 or 見守り開始時）
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // ステータス表示
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(OkanPrimaryContainer)
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (userRole == UserRole.CHILD) "👶 こどもモード: 見守り中" else "👀 見守り中",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = OkanPrimary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // おかんイラスト
                Image(
                    painter = painterResource(id = R.drawable.okan_angry),
                    contentDescription = "怒るお母さん",
                    modifier = Modifier.size(220.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 赤い強制停止ボタン
                Button(
                    onClick = {
                        if (userRole == UserRole.CHILD) {
                            showStopDialog = true
                        } else {
                            onStopMonitoringClick()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OkanDanger,
                        contentColor = OkanSurface
                    )
                ) {
                    Text(
                        text = "強制停止ボタン",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // メッセージボックス
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(OkanCardGray)
                        .padding(vertical = 20.dp, horizontal = 20.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "見守ります！！",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = OkanTextPrimary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "余計なアプリ見ちゃ\nだめよ！！",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = OkanTextPrimary,
                            textAlign = TextAlign.Center,
                            lineHeight = 30.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "設定制限時間: ${durationMinutes}分",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = OkanTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // 強制停止確認ダイアログ
    if (showStopDialog) {
        AlertDialog(
            onDismissRequest = { showStopDialog = false },
            title = {
                Text(text = "見守りを強制停止しますか？", fontWeight = FontWeight.Bold)
            },
            text = {
                Text(text = "親御様のスマートフォンに強制停止の通知が送信されます。")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showStopDialog = false
                        onStopMonitoringClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OkanDanger)
                ) {
                    Text("停止する", color = OkanSurface)
                }
            },
            dismissButton = {
                TextButton(onClick = { showStopDialog = false }) {
                    Text("続ける", color = OkanTextPrimary)
                }
            }
        )
    }
}
