package com.swyp.mangro.feature.consumer.hold.detail

import com.swyp.mangro.core.designsystem.component.card.wishlist.WishDetailItem
import com.swyp.mangro.core.designsystem.component.card.wishlist.WishStatus
import kotlinx.collections.immutable.persistentListOf

val dummyHoldDetailInfo = HoldDetailInfo(
    item = WishDetailItem(
        id = "1",
        name = "복숭아 4입",
        quantity = 1,
        price = 4_000,
        originalPrice = 10_000,
        discountRate = 50,
        status = WishStatus.IN_PROGRESS,
    ),
    images = persistentListOf("", "", "", "", ""),
    storeName = "청과마을",
    requestedDateText = "08/24",
    requestedTimeText = "18:02",
    expiredDateText = "08/24",
    expiredTimeText = "18:17",
)

val dummyHoldDetailUiState = HoldDetailUiState(
    detail = dummyHoldDetailInfo,
)

val dummyHoldDetailUiStatePast = dummyHoldDetailUiState.copy(
    detail = dummyHoldDetailInfo.copy(
        item = dummyHoldDetailInfo.item.copy(status = WishStatus.PICKED_UP),
    ),
)
