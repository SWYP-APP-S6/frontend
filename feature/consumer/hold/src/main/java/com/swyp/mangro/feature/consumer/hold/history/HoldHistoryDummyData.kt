package com.swyp.mangro.feature.consumer.hold.history

import com.swyp.mangro.core.designsystem.component.card.wishlist.WishHistoryItem
import com.swyp.mangro.core.designsystem.component.card.wishlist.WishStatus
import kotlinx.collections.immutable.persistentListOf

private val now = System.currentTimeMillis()

val dummyInProgressItems = persistentListOf(
    WishHistoryItem(
        id = "1",
        imageUrl = "",
        discountRate = 60,
        name = "복숭아 4입",
        quantity = 1,
        storeName = "청과마을",
        price = 4_000,
        status = WishStatus.IN_PROGRESS,
        requestTimeMillis = now - 5_000,
        endTimeMillis = now + 9 * 60_000 + 24_000,
    ),
)

val dummyCheckingStockItem = WishHistoryItem(
    id = "4",
    imageUrl = "",
    discountRate = 40,
    name = "양파 3입",
    quantity = 1,
    storeName = "청과마을",
    price = 2_500,
    status = WishStatus.CHECKING_STOCK,
)

val dummyPastItems = persistentListOf(
    WishHistoryItem(
        id = "2",
        imageUrl = "",
        discountRate = 60,
        name = "대파 1단",
        quantity = 2,
        storeName = "청과마을",
        price = 7_000,
        status = WishStatus.PICKED_UP,
        dateLabel = "어제",
    ),
    WishHistoryItem(
        id = "3",
        imageUrl = "",
        discountRate = 50,
        name = "알배추",
        quantity = 1,
        storeName = "청과마을",
        price = 3_500,
        status = WishStatus.EXPIRED,
        dateLabel = "3일 전",
    ),
)

val dummyHoldHistoryUiState = HoldHistoryUiState(
    inProgressItems = dummyInProgressItems,
    pastItems = dummyPastItems,
)
