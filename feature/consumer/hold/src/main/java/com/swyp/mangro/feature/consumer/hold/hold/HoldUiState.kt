package com.swyp.mangro.feature.consumer.hold.hold

import com.swyp.mangro.core.designsystem.component.card.timer.TimerCardPhase
import com.swyp.mangro.core.designsystem.component.card.wishlist.WishedProduct
import com.swyp.mangro.core.model.store.StoreInfo
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class HoldUiState(
    val requestTimeMillis: Long = 0L,
    val endTimeMillis: Long = 0L,
    val timerPhase: TimerCardPhase = TimerCardPhase.DEFAULT,
    val wishedProducts: ImmutableList<WishedProduct> = persistentListOf(),
    val storeInfo: StoreInfo? = null,
    val isLoading: Boolean = false,
)
