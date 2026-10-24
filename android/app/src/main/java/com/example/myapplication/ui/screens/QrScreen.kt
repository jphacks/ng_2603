package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
    // 親の場合はデフォルトでQR表示、子の場合はデフォルトでカメラ読み込み
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

            Text(
                text = if (userRole == UserRole.PARENT) "親と子供の共有設定（親）" else "親と子供の共有設定（こども）",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = OkanTextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (userRole == UserRole.PARENT) {
                    "お子様のスマホの「おかん通知」でこのQRコードを読み取ってください。"
                } else {
                    "親御様のスマホに表示されたQRコードを枠内に合わせて読み取ってください。"
                },
                fontSize = 13.sp,
                color = OkanTextSecondary,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 中央のカメラ/QR領域（Figmaの画面5/6を再現）
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(OkanCardGray)
                    .padding(24.dp)
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
                                .size(180.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(OkanSurface)
                                .border(2.dp, OkanBorder, RoundedCornerShape(16.dp))
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "⬛ ⬛ ⬛\n⬛ 📱 ⬛\n⬛ ⬛ ⬛",
                                    fontSize = 26.sp,
                                    lineHeight = 28.sp,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "ペアリングQRコード",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OkanTextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "ここにQRコードを読み込むカメラorQRコードを\n表示させる",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = OkanTextSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                    }
                } else {
                    // カメラ読み取りモード
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(200.dp)
                                .border(2.dp, OkanPrimary, RoundedCornerShape(16.dp))
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "📷\n\nカメラ起動中...\nQRコードを枠に合わせてください",
                                fontSize = 13.sp,
                                color = OkanTextPrimary,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "ここにQRコードを読み込むカメラorQRコードを\n表示させる",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = OkanTextSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 切り替えボタン
            OutlinedButton(
                onClick = { isShowingQr = !isShowingQr },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (isShowingQr) "カメラで読み込む" else "QRコードを表示",
                    fontSize = 14.sp,
                    color = OkanTextPrimary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ペアリング完了ボタン
            Button(
                onClick = onPairingCompleted,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = OkanPrimary,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = if (userRole == UserRole.PARENT) {
                        "設定完了（ホームへ）"
                    } else {
                        "設定完了（見守りを開始）"
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
