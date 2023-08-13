package com.justin.composescrollbar

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListLayoutInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.launch
import kotlin.math.floor

@Composable
fun CustomScrollBar(
    listState: LazyListState,
    rightSide: Boolean = true,
    thumbProperties: ThumbProperties,
    indicatorContent: (@Composable (index: Int) -> Unit)? = null,
) {
    val coroutineScope = rememberCoroutineScope()
    var isSelected by remember { mutableStateOf(false) }
    var dragOffset by remember { mutableStateOf(0f) }
    val reverseLayout by remember { derivedStateOf { listState.layoutInfo.reverseLayout } }
    val firstVisibleVisibleItemIndex by remember { derivedStateOf { listState.getFirstVisibleItemIndex() } }
    val realFirstVisibleItem by remember { derivedStateOf { listState.getRealFirstVisibleItem() } }
    val isStickyHeaderInAction by remember { derivedStateOf { listState.isStickyHeaderInAction() } }
    val normalizedThumbSizeReal by remember {
        derivedStateOf {
            getNormalizedThumbSizeReal(
                layoutInfo = listState.layoutInfo,
                realFirstVisibleItem = realFirstVisibleItem,
                isStickyHeaderInAction = isStickyHeaderInAction
            )
        }
    }
    val normalizedThumbSize by remember {
        derivedStateOf {
            normalizedThumbSizeReal.coerceAtLeast(
                thumbProperties.thumbMinHeight
            )
        }
    }
    val normalizedOffsetPosition by remember {
        derivedStateOf {
            getNormalizedOffsetPosition(
                layoutInfo = listState.layoutInfo,
                realFirstVisibleItem = realFirstVisibleItem,
                reverseLayout = reverseLayout,
                normalizedThumbSizeReal = normalizedThumbSizeReal,
                thumbMinHeight = thumbProperties.thumbMinHeight
            )
        }
    }
    val isInAction = listState.isScrollInProgress || isSelected

    fun setScrollOffSetTemp(newOffset: Float) = setScrollOffset(
        newOffset = newOffset,
        listState = listState,
        normalizedThumbSize = normalizedThumbSize,
        setDragOffset = { dragOffset = it },
        normalizedThumbSizeReal = normalizedThumbSizeReal,
        thumbMinHeight = thumbProperties.thumbMinHeight,
        scrollToItem = { index, reminder ->
            coroutineScope.launch {
                listState.scrollToItem(index = index, scrollOffset = 0)
                val offset = realFirstVisibleItem
                    ?.size
                    ?.let { it.toFloat() * reminder }
                    ?.toInt() ?: 0
                listState.scrollToItem(index = index, scrollOffset = offset)
            }
        },
    )

    val trackProperties = TrackProperties(
        reverseLayout = reverseLayout,
        rightSide = rightSide,
        normalizedThumbSize = normalizedThumbSize,
        normalizedOffsetPosition = normalizedOffsetPosition,
        isSelected = isSelected
    )
    val scrollBarProperties = ScrollBarProperties(
        trackProperties = trackProperties,
        thumbProperties = thumbProperties
    )
    ShowContent(
        scrollBarProperties = scrollBarProperties,
        indicatorContent = indicatorContent,
        isInAction = isInAction,
        firstVisibleVisibleItemIndex = firstVisibleVisibleItemIndex,
        setSelected = { isSelected = it },
        onDragStarted = { offset, maxHeight ->
            onDragStarted(offset = offset,
                maxHeight = maxHeight,
                reverseLayout = reverseLayout,
                normalizedOffsetPosition = normalizedOffsetPosition,
                normalizedThumbSize = normalizedThumbSize,
                thumbProperties = thumbProperties,
                setDragOffset = { dragOffset = it },
                setScrollOffset = { setScrollOffSetTemp(it) },
                setSelected = { isSelected = it })
        },
        setScrollOffset = { displace, maxHeight ->
            setScrollOffSetTemp((dragOffset + displace / maxHeight))
        },
    )
}

@Composable
fun ShowContent(
    scrollBarProperties: ScrollBarProperties,
    isInAction: Boolean,
    firstVisibleVisibleItemIndex: Int,
    setSelected: (Boolean) -> Unit,
    onDragStarted: (offset: Offset, maxHeight: Float) -> Unit,
    setScrollOffset: (Float, Float) -> Unit,
    indicatorContent: (@Composable (index: Int) -> Unit)? = null,
) {
    val trackProperties = scrollBarProperties.trackProperties

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        if (indicatorContent != null && trackProperties.isSelected) {
            Indicator(
                boxWithConstraintsScope = this,
                isInAction = isInAction,
                scrollBarProperties = scrollBarProperties,
                showContent = { indicatorContent(index = firstVisibleVisibleItemIndex) })
        }
        Track(
            boxWithConstraintsScope = this,
            scrollBarProperties = scrollBarProperties,
            setSelected = {
                setSelected.invoke(it)
            },
            onDragStarted = { offset ->
                onDragStarted(offset, constraints.maxHeight.toFloat())
            },
            setScrollOffset = { displace, maxHeight ->
                setScrollOffset(displace, maxHeight)
            }
        )
    }
}

fun setScrollOffset(
    newOffset: Float,
    listState: LazyListState,
    normalizedThumbSize: Float,
    normalizedThumbSizeReal: Float,
    thumbMinHeight: Float,
    setDragOffset: (Float) -> Unit,
    scrollToItem: (Int, Float) -> Unit
) {
    val dragOffset =
        getDragOffset(offsetValue = newOffset, normalizedThumbSize = normalizedThumbSize)
    setDragOffset.invoke(dragOffset)
    val totalItemsCount = listState.layoutInfo.totalItemsCount.toFloat()
    val exactIndex = offsetCorrectionInverse(
        top = totalItemsCount * dragOffset,
        normalizedThumbSizeReal = normalizedThumbSizeReal,
        thumbMinHeight = thumbMinHeight
    )
    val index: Int = floor(exactIndex).toInt()
    val remainder: Float = exactIndex - floor(exactIndex)
    scrollToItem.invoke(index, remainder)
}

fun getNormalizedOffsetPosition(
    layoutInfo: LazyListLayoutInfo,
    realFirstVisibleItem: LazyListItemInfo?,
    reverseLayout: Boolean,
    normalizedThumbSizeReal: Float,
    thumbMinHeight: Float
): Float {
    if (layoutInfo.totalItemsCount == 0 || layoutInfo.visibleItemsInfo.isEmpty() || realFirstVisibleItem == null) {
        return 0f
    }
    val top =
        realFirstVisibleItem.run { index.toFloat() + fractionHiddenTop() } / layoutInfo.totalItemsCount.toFloat()
    return offsetCorrection(
        top = top,
        reverseLayout = reverseLayout,
        normalizedThumbSizeReal = normalizedThumbSizeReal,
        thumbMinHeight = thumbMinHeight
    )
}

fun getNormalizedThumbSizeReal(
    layoutInfo: LazyListLayoutInfo,
    realFirstVisibleItem: LazyListItemInfo?,
    isStickyHeaderInAction: Boolean
): Float {
    if (layoutInfo.totalItemsCount == 0 || realFirstVisibleItem == null) {
        return 0f
    }
    val firstPartial = realFirstVisibleItem.fractionHiddenTop()
    val lastPartial =
        1f - layoutInfo.visibleItemsInfo.last().fractionVisibleBottom(layoutInfo.viewportEndOffset)
    val realSize = layoutInfo.visibleItemsInfo.size - if (isStickyHeaderInAction) 1 else 0
    val realVisibleSize = realSize.toFloat() - firstPartial - lastPartial
    return realVisibleSize / layoutInfo.totalItemsCount.toFloat()
}

private fun onDragStarted(
    offset: Offset,
    maxHeight: Float,
    reverseLayout: Boolean,
    normalizedOffsetPosition: Float,
    normalizedThumbSize: Float,
    thumbProperties: ThumbProperties,
    setDragOffset: (Float) -> Unit,
    setScrollOffset: (Float) -> Unit,
    setSelected: (Boolean) -> Unit
) {
    if (maxHeight <= 0f) return
    val newOffset = when {
        reverseLayout -> (maxHeight - offset.y) / maxHeight
        else -> offset.y / maxHeight
    }
    val currentOffset = when {
        reverseLayout -> 1f - normalizedOffsetPosition - normalizedThumbSize
        else -> normalizedOffsetPosition
    }
    when (thumbProperties.selectionMode) {
        ScrollbarSelectionMode.Full -> {
            if (newOffset in currentOffset..(currentOffset + normalizedThumbSize)) {
                val dragOffset = getDragOffset(
                    offsetValue = currentOffset,
                    normalizedThumbSize = normalizedThumbSize
                )
                setDragOffset(dragOffset)
            } else {
                setScrollOffset(newOffset)
            }
            setSelected(true)
        }
        ScrollbarSelectionMode.Thumb -> {
            if (newOffset in currentOffset..(currentOffset + normalizedThumbSize)) {
                val dragOffset = getDragOffset(
                    offsetValue = currentOffset,
                    normalizedThumbSize = normalizedThumbSize
                )
                setDragOffset(dragOffset)
                setSelected(true)
            }
        }
        ScrollbarSelectionMode.Disabled -> Unit
    }
}

private fun offsetCorrectionInverse(
    top: Float,
    normalizedThumbSizeReal: Float,
    thumbMinHeight: Float
): Float {
    if (normalizedThumbSizeReal >= thumbMinHeight)
        return top
    val topRealMax = 1f - normalizedThumbSizeReal
    val topMax = 1f - thumbMinHeight
    return top * topRealMax / topMax
}

private fun offsetCorrection(
    top: Float,
    reverseLayout: Boolean,
    normalizedThumbSizeReal: Float,
    thumbMinHeight: Float
): Float {
    val topRealMax = (1f - normalizedThumbSizeReal).coerceIn(0f, 1f)
    if (normalizedThumbSizeReal >= thumbMinHeight) {
        return when {
            reverseLayout -> topRealMax - top
            else -> top
        }
    }

    val topMax = 1f - thumbMinHeight
    return when {
        reverseLayout -> (topRealMax - top) * topMax / topRealMax
        else -> top * topMax / topRealMax
    }
}

private fun getDragOffset(offsetValue: Float, normalizedThumbSize: Float): Float {
    val maxValue = (1f - normalizedThumbSize).coerceAtLeast(0f)
    return offsetValue.coerceIn(0f, maxValue)
}

data class ScrollBarProperties(
    val trackProperties: TrackProperties,
    val thumbProperties: ThumbProperties
)