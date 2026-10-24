package com.example.myapplication.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.components.OkanTopBar
import com.example.myapplication.ui.model.UserRole
import com.example.myapplication.ui.theme.OkanBackground
import com.example.myapplication.ui.theme.OkanBorder
import com.example.myapplication.ui.theme.OkanCardGray
import com.example.myapplication.ui.theme.OkanPrimary
import com.example.myapplication.ui.theme.OkanPrimaryContainer
import com.example.myapplication.ui.theme.OkanSurface
import com.example.myapplication.ui.theme.OkanTextPrimary
import com.example.myapplication.ui.theme.OkanTextSecondary

@Composable
fun QrScreen(
    userRole: UserRole,
    onPairingCompleted: () -> Unit,
    onBackClick: () -> Unit
) {
    // 親の場合はデフォルトでQR表示、子供の場合はカメラ読み込み
    var isShowingQr by remember { mutableStateOf(userRole == UserRole.PARENT) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OkanBackground)
    ) {
        OkanTopBar(
            canNavigateBack = true,
            onBackClick = onBackClick
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // タイトル
            Text(
                text = if (userRole == UserRole.PARENT) "親と子供の共有設定（親）" else "親と子供の共有設定（こども）",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = OkanTextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 説明文
            Text(
                text = if (userRole == UserRole.PARENT) {
                    "お子様のスマホの「おかん通知」でこのQRコードを読み取ってください。"
                } else {
                    "親御様のスマホに表示されたQRコードを枠内に合わせて読み取ってください。"
                },
                fontSize = 14.sp,
                color = OkanTextSecondary,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 表示切替タブ
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(OkanCardGray)
                    .padding(4.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isShowingQr) OkanSurface else Color.Transparent)
                        .clickable { isShowingQr = true }
                ) {
                    Text(
                        text = "QRコードを表示",
                        fontSize = 13.sp,
                        fontWeight = if (isShowingQr) FontWeight.Bold else FontWeight.Normal,
                        color = OkanTextPrimary
                    )
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (!isShowingQr) OkanSurface else Color.Transparent)
                        .clickable { isShowingQr = false }
                ) {
                    Text(
                        text = "カメラで読み取る",
                        fontSize = 13.sp,
                        fontWeight = if (!isShowingQr) FontWeight.Bold else FontWeight.Normal,
                        color = OkanTextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 中央のコンテンツ領域
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(OkanCardGray)
                    .padding(20.dp)
            ) {
                if (isShowingQr) {
                    // QRコード表示モード
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(210.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(OkanSurface)
                                .border(1.dp, OkanBorder, RoundedCornerShape(16.dp))
                                .padding(16.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                // リアルなQRコード風Canvas
                                QrCodeCanvas(modifier = Modifier.size(150.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "ペアリングコード: 839-204",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = OkanTextPrimary
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "お子様の端末で上記のQRコードをスキャンしてください",
                            fontSize = 12.sp,
                            color = OkanTextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    // カメラ読み取りモード
                    CameraScannerMock(
                        onScanSimulated = onPairingCompleted
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ペアリング完了 / アクションボタン
            Button(
                onClick = onPairingCompleted,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = OkanPrimary,
                    contentColor = Color.White
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (userRole == UserRole.PARENT) {
                            "設定完了（ホームへ）"
                        } else {
                            "読み取り完了（見守りを開始）"
                        },
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

/**
 * QRコードを模したCanvas描画
 */
@Composable
private fun QrCodeCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val gridSize = 19
        val cellSize = size.width / gridSize

        // 背景
        drawRect(Color.White)

        // 角の3つのマーカー（位置検出パターン）
        fun drawFinderPattern(rowStart: Int, colStart: Int) {
            // 外枠 7x7
            drawRect(
                color = Color.Black,
                topLeft = Offset(colStart * cellSize, rowStart * cellSize),
                size = Size(7 * cellSize, 7 * cellSize)
            )
            // 内側白 5x5
            drawRect(
                color = Color.White,
                topLeft = Offset((colStart + 1) * cellSize, (rowStart + 1) * cellSize),
                size = Size(5 * cellSize, 5 * cellSize)
            )
            // 中心黒 3x3
            drawRect(
                color = Color.Black,
                topLeft = Offset((colStart + 2) * cellSize, (rowStart + 2) * cellSize),
                size = Size(3 * cellSize, 3 * cellSize)
            )
        }

        drawFinderPattern(0, 0)
        drawFinderPattern(0, gridSize - 7)
        drawFinderPattern(gridSize - 7, 0)

        // ランダム風のデータドットパターン（固定シード）
        val pattern = listOf(
            0b1011001010110101001,
            0b0100110101001011010,
            0b1101001011010010111,
            0b0010110100101101000,
            0b1011010011010010101,
            0b0100101100101101010,
            0b1110010010110010111,
            0b0101101010011010010,
            0b1010010101100101101,
            0b0110101100101101010,
            0b1001010011010010101
        )

        for (r in 8..(gridSize - 8)) {
            val rowBits = pattern[(r - 8) % pattern.size]
            for (c in 0 until gridSize) {
                if ((rowBits and (1 shl (c % 19))) != 0) {
                    drawRect(
                        color = Color.Black,
                        topLeft = Offset(c * cellSize, r * cellSize),
                        size = Size(cellSize, cellSize)
                    )
                }
            }
        }
    }
}

/**
 * カメラファインダー風のスキャン画面モック
 */
@Composable
private fun CameraScannerMock(
    onScanSimulated: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "scanLine")
    val scanProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scanProgress"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(200.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1E2026))
                .border(2.dp, OkanPrimary, RoundedCornerShape(16.dp))
        ) {
            // スキャンバー（上下移動アニメーション）
            Canvas(modifier = Modifier.fillMaxSize()) {
                val lineY = size.height * scanProgress
                drawLine(
                    color = Color(0xFF4A6EF5).copy(alpha = 0.8f),
                    start = Offset(0f, lineY),
                    end = Offset(size.width, lineY),
                    strokeWidth = 4.dp.toPx()
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "📷",
                    fontSize = 32.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "カメラ起動中...\nQRコードを枠に合わせてください",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "親御様のスマホのQRコードを\nカメラの枠内に収めると自動認識されます",
            fontSize = 12.sp,
            color = OkanTextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )
    }
}
