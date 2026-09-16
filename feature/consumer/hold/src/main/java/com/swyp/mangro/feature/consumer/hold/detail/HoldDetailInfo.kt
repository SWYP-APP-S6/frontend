package com.swyp.mangro.feature.consumer.hold.detail

import com.swyp.mangro.core.designsystem.component.card.wishlist.WishDetailItem
import kotlinx.collections.immutable.ImmutableList

data class HoldDetailInfo(
    val item: WishDetailItem,
    val images: ImmutableList<String>,
    val storeName: String,
    val requestedDateText: String,
    val requestedTimeText: String,
    val expiredDateText: String,
    val expiredTimeText: String,
)
