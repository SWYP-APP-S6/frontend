package com.swyp.mangro.feature.consumer.hold.hold

import com.swyp.mangro.core.designsystem.component.card.wishlist.WishedProduct
import com.swyp.mangro.core.model.store.StoreInfo
import kotlinx.collections.immutable.persistentListOf

private val now = System.currentTimeMillis()

val dummyWishedProducts = persistentListOf(
    WishedProduct(
        id = "1",
        imageUrl = "",
        name = "복숭아 4입",
        quantity = 1,
        price = 4_000,
        originalPrice = 10_000,
    ),
)

val dummyStoreInfo = StoreInfo(
    id = 1L,
    name = "청과마을",
    address = "서울 마포구 망원로 12",
    phoneNumber = "02-5894-1982",
    distanceMeters = 450,
    travelInfo = "도보 7분",
    closingTime = "영업중 · 10:00~21:00",
)

val dummyHoldUiState = HoldUiState(
    requestTimeMillis = now - 5_000,
    endTimeMillis = now + 9 * 60_000,
    wishedProducts = dummyWishedProducts,
    storeInfo = dummyStoreInfo,
)

val dummyHoldUiStateCaution = dummyHoldUiState.copy(
    requestTimeMillis = now - 11 * 60_000,
    endTimeMillis = now + 4 * 60_000,
)

val dummyHoldUiStateExpired = dummyHoldUiState.copy(
    requestTimeMillis = now - 20 * 60_000,
    endTimeMillis = now - 5 * 60_000,
)

val dummyHoldUiStateCancelled = dummyHoldUiState.copy(
    isCancelled = true,
)
