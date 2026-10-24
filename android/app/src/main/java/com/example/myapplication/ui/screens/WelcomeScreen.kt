package com.example.myapplication.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.components.OkanTopBar
import com.example.myapplication.ui.model.UserRole
import com.example.myapplication.ui.theme.OkanBackground
import com.example.myapplication.ui.theme.OkanCardGray
import com.example.myapplication.ui.theme.OkanPrimary
import com.example.myapplication.ui.theme.OkanPrimaryContainer
import com.example.myapplication.ui.theme.OkanTextPrimary
import com.example.myapplication.ui.theme.OkanTextSecondary

@Composable
fun WelcomeScreen(
    onRoleSelected: (UserRole) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OkanBackground)
    ) {
        OkanTopBar()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "はじめまして",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = OkanTextPrimary
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "あなたの使い方を教えてね",
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = OkanTextSecondary
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 3つのステータス選択ボタン（上から: こども, 親, 個人管理）
            Column(
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // 1. こども（お子様のスマホに入れる場合） -> QRコード画面へ
                RoleSelectionCard(
                    role = UserRole.CHILD,
                    icon = Icons.Default.Face,
                    onClick = { onRoleSelected(UserRole.CHILD) }
                )

                // 2. 親（お子様のスマホを管理する場合） -> QRコード画面へ
                RoleSelectionCard(
                    role = UserRole.PARENT,
                    icon = Icons.Default.Home,
                    onClick = { onRoleSelected(UserRole.PARENT) }
                )

                // 3. 個人管理（自分自身のスマホを管理する場合） -> ホームへ
                RoleSelectionCard(
                    role = UserRole.INDIVIDUAL,
                    icon = Icons.Default.Person,
                    onClick = { onRoleSelected(UserRole.INDIVIDUAL) }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "※設定は後からいつでも変更できます",
                fontSize = 12.sp,
                color = OkanTextSecondary.copy(alpha = 0.7f),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 24.dp)
            )
        }
    }
}

@Composable
private fun RoleSelectionCard(
    role: UserRole,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = OkanCardGray
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 22.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // アイコン丸バッジ
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(OkanPrimaryContainer)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = role.title,
                    tint = OkanPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(0.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = role.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = OkanTextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = role.description,
                    fontSize = 13.sp,
                    color = OkanTextSecondary,
                    lineHeight = 18.sp
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "選択",
                tint = OkanTextSecondary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
