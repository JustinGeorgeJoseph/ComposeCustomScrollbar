package com.justin.composescrollbar

import androidx.compose.foundation.lazy.LazyListState

fun LazyListState.getFirstVisibleItemIndex() =
    layoutInfo.visibleItemsInfo.firstOrNull()?.index ?: firstVisibleItemIndex

fun LazyListState.getRealFirstVisibleItem() =
    layoutInfo.visibleItemsInfo.firstOrNull { it.index == firstVisibleItemIndex }

fun LazyListState.isStickyHeaderInAction() =
    this.getRealFirstVisibleItem()?.index != this.getFirstVisibleItemIndex()

