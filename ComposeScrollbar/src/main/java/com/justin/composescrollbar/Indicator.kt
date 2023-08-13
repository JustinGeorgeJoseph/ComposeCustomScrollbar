package com.justin.composescrollbar

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

private object AlphaAnimationSpecs {
    const val START_ALPHA = 1F
    const val END_ALPHA = 0F
    const val START_DURATION_IN_MILLS = 75
    const val END_DURATION_IN_MILLS = 500
    const val START_DELAY_IN_MILLS = 75
    const val END_DELAY_IN_MILLS = 500
}

private object DisplacementAnimationSpecs {
    const val START_DISPLACEMENT = 0F
    const val END_DISPLACEMENT = 100F
    const val START_DURATION_IN_MILLS = 250
    const val END_DURATION_IN_MILLS = 500
    const val START_DELAY_IN_MILLS = 0
    const val END_DELAY_IN_MILLS = 500
}

private const val INDICATOR_PADDING_DELTA = 4

@Composable
fun Indicator(
    boxWithConstraintsScope: BoxWithConstraintsScope,
    isInAction: Boolean,
    scrollBarProperties: ScrollBarProperties,
    showContent: (@Composable () -> Unit)
) {

    var isThumbInAction by remember { mutableStateOf(false) }
    SideEffect { isThumbInAction = isInAction }

    val alpha by animateFloatAsState(
        targetValue = if (isThumbInAction) {
            AlphaAnimationSpecs.START_ALPHA
        } else {
            AlphaAnimationSpecs.END_ALPHA
        },
        animationSpec = tween(
            durationMillis = if (isThumbInAction) {
                AlphaAnimationSpecs.START_DURATION_IN_MILLS
            } else {
                AlphaAnimationSpecs.END_DURATION_IN_MILLS
            },
            delayMillis = if (isThumbInAction) {
                AlphaAnimationSpecs.START_DELAY_IN_MILLS
            } else {
                AlphaAnimationSpecs.END_DELAY_IN_MILLS
            }
        )
    )

    val displacement by animateFloatAsState(
        targetValue = if (isThumbInAction) {
            DisplacementAnimationSpecs.START_DISPLACEMENT
        } else {
            DisplacementAnimationSpecs.END_DISPLACEMENT
        },
        animationSpec = tween(
            durationMillis = if (isThumbInAction) {
                DisplacementAnimationSpecs.START_DURATION_IN_MILLS
            } else {
                DisplacementAnimationSpecs.END_DURATION_IN_MILLS
            },
            delayMillis = if (isThumbInAction) {
                DisplacementAnimationSpecs.START_DELAY_IN_MILLS
            } else {
                DisplacementAnimationSpecs.END_DELAY_IN_MILLS
            }
        )
    )

    ShowIndicator(
        boxWithConstraintsScope = boxWithConstraintsScope,
        scrollBarProperties = scrollBarProperties,
        alpha = alpha,
        displacement = displacement,
        showContent = showContent
    )
}

@Composable
private fun ShowIndicator(
    boxWithConstraintsScope: BoxWithConstraintsScope,
    scrollBarProperties: ScrollBarProperties,
    alpha: Float,
    displacement: Float,
    showContent: @Composable () -> Unit
) {
    val trackProperties = scrollBarProperties.trackProperties
    val rightSide = scrollBarProperties.trackProperties.rightSide
    val thumbProperties = scrollBarProperties.thumbProperties
    val indicatorPaddingStart = if (rightSide) {
        0.dp
    } else {
        thumbProperties.padding
    }
    val indicatorPaddingEnd = if (!rightSide) {
        0.dp
    } else {
        (thumbProperties.padding * INDICATOR_PADDING_DELTA) + thumbProperties.thickness
    }
    with(boxWithConstraintsScope) {
        Box(
            Modifier
                .align(if (rightSide) Alignment.TopEnd else Alignment.TopStart)
                .fillMaxHeight()
                .alpha(alpha)
                .graphicsLayer(
                    translationX = with(LocalDensity.current) { (if (rightSide) displacement.dp else -displacement.dp).toPx() },
                    translationY = constraints.maxHeight.toFloat() * trackProperties.normalizedOffsetPosition
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight(trackProperties.normalizedThumbSize)
                    .padding(start = indicatorPaddingStart, end = indicatorPaddingEnd),
                contentAlignment = Alignment.Center
            ) {
                showContent()
            }
        }
    }
}

