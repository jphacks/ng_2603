package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.components.OkanTopBar
import com.example.myapplication.ui.theme.OkanBackground
import com.example.myapplication.ui.theme.OkanCardGray
import com.example.myapplication.ui.theme.OkanTextPrimary
import com.example.myapplication.ui.theme.OkanTextSecondary

@Composable
fun NotificationSettingsScreen(
    currentSoundTitle: String,
    onSoundSettingClick: () -> Unit,
    onBackClick: () -> Unit
) {
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
                text = "設定 > 通知",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = OkanTextPrimary
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "通知に関する設定",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = OkanTextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(OkanCardGray)
                    .clickable(onClick = onSoundSettingClick)
                    .padding(horizontal = 18.dp, vertical = 18.dp)
            ) {
                Column {
                    Text(
                        text = "音",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = OkanTextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$currentSoundTitle / 変更",
                        fontSize = 12.sp,
                        color = OkanTextSecondary
                    )
                }
            }
        }
    }
}
