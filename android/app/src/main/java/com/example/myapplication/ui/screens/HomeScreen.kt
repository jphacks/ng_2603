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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.myapplication.domain.model.AppUsageInfo
import com.example.myapplication.domain.repository.HourlyScreenTime
import com.example.myapplication.ui.components.OkanTopBar
import com.example.myapplication.ui.disappointment.components.ScreenTimeCard
import com.example.myapplication.ui.model.UserRole
import com.example.myapplication.ui.theme.OkanBackground
import com.example.myapplication.ui.theme.OkanCardGray
import com.example.myapplication.ui.theme.OkanDanger
import com.example.myapplication.ui.theme.OkanPrimary
import com.example.myapplication.ui.theme.OkanPrimaryContainer
import com.example.myapplication.ui.theme.OkanSurface
import com.example.myapplication.ui.theme.OkanTextPrimary
import com.example.myapplication.ui.theme.OkanTextSecondary
import kotlinx.coroutines.flow.first

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

    // スクリーンタイム測定データ
    var appUsageList by remember { mutableStateOf<List<AppUsageInfo>>(emptyList()) }
    var hourlyData by remember { mutableStateOf<List<HourlyScreenTime>>(emptyList()) }
    var ignoredCount by remember { mutableIntStateOf(0) }
    var distractionMins by remember { mutableIntStateOf(0) }

    val scrollState = rememberScrollState()

    // 実際のスクリーンタイム・1時間ごと推移・無視回数・他事時間を測定・集計
    LaunchedEffect(Unit) {
        val apps = screenTimeRepo.getTodayAppUsageList()
        val hourly = screenTimeRepo.getHourlyScreenTime()
        val count = settingsRepo.violationCountFlow.first()
        val totalDistract = hourly.sumOf { it.distractionMinutes }

        appUsageList = apps
        hourlyData = hourly
        ignoredCount = count
        distractionMins = totalDistract
    }

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
            // 「見守り待機中」画面は廃止し、スクリーンタイム画面（縦積み棒グラフ & 指標集計）を直接表示
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "スクリーンタイム",
                        fontSize = 24.sp,
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

                Spacer(modifier = Modifier.height(14.dp))

                // スクリーンタイム詳細カード
                // ・実際のスクリーンタイムを測定し、1時間ごとの縦積み棒グラフ（Canvas描画）を表示
                // ・通知を無視した回数、見守り時間内の他事（余計なアプリ起動）時間を集計表示
                ScreenTimeCard(
                    apps = appUsageList,
                    hourlyData = hourlyData,
                    ignoredNotificationCount = ignoredCount,
                    totalDistractionMinutes = distractionMins
                )

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
                        text = "見守り開始設定へ",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        } else {
            // 見守り実行中 / 警告状態
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
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
                    modifier = Modifier.size(200.dp)
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
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = OkanTextPrimary,
                            textAlign = TextAlign.Center,
                            lineHeight = 26.sp
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

                Spacer(modifier = Modifier.height(24.dp))
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
