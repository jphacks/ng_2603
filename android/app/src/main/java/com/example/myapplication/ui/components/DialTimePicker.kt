package com.example.myapplication.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
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
import com.example.myapplication.ui.theme.OkanTextPrimary
import com.example.myapplication.ui.theme.OkanTextSecondary
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

/**
 * 制限時間セット用のドラムロール・ダイヤルピッカー
 * 時間（0〜23）と分（0〜59）をホイールスクロールまたは上下ボタンでセット可能。
 */
@Composable
fun DialTimePicker(
    selectedHour: Int,
    selectedMinute: Int,
    onTimeChanged: (hour: Int, minute: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val hours = remember { (0..23).toList() }
    val minutes = remember { (0..59).toList() }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        // メインのダイヤル表示部分
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 時間ダイヤル
            DialColumnWithControls(
                label = "時間",
                values = hours,
                selectedValue = selectedHour,
                onValueSelected = { newHour ->
                    onTimeChanged(newHour, selectedMinute)
                },
                onStepUp = {
                    val next = (selectedHour - 1 + 24) % 24
                    onTimeChanged(next, selectedMinute)
                },
                onStepDown = {
                    val next = (selectedHour + 1) % 24
                    onTimeChanged(next, selectedMinute)
                }
            )

            // 区切りコロン
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    text = ":",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = OkanTextPrimary
                )
            }

            // 分ダイヤル
            DialColumnWithControls(
                label = "分",
                values = minutes,
                selectedValue = selectedMinute,
                onValueSelected = { newMin ->
                    onTimeChanged(selectedHour, newMin)
                },
                onStepUp = {
                    val next = (selectedMinute - 5 + 60) % 60
                    onTimeChanged(selectedHour, next)
                },
                onStepDown = {
                    val next = (selectedMinute + 5) % 60
                    onTimeChanged(selectedHour, next)
                }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // クイックプリセットチップ
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            listOf(
                "15分" to (0 to 15),
                "30分" to (0 to 30),
                "45分" to (0 to 45),
                "1時間" to (1 to 0),
                "2時間" to (2 to 0)
            ).forEach { (label, time) ->
                val isSelected = selectedHour == time.first && selectedMinute == time.second
                SuggestionChip(
                    onClick = {
                        onTimeChanged(time.first, time.second)
                    },
                    label = {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) OkanPrimary else OkanTextSecondary
                        )
                    },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = if (isSelected) OkanPrimaryContainer else OkanCardGray
                    ),
                    border = SuggestionChipDefaults.suggestionChipBorder(
                        enabled = true,
                        borderColor = if (isSelected) OkanPrimary else Color.Transparent
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DialColumnWithControls(
    label: String,
    values: List<Int>,
    selectedValue: Int,
    onValueSelected: (Int) -> Unit,
    onStepUp: () -> Unit,
    onStepDown: () -> Unit,
    modifier: Modifier = Modifier
) {
    val itemHeight: Dp = 44.dp
    val visibleItemsCount = 3
    val totalHeight = itemHeight * visibleItemsCount

    val selectedIndex = values.indexOf(selectedValue).coerceAtLeast(0)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = selectedIndex)
    val snapFlingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val coroutineScope = rememberCoroutineScope()

    // リストがスクロールされたときに中央のインデックスを監視
    val centerIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo
            if (visibleItems.isEmpty()) {
                0
            } else {
                val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
                val closest = visibleItems.minByOrNull { item ->
                    val itemCenter = item.offset + item.size / 2
                    kotlin.math.abs(itemCenter - viewportCenter)
                }
                closest?.index ?: 0
            }
        }
    }

    // スクロール停止時に値を更新
    LaunchedEffect(centerIndex) {
        snapshotFlow { centerIndex }
            .distinctUntilChanged()
            .collect { idx ->
                val adjustedIdx = (idx - 1).coerceIn(0, values.size - 1)
                if (values[adjustedIdx] != selectedValue) {
                    onValueSelected(values[adjustedIdx])
                }
            }
    }

    // 外部から値が変わったときにスクロール位置を同期
    LaunchedEffect(selectedValue) {
        val targetIdx = values.indexOf(selectedValue)
        if (targetIdx >= 0 && !listState.isScrollInProgress) {
            // targetIdx + 1 が中央になるように targetIdx にスクロール
            listState.animateScrollToItem(targetIdx)
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        // 上ボタン（▲）
        IconButton(
            onClick = {
                onStepUp()
            },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = "$label を増やす",
                tint = OkanTextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }

        // ホイール表示領域
        Box(
            modifier = Modifier
                .width(80.dp)
                .height(totalHeight),
            contentAlignment = Alignment.Center
        ) {
            // 中央選択中のハイライト帯
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(totalHeight),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 上部余白アイテム
                item {
                    Box(modifier = Modifier.height(itemHeight))
                }

                items(values.size) { index ->
                    val value = values[index]
                    val isCurrentCenter = (index == centerIndex - 1)

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
                            fontSize = if (isCurrentCenter) 26.sp else 16.sp,
                            fontWeight = if (isCurrentCenter) FontWeight.Bold else FontWeight.Normal,
                            color = if (isCurrentCenter) OkanTextPrimary else OkanTextSecondary.copy(alpha = 0.4f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // 下部余白アイテム
                item {
                    Box(modifier = Modifier.height(itemHeight))
                }
            }
        }

        // 下ボタン（▼）
        IconButton(
            onClick = {
                onStepDown()
            },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "$label を減らす",
                tint = OkanTextSecondary,
                modifier = Modifier.size(20.dp)
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
