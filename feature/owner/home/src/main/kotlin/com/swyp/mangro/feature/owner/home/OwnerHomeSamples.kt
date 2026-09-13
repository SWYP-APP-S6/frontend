package com.swyp.mangro.feature.owner.home

import com.swyp.mangro.core.designsystem.component.card.owner.OwnerProduct
import com.swyp.mangro.feature.owner.home.screen.OwnerHomeUiState
import com.swyp.mangro.feature.owner.home.screen.OwnerHomeVisitor
import kotlinx.collections.immutable.persistentListOf

/** Temporary UI sample data for Debug and Release until the data layer is connected. */
object OwnerHomeSamples {
    val welcome = OwnerHomeUiState()
    val empty = OwnerHomeUiState(hasRegisteredProduct = true, storeName = "청과마을", storeCategory = "과채류")

    fun operating(nowMillis: Long = System.currentTimeMillis()): OwnerHomeUiState = empty.copy(
        expectedVisitCount = 10,
        completedPickupCount = 8,
        sellingCount = 20,
        hasNewPickup = true,
        cancellationRequiredCount = 2,
        needsPickupConfirmation = true,
        visitors = persistentListOf(
            OwnerHomeVisitor("pickup-1", "윤지현", "시금치 한 단", 1, nowMillis + 8 * 60_000),
            OwnerHomeVisitor("pickup-2", "닉네임최대몇글자까지", "상품명은25자내외까지허용입니다그것을넘어가면안돼", 1, nowMillis + 8 * 60_000),
            OwnerHomeVisitor("pickup-3", "망그로", "콩나물 한 바구니", 2, nowMillis + 12 * 60_000),
        ),
        products = persistentListOf(
            OwnerProduct("product-1", "https://picsum.photos/400/400?random=1", "시금치 한 단", 4_000, 6, 4, 2),
            OwnerProduct("product-2", "https://picsum.photos/400/400?random=2", "애호박", 4_000, 2, 3, 0),
            OwnerProduct("product-3", "https://picsum.photos/400/400?random=3", "콩나물 한 바구니", 4_000, 4, 3, 2),
        ),
    )
}
