package com.swyp.mangro.feature.consumer.hold.history

import com.swyp.mangro.core.designsystem.component.card.wishlist.WishHistoryItem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class HoldHistoryUiState(
    val inProgressItems: ImmutableList<WishHistoryItem> = persistentListOf(),
    val pastItems: ImmutableList<WishHistoryItem> = persistentListOf(),
    val isLoading: Boolean = false,
)
