package com.example.myapplication.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.model.TabItem
import com.example.myapplication.ui.theme.OkanBackground
import com.example.myapplication.ui.theme.OkanBorder
import com.example.myapplication.ui.theme.OkanPrimaryContainer
import com.example.myapplication.ui.theme.OkanTabInactive
import com.example.myapplication.ui.theme.OkanTextPrimary

@Composable
fun OkanBottomBar(
    currentTab: TabItem,
    onTabSelected: (TabItem) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(OkanBackground)
            .navigationBarsPadding()
    ) {
        HorizontalDivider(thickness = 0.8.dp, color = OkanBorder.copy(alpha = 0.6f))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomBarItem(
                label = "ホーム",
                icon = Icons.Default.Home,
                isSelected = currentTab == TabItem.HOME,
                onClick = { onTabSelected(TabItem.HOME) }
            )
            BottomBarItem(
                label = "マイ設定",
                icon = Icons.Default.Menu,
                isSelected = currentTab == TabItem.MY_PRESETS,
                onClick = { onTabSelected(TabItem.MY_PRESETS) }
            )
            BottomBarItem(
                label = "設定",
                icon = Icons.Default.Settings,
                isSelected = currentTab == TabItem.SETTINGS,
                onClick = { onTabSelected(TabItem.SETTINGS) }
            )
        }
    }
}

@Composable
private fun BottomBarItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(width = 54.dp, height = 30.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(if (isSelected) OkanPrimaryContainer else Color.Transparent)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) OkanTextPrimary else OkanTabInactive,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) OkanTextPrimary else OkanTabInactive
        )
    }
}
