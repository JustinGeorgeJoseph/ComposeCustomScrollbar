package com.justin.composescrollbar

import androidx.compose.foundation.lazy.LazyListItemInfo

fun LazyListItemInfo.fractionHiddenTop() = if (size == 0) 0f else -offset.toFloat() / size.toFloat()

fun LazyListItemInfo.fractionVisibleBottom(viewportEndOffset: Int) =
    if (size == 0) 0f else (viewportEndOffset - offset).toFloat() / size.toFloat()
