package com.example.myapplication.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.OkanPrimaryContainer
import com.example.myapplication.ui.theme.OkanTextPrimary
import com.example.myapplication.ui.theme.OkanTextSecondary
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DialTimePicker(
    selectedHour: Int,
    selectedMinute: Int,
    onTimeChanged: (hour: Int, minute: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val itemHeight: Dp = 38.dp
    val visibleItemsCount = 3
    val totalHeight = itemHeight * visibleItemsCount

    val hours = remember { (0..23).toList() }
    val minutes = remember { (0..59).toList() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(totalHeight),
        contentAlignment = Alignment.Center
    ) {
        // 中央のハイライトピル（Figmaの水色角丸）
        Box(
            modifier = Modifier
                .fillMaxWidth(0.72f)
                .height(itemHeight)
                .clip(RoundedCornerShape(12.dp))
                .background(OkanPrimaryContainer)
        )

        Row(
            modifier = Modifier.fillMaxWidth(0.72f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 時間ホイール
            NumberWheelColumn(
                values = hours,
                selectedValue = selectedHour,
                onValueSelected = { newHour ->
                    onTimeChanged(newHour, selectedMinute)
                },
                itemHeight = itemHeight,
                visibleItemsCount = visibleItemsCount,
                modifier = Modifier.width(64.dp)
            )

            // コロン区切り
            Text(
                text = ":",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = OkanTextPrimary,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            // 分ホイール
            NumberWheelColumn(
                values = minutes,
                selectedValue = selectedMinute,
                onValueSelected = { newMin ->
                    onTimeChanged(selectedHour, newMin)
                },
                itemHeight = itemHeight,
                visibleItemsCount = visibleItemsCount,
                modifier = Modifier.width(64.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NumberWheelColumn(
    values: List<Int>,
    selectedValue: Int,
    onValueSelected: (Int) -> Unit,
    itemHeight: Dp,
    visibleItemsCount: Int,
    modifier: Modifier = Modifier
) {
    // 無限スクロールっぽくするためにパディング用のダミー要素
    val initialIndex = values.indexOf(selectedValue).coerceAtLeast(0)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
    val snapFlingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val coroutineScope = rememberCoroutineScope()

    // 現在中央にあるインデックスを監視
    val centerIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo
            if (visibleItems.isEmpty()) {
                0
            } else {
                val viewportCenter = layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset / 2
                val closest = visibleItems.minByOrNull { item ->
                    val itemCenter = item.offset + item.size / 2
                    kotlin.math.abs(itemCenter - viewportCenter)
                }
                closest?.index ?: 0
            }
        }
    }

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

    // 初回・外部変更時のスクロール位置同期
    LaunchedEffect(selectedValue) {
        val targetIdx = values.indexOf(selectedValue)
        if (targetIdx >= 0 && targetIdx != centerIndex - 1 && !listState.isScrollInProgress) {
            listState.scrollToItem(targetIdx)
        }
    }

    LazyColumn(
        state = listState,
        flingBehavior = snapFlingBehavior,
        modifier = modifier.height(itemHeight * visibleItemsCount),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 上部スペーサー（中央に合わせるため）
        item {
            Box(modifier = Modifier.height(itemHeight))
        }

        items(values.size) { index ->
            val value = values[index]
            val isCurrentCenter = (index == centerIndex - 1)

            Box(
                modifier = Modifier
                    .height(itemHeight)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "%02d".format(value),
                    fontSize = if (isCurrentCenter) 21.sp else 13.sp,
                    fontWeight = if (isCurrentCenter) FontWeight.Bold else FontWeight.Normal,
                    color = if (isCurrentCenter) OkanTextPrimary else OkanTextSecondary.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center
                )
            }
        }

        // 下部スペーサー
        item {
            Box(modifier = Modifier.height(itemHeight))
        }
    }
}
