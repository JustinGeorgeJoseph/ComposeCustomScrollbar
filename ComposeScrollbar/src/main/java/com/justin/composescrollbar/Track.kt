package com.justin.composescrollbar

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.colorResource


@Composable
fun Track(
    boxWithConstraintsScope: BoxWithConstraintsScope,
    scrollBarProperties: ScrollBarProperties,
    setSelected: (Boolean) -> Unit,
    onDragStarted: (Offset) -> Unit,
    setScrollOffset: (Float, Float) -> Unit
) {
    val trackProperties = scrollBarProperties.trackProperties
    val thumbProperties = scrollBarProperties.thumbProperties
    with(boxWithConstraintsScope) {
        BoxWithConstraints(
            Modifier
                .align(if (trackProperties.rightSide) Alignment.TopEnd else Alignment.TopStart)
                .fillMaxHeight()
                .background(colorResource(id = R.color.scrollbar_track_bg))
                .draggable(
                    state = rememberDraggableState { delta ->
                        val displace = if (trackProperties.reverseLayout) {
                            -delta
                        } else {
                            delta
                        }
                        if (trackProperties.isSelected) {
                            setScrollOffset(displace, constraints.maxHeight.toFloat())
                        }
                    },
                    orientation = Orientation.Vertical,
                    enabled = thumbProperties.selectionMode != ScrollbarSelectionMode.Disabled,
                    startDragImmediately = true,
                    onDragStarted = onDragStarted@{ offset -> onDragStarted(offset) },
                    onDragStopped = { setSelected(false) }
                )
        ) {
            Thumb(
                scope = this,
                normalizedOffsetPosition = trackProperties.normalizedOffsetPosition,
                isSelected = trackProperties.isSelected,
                thumbProperties = thumbProperties,
                normalizedThumbSize = trackProperties.normalizedThumbSize
            )
        }
    }
}

data class TrackProperties(
    val reverseLayout: Boolean,
    val rightSide: Boolean,
    val normalizedThumbSize: Float,
    val normalizedOffsetPosition: Float,
    val isSelected: Boolean,
)