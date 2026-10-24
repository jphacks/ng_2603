package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.components.OkanTopBar
import com.example.myapplication.ui.model.UserRole
import com.example.myapplication.ui.theme.OkanBackground
import com.example.myapplication.ui.theme.OkanCardGray
import com.example.myapplication.ui.theme.OkanPrimaryContainer
import com.example.myapplication.ui.theme.OkanTextPrimary
import com.example.myapplication.ui.theme.OkanTextSecondary

@Composable
fun WelcomeScreen(
    onRoleSelected: (UserRole) -> Unit
) {
    var selectedRole by remember { mutableStateOf<UserRole>(UserRole.INDIVIDUAL) }

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
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = OkanTextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "あなたの使い方を教えてね",
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                color = OkanTextSecondary
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 3つのステータス選択カード
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                UserRole.values().forEach { role ->
                    val isSelected = selectedRole == role
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) OkanPrimaryContainer else OkanCardGray)
                            .clickable {
                                selectedRole = role
                            }
                            .padding(horizontal = 20.dp, vertical = 20.dp)
                    ) {
                        Column {
                            Text(
                                text = role.title,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = OkanTextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = role.description,
                                fontSize = 12.sp,
                                color = OkanTextSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    onRoleSelected(selectedRole)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = OkanCardGray,
                    contentColor = OkanTextPrimary
                )
            ) {
                Text(
                    text = "次へ進む",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}
