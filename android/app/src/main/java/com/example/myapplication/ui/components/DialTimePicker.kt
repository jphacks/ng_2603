package com.example.myapplication.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.OkanBorder
import com.example.myapplication.ui.theme.OkanCardGray
import com.example.myapplication.ui.theme.OkanPrimary
import com.example.myapplication.ui.theme.OkanPrimaryContainer
import com.example.myapplication.ui.theme.OkanSurface
import com.example.myapplication.ui.theme.OkanTextPrimary
import com.example.myapplication.ui.theme.OkanTextSecondary
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

/**
 * 制限時間セット用のダイヤルピッカー
 * - 上ボタン（^）: 数字を減らす（0で下限停止）
 * - 下ボタン（v）: 数字を増やす
 * - 分は1分単位（0〜59分）で設定可能
 * - 4つのクイックボタン（15分, 30分, 45分, 1時間）は等幅横並びで縦に伸びない
 */
@Composable
fun DialTimePicker(
    selectedHour: Int,
    selectedMinute: Int,
    onTimeChanged: (hour: Int, minute: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val hours = remember { (0..23).toList() }
    val minutes = remember { (0..59).toList() } // ★1分刻み (0, 1, 2 ... 59)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        // メインのダイヤル表示部分
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 時間ダイヤル (0〜23時間)
            WheelDialColumn(
                label = "時間",
                values = hours,
                selectedValue = selectedHour.coerceIn(0, 23),
                onValueSelected = { newHour ->
                    onTimeChanged(newHour, selectedMinute)
                },
                canStepUp = selectedHour > 0,
                canStepDown = selectedHour < 23,
                onStepUp = {
                    if (selectedHour > 0) {
                        onTimeChanged(selectedHour - 1, selectedMinute)
                    }
                },
                onStepDown = {
                    if (selectedHour < 23) {
                        onTimeChanged(selectedHour + 1, selectedMinute)
                    }
                }
            )

            // 区切りコロン
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 14.dp)
            ) {
                Text(
                    text = ":",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = OkanTextPrimary
                )
            }

            // 分ダイヤル (00〜59分、1分刻み)
            WheelDialColumn(
                label = "分",
                values = minutes,
                selectedValue = selectedMinute.coerceIn(0, 59),
                onValueSelected = { newMin ->
                    onTimeChanged(selectedHour, newMin)
                },
                canStepUp = selectedMinute > 0,
                canStepDown = selectedMinute < 59,
                onStepUp = {
                    if (selectedMinute > 0) {
                        val newMin = (selectedMinute - 1).coerceAtLeast(0)
                        onTimeChanged(selectedHour, newMin)
                    }
                },
                onStepDown = {
                    if (selectedMinute < 59) {
                        val newMin = (selectedMinute + 1).coerceAtMost(59)
                        onTimeChanged(selectedHour, newMin)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // クイック選択チップ（4つ等幅で横並び、縦に伸びない）
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            listOf(
                "15分" to (0 to 15),
                "30分" to (0 to 30),
                "45分" to (0 to 45),
                "1時間" to (1 to 0)
            ).forEach { (label, time) ->
                val isSelected = selectedHour == time.first && selectedMinute == time.second
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) OkanPrimaryContainer else OkanCardGray)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) OkanPrimary else Color.Transparent,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable {
                            onTimeChanged(time.first, time.second)
                        }
                ) {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) OkanPrimary else OkanTextSecondary,
                        maxLines = 1,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun WheelDialColumn(
    label: String,
    values: List<Int>,
    selectedValue: Int,
    onValueSelected: (Int) -> Unit,
    canStepUp: Boolean,
    canStepDown: Boolean,
    onStepUp: () -> Unit,
    onStepDown: () -> Unit,
    modifier: Modifier = Modifier
) {
    val itemHeight: Dp = 40.dp
    val totalHeight: Dp = itemHeight * 3

    val targetIndex = values.indexOf(selectedValue).coerceIn(0, values.size - 1)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = targetIndex)
    val snapFlingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(selectedValue) {
        val idx = values.indexOf(selectedValue)
        if (idx in values.indices && !listState.isScrollInProgress) {
            if (listState.firstVisibleItemIndex != idx) {
                listState.animateScrollToItem(idx)
            }
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }
            .filter { !it }
            .collect {
                val centerIdx = listState.firstVisibleItemIndex.coerceIn(0, values.size - 1)
                val value = values[centerIdx]
                if (value != selectedValue) {
                    onValueSelected(value)
                }
            }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        // 上ボタン（▲ / ^）: 数値を減らす（0で停止）
        IconButton(
            onClick = onStepUp,
            enabled = canStepUp,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = "$label を減らす",
                tint = if (canStepUp) OkanTextPrimary else OkanTextSecondary.copy(alpha = 0.25f),
                modifier = Modifier.size(24.dp)
            )
        }

        // ホイール表示領域
        Box(
            modifier = Modifier
                .width(80.dp)
                .height(totalHeight),
            contentAlignment = Alignment.Center
        ) {
            // 中央選択中の水色ハイライト帯
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(itemHeight)
                    .clip(RoundedCornerShape(12.dp))
                    .background(OkanPrimaryContainer)
            )

            LazyColumn(
                state = listState,
                flingBehavior = snapFlingBehavior,
                contentPadding = PaddingValues(vertical = itemHeight),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(totalHeight),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                items(values.size) { index ->
                    val value = values[index]
                    val isSelected = (value == selectedValue)

                    Box(
                        modifier = Modifier
                            .height(itemHeight)
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                coroutineScope.launch {
                                    listState.animateScrollToItem(index)
                                    onValueSelected(value)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "%02d".format(value),
                            fontSize = if (isSelected) 24.sp else 16.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) OkanTextPrimary else OkanTextSecondary.copy(alpha = 0.45f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // 下ボタン（▼ / v）: 数値を増やす
        IconButton(
            onClick = onStepDown,
            enabled = canStepDown,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "$label を増やす",
                tint = if (canStepDown) OkanTextPrimary else OkanTextSecondary.copy(alpha = 0.25f),
                modifier = Modifier.size(24.dp)
            )
        }

        // 単位ラベル（「時間」または「分」）
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = OkanTextSecondary
        )
    }
}
