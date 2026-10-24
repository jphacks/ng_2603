package com.example.myapplication.ui.components

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
import com.example.myapplication.ui.theme.OkanCardGray
import com.example.myapplication.ui.theme.OkanPrimary
import com.example.myapplication.ui.theme.OkanPrimaryContainer
import com.example.myapplication.ui.theme.OkanTextPrimary
import com.example.myapplication.ui.theme.OkanTextSecondary
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

/**
 * 制限時間セット用のドラムロール・ダイヤルピッカー
 * - 「分」が00のときはそれ以上上にスクロール・「^」を押しても反応しない（最小値0で停止）
 * - 急激な増減や誤動作のない安定したドラムロールスクロール
 * - クイック選択チップとの正確な連動
 */
@Composable
fun DialTimePicker(
    selectedHour: Int,
    selectedMinute: Int,
    onTimeChanged: (hour: Int, minute: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val hours = remember { (0..23).toList() }
    val minutes = remember { (0..55 step 5).toList() } // 5分刻み (00, 05, 10 ... 55)

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
                selectedValue = selectedHour,
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

            // 分ダイヤル (00〜55分、5分刻み)
            WheelDialColumn(
                label = "分",
                values = minutes,
                selectedValue = (selectedMinute / 5 * 5).coerceIn(0, 55),
                onValueSelected = { newMin ->
                    onTimeChanged(selectedHour, newMin)
                },
                canStepUp = selectedMinute > 0, // 00のときはそれ以上上に行かない
                canStepDown = selectedMinute < 55,
                onStepUp = {
                    // 00 のときはそれ以上上に行かない（反応しない）
                    if (selectedMinute > 0) {
                        val newMin = (selectedMinute - 5).coerceAtLeast(0)
                        onTimeChanged(selectedHour, newMin)
                    }
                },
                onStepDown = {
                    if (selectedMinute < 55) {
                        val newMin = (selectedMinute + 5).coerceAtMost(55)
                        onTimeChanged(selectedHour, newMin)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // クイック選択チップ
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
                    shape = RoundedCornerShape(14.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
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
    val visibleItemsCount = 3
    val totalHeight = itemHeight * visibleItemsCount

    val targetIndex = values.indexOf(selectedValue).coerceIn(0, values.size - 1)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = targetIndex)
    val snapFlingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val coroutineScope = rememberCoroutineScope()

    // 外部（クイックチップやステップボタン）から selectedValue が変更されたときの同期
    LaunchedEffect(selectedValue) {
        val idx = values.indexOf(selectedValue)
        if (idx in values.indices && !listState.isScrollInProgress) {
            if (listState.firstVisibleItemIndex != idx) {
                listState.animateScrollToItem(idx)
            }
        }
    }

    // スクロール停止時のみ中央（firstVisibleItemIndex）の値を確定（フィードバックループ防止）
    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }
            .filter { !it } // スクロールが停止した瞬間
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
        // 上ボタン（▲ / ^）: canStepUp が false のときは無効化（00のときは反応しない）
        IconButton(
            onClick = onStepUp,
            enabled = canStepUp,
            modifier = Modifier.size(30.dp)
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = "$label を減らす",
                tint = if (canStepUp) OkanTextPrimary else OkanTextSecondary.copy(alpha = 0.25f),
                modifier = Modifier.size(22.dp)
            )
        }

        // ホイール表示領域
        Box(
            modifier = Modifier
                .width(76.dp)
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(totalHeight),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 上部ダミー余白
                item {
                    Box(modifier = Modifier.height(itemHeight))
                }

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

                // 下部ダミー余白
                item {
                    Box(modifier = Modifier.height(itemHeight))
                }
            }
        }

        // 下ボタン（▼）
        IconButton(
            onClick = onStepDown,
            enabled = canStepDown,
            modifier = Modifier.size(30.dp)
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "$label を増やす",
                tint = if (canStepDown) OkanTextPrimary else OkanTextSecondary.copy(alpha = 0.25f),
                modifier = Modifier.size(22.dp)
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
