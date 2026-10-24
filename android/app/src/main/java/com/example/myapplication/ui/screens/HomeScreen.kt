package com.example.myapplication.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
    onPrepareMonitoringClick: () -> Unit,
    onStopMonitoringClick: () -> Unit,
    onQrClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OkanBackground)
    ) {
        OkanTopBar(onMoreClick = onQrClick)

        if (!isMonitoring) {
            // 通常の待機状態ホーム（画面2）
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "ホーム",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = OkanTextPrimary
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 中央の大きな状態カード
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
                                .size(54.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(OkanPrimaryContainer)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "待機中",
                                tint = OkanPrimary,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "ここにレベル（進行度）等",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = OkanTextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

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
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        } else {
            // 見守り実行中 / 警告状態（画面7）
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                // おかんイラスト
                Image(
                    painter = painterResource(id = R.drawable.okan_angry),
                    contentDescription = "怒るお母さん",
                    modifier = Modifier
                        .size(230.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 赤い強制停止ボタン
                Button(
                    onClick = onStopMonitoringClick,
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(48.dp),
                    shape = RoundedCornerShape(10.dp),
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

                Spacer(modifier = Modifier.height(24.dp))

                // メッセージボックス
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(OkanCardGray)
                        .padding(vertical = 28.dp, horizontal = 20.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "見守ります！！",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = OkanTextPrimary
                        )
                        Text(
                            text = "余計なアプリ見ちゃ\nだめよ！！",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = OkanTextPrimary,
                            textAlign = TextAlign.Center,
                            lineHeight = 28.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
