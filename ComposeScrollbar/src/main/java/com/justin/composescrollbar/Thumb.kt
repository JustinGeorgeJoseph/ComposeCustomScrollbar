package com.justin.composescrollbar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun Thumb(
    scope: BoxWithConstraintsScope,
    isSelected: Boolean,
    normalizedThumbSize: Float,
    normalizedOffsetPosition: Float,
    thumbProperties: ThumbProperties,
) {
    with(scope) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .graphicsLayer(translationY = constraints.maxHeight.toFloat() * normalizedOffsetPosition)
                .padding(horizontal = thumbProperties.padding)
                .width(thumbProperties.thickness)
                .clip(thumbProperties.thumbShape)
                .background(
                    if (isSelected) {
                        thumbProperties.thumbSelectedColor
                    } else {
                        thumbProperties.thumbColor
                    }
                )
                .fillMaxHeight(normalizedThumbSize)
        )
    }
}

data class ThumbProperties(
    val thumbColor: Color,
    val thumbSelectedColor: Color,
    val thickness: Dp = 6.dp,
    val padding: Dp = 2.dp,
    val thumbMinHeight: Float = 0.1f,
    val thumbShape: Shape = CircleShape,
    val selectionMode: ScrollbarSelectionMode = ScrollbarSelectionMode.Full,
)
