package com.example.myapplication.ui.screens

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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.domain.model.AvatarExpression
import com.example.myapplication.domain.model.DisappointmentLevel
import com.example.myapplication.ui.components.OkanTopBar
import com.example.myapplication.ui.disappointment.MainDisappointmentScreen
import com.example.myapplication.ui.disappointment.components.ParentAvatarView
import com.example.myapplication.ui.theme.OkanBackground
import com.example.myapplication.ui.theme.OkanCardGray
import com.example.myapplication.ui.theme.OkanPrimary
import com.example.myapplication.ui.theme.OkanPrimaryContainer
import com.example.myapplication.ui.theme.OkanTextPrimary
import com.example.myapplication.ui.theme.OkanTextSecondary

@Composable
fun HomeScreen(
    isMonitoring: Boolean,
    onPrepareMonitoringClick: () -> Unit,
    onStopMonitoringClick: () -> Unit,
    onQrClick: () -> Unit = {}
) {
    if (isMonitoring) {
        // 見守り・監視中（統合メイン画面：親の呆れ演出、スクリーンタイム、段階的警告）
        MainDisappointmentScreen()
    } else {
        // 通常の待機状態ホーム
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(OkanBackground)
        ) {
            OkanTopBar(onMoreClick = onQrClick)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "ホーム",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = OkanTextPrimary
                )

                Spacer(modifier = Modifier.height(18.dp))

                // 中央の状態カード（親アバタープレビューと状態）
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
                        ParentAvatarView(
                            expression = AvatarExpression.SMILE,
                            level = DisappointmentLevel.LEVEL_0_SERENE,
                            modifier = Modifier.size(130.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "見守り待機中",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = OkanTextPrimary
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "使いすぎると親がだんだん呆れていきます。\n準備ができたら見守りを開始しましょう！",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal,
                            color = OkanTextSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 「見守り開始準備」ボタン
                Button(
                    onClick = onPrepareMonitoringClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OkanPrimary,
                        contentColor = androidx.compose.ui.graphics.Color.White
                    )
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "見守り開始準備",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
