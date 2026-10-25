package com.example.myapplication.ui.disappointment

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.domain.model.DisappointmentLevel
import com.example.myapplication.domain.model.UserRole
import com.example.myapplication.service.AppMonitoringService
import com.example.myapplication.ui.disappointment.components.AkiMeterGauge
import com.example.myapplication.ui.disappointment.components.ParentAvatarView
import com.example.myapplication.ui.disappointment.components.ScreenTimeCard
import com.example.myapplication.ui.disappointment.components.WarningBanner
import com.example.myapplication.ui.disappointment.friction.LongPressDelayButton
import com.example.myapplication.ui.disappointment.friction.MathTaskDialog

/**
 * 統合メイン画面 (MainDisappointmentScreen)
 * 個人モード、ペア（親・子）モードのすべてのユースケースを統合した中核画面
 */
@Composable
fun MainDisappointmentScreen() {
    val context = LocalContext.current
    val viewModel = remember {
        MainViewModel.Factory(context.applicationContext).create(MainViewModel::class.java)
    }
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    // 呆れ度合いに応じた背景グラデーション/コンテナ色
    val containerBgColor by animateColorAsState(
        targetValue = when (uiState.disappointmentLevel) {
            DisappointmentLevel.LEVEL_4_COLD_STARE -> Color(0xFF140F24)
            DisappointmentLevel.LEVEL_3_ANNOYED -> Color(0xFF281313)
            else -> MaterialTheme.colorScheme.background
        },
        animationSpec = tween(durationMillis = 600),
        label = "bg_color"
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        containerColor = containerBgColor
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. モード切り替えタブ（個人・親・子）
                RoleSelectorChips(
                    currentRole = uiState.role,
                    onRoleSelected = { viewModel.switchRole(it) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 2. 権限許可ステータス誘導カード（UsageStats / Overlay / Notification）
                if (!uiState.hasUsagePermission || !uiState.hasOverlayPermission) {
                    PermissionRequestCard(
                        hasUsage = uiState.hasUsagePermission,
                        hasOverlay = uiState.hasOverlayPermission,
                        onRequestUsage = {
                            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(intent)
                        },
                        onRequestOverlay = {
                            val intent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:${context.packageName}")
                            ).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(intent)
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 3. 親のキャラクターアバター表示（表情・アニメーション）
                Box(
                    modifier = Modifier.padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ParentAvatarView(
                        expression = uiState.message.expression,
                        level = uiState.disappointmentLevel
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 4. 吹き出しメッセージ（セリフとため息）
                SpeechBubbleSection(
                    quote = uiState.message.mainQuote,
                    subQuote = uiState.message.subQuote,
                    isColdStare = uiState.disappointmentLevel == DisappointmentLevel.LEVEL_4_COLD_STARE
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 5. 呆れメーターゲージ
                AkiMeterGauge(
                    level = uiState.disappointmentLevel,
                    usagePercentage = uiState.usagePercentage
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 6. スクリーンタイム詳細カード
                ScreenTimeCard(
                    apps = uiState.topApps,
                    hourlyData = uiState.hourlyData,
                    ignoredNotificationCount = uiState.ignoredNotificationCount,
                    totalDistractionMinutes = uiState.totalDistractionMinutes
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 7. モード別の操作アクション
                when (uiState.role) {
                    UserRole.INDIVIDUAL -> {
                        // 個人利用：甘え防止フリクションタスク
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "甘え防止（衝動的解除ブロック）",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            // 30秒長押しディレイボタン
                            LongPressDelayButton(
                                requiredSeconds = 30,
                                onCompleted = {
                                    viewModel.onFrictionTaskSucceeded()
                                }
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // 計算問題解除ボタン
                            OutlinedButton(
                                onClick = { viewModel.startUnlockChallenge() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text("計算問題を解いて解除する（思考摩擦）")
                            }
                        }
                    }

                    UserRole.PARENT -> {
                        // 親モード：即時介入・嫌がらせ発動ボタン
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFFECE5)
                            ),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "親専用コントロール",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFC62828)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "子の端末に対して、即座に「呆れ顔オーバーレイ（最前面固定）」と連続通知を発動させます。",
                                    fontSize = 13.sp,
                                    color = Color(0xFF444444)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { viewModel.triggerImmediateIntervention() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Warning, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("今すぐ嫌がらせ・オーバーレイを発動")
                                }
                            }
                        }
                    }

                    UserRole.CHILD -> {
                        // 子モード：親との約束ルール情報
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "親との約束ルール適用中",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "1日の上限は ${uiState.limitMinutes} 分です。制限を超えると自動的にスマホがロックされます。",
                                    fontSize = 13.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 常駐監視サービスの起動ボタン
                Button(
                    onClick = {
                        val serviceIntent = Intent(context, AppMonitoringService::class.java)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            context.startForegroundService(serviceIntent)
                        } else {
                            context.startService(serviceIntent)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("常駐監視ガードを稼働開始")
                }

                Spacer(modifier = Modifier.height(40.dp))
            }

            // 8. 画面上部ポップアップ警告バナー（フォアグラウンド時）
            WarningBanner(
                visible = uiState.showWarningBanner,
                level = uiState.disappointmentLevel,
                messageQuote = uiState.message.mainQuote,
                modifier = Modifier.align(Alignment.TopCenter)
            )

            // 9. 計算問題フリクションダイアログ
            if (uiState.isUnlockTaskVisible && uiState.currentMathProblem != null) {
                MathTaskDialog(
                    problem = uiState.currentMathProblem!!,
                    onDismiss = { viewModel.dismissUnlockDialog() },
                    onSubmitAnswer = { viewModel.onFrictionTaskSucceeded() }
                )
            }
        }
    }
}

@Composable
private fun RoleSelectorChips(
    currentRole: UserRole,
    onRoleSelected: (UserRole) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        FilterChip(
            selected = currentRole == UserRole.INDIVIDUAL,
            onClick = { onRoleSelected(UserRole.INDIVIDUAL) },
            label = { Text("個人モード") }
        )
        FilterChip(
            selected = currentRole == UserRole.PARENT,
            onClick = { onRoleSelected(UserRole.PARENT) },
            label = { Text("親モード") }
        )
        FilterChip(
            selected = currentRole == UserRole.CHILD,
            onClick = { onRoleSelected(UserRole.CHILD) },
            label = { Text("子モード") }
        )
    }
}

@Composable
private fun SpeechBubbleSection(
    quote: String,
    subQuote: String,
    isColdStare: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 3.dp, shape = RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(if (isColdStare) Color(0xFF221A33) else MaterialTheme.colorScheme.surface)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = quote,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = if (isColdStare) Color.White else MaterialTheme.colorScheme.onSurface,
                lineHeight = 28.sp
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = subQuote,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = if (isColdStare) Color(0xFFAAAAAA) else Color.Gray
            ),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PermissionRequestCard(
    hasUsage: Boolean,
    hasOverlay: Boolean,
    onRequestUsage: () -> Unit,
    onRequestOverlay: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF8E1)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "⚠️ 必要な権限が不足しています",
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF57F17)
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (!hasUsage) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "使用状況へのアクセス", fontSize = 13.sp)
                    Button(
                        onClick = onRequestUsage,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("許可する", fontSize = 12.sp)
                    }
                }
            }

            if (!hasOverlay) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "他のアプリの上に重ねて表示", fontSize = 13.sp)
                    Button(
                        onClick = onRequestOverlay,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("許可する", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
