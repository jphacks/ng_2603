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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import com.example.myapplication.data.repository.ScreenTimeRepositoryImpl
import com.example.myapplication.data.repository.SettingsRepositoryImpl
import com.example.myapplication.domain.model.AvatarExpression
import com.example.myapplication.domain.model.DisappointmentLevel
import com.example.myapplication.ui.components.OkanTopBar
import com.example.myapplication.ui.disappointment.components.ParentAvatarView
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
    onResetStatusClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val settingsRepo = remember { SettingsRepositoryImpl(context) }
    val screenTimeRepo = remember { ScreenTimeRepositoryImpl(context, settingsRepo) }

    var showStopDialog by remember { mutableStateOf(false) }
    var todayScreenTimeMillis by remember { mutableLongStateOf(0L) }

    // 本日のスクリーンタイム取得
    LaunchedEffect(Unit) {
        val list = screenTimeRepo.getTodayAppUsageList()
        val total = list.sumOf { it.usedTimeMillis }
        todayScreenTimeMillis = total
    }

    val screenTimeHours = todayScreenTimeMillis / (1000 * 60 * 60)
    val screenTimeMinutes = (todayScreenTimeMillis % (1000 * 60 * 60)) / (1000 * 60)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OkanBackground)
    ) {
        OkanTopBar(
            title = when {
                isMonitoring && userRole == UserRole.CHILD -> "見守り中（こども）"
                isMonitoring && userRole == UserRole.PARENT -> "見守り中（親）"
                isMonitoring -> "見守り中"
                userRole == UserRole.PARENT -> "おかん通知（親）"
                userRole == UserRole.CHILD -> "おかん通知（こども）"
                else -> "おかん通知"
            },
            onResetStatusClick = onResetStatusClick
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

                Spacer(modifier = Modifier.height(18.dp))

                // 中央の大きな状態カード（待機中 ＆ スクリーンタイム表示）
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(OkanCardGray)
                        .padding(20.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // 親アバタープレビュー
                        ParentAvatarView(
                            expression = AvatarExpression.SMILE,
                            level = DisappointmentLevel.LEVEL_0_SERENE,
                            modifier = Modifier.size(115.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "見守り待機中",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = OkanTextPrimary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // スクリーンタイム表示バッジ
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(OkanSurface)
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "今日のスクリーンタイム: ${screenTimeHours}時間${screenTimeMinutes}分",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = OkanPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = if (userRole == UserRole.PARENT) {
                                "連携端末: お子様のスマホと連携中"
                            } else {
                                "使いすぎると親がだんだん呆れていきます。\n準備ができたら見守りを開始しましょう！"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            color = OkanTextSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 17.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 「見守り開始準備」ボタン
                Button(
                    onClick = onPrepareMonitoringClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OkanPrimary,
                        contentColor = OkanSurface
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "見守り開始準備",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        } else {
            // 見守り実行中 / 警告状態
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
                        text = when (userRole) {
                            UserRole.CHILD -> "👶 こどもモード: 見守り中"
                            UserRole.PARENT -> "👀 親モード: 見守り中"
                            else -> "👀 見守り中"
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = OkanPrimary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // おかんイラスト
                Image(
                    painter = painterResource(id = R.drawable.okan_angry),
                    contentDescription = "怒るお母さん",
                    modifier = Modifier.size(210.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 赤い強制停止ボタン（アプリおよびバックグラウンドごと終了）
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
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OkanDanger,
                        contentColor = OkanSurface
                    )
                ) {
                    Text(
                        text = "強制停止ボタン",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // メッセージボックス
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(OkanCardGray)
                        .padding(vertical = 16.dp, horizontal = 20.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // 親アカウントとその他で文言を切り替え
                        Text(
                            text = if (userRole == UserRole.PARENT) "見守っています！！" else "見守ります！！",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = OkanTextPrimary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = if (userRole == UserRole.PARENT) {
                                "余計なアプリを見ていたら\nお知らせします！！"
                            } else {
                                "余計なアプリ見ちゃ\nだめよ！！"
                            },
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = OkanTextPrimary,
                            textAlign = TextAlign.Center,
                            lineHeight = 28.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "設定制限時間: ${durationMinutes}分",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = OkanTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
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
                Text(text = "見守りサービスを停止し、アプリを完全に終了します。")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showStopDialog = false
                        onStopMonitoringClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OkanDanger)
                ) {
                    Text("停止して終了する", color = OkanSurface)
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
