package com.swyp.mangro

import androidx.compose.runtime.Composable
import com.swyp.mangro.feature.owner.pickup.OwnerPickupRoute

/** API 미연결 상태. 샘플 고객이나 가짜 처리 결과를 Release에 제공하지 않는다. */
@Composable
internal fun OwnerPickupEntry(onHomeClick: () -> Unit) {
    OwnerPickupRoute(
        pickups = emptyList(),
        stock = emptyMap(),
        storeName = "",
        storePhone = "",
        onCompletePickup = { error("Pickup API is not connected") },
        onCancelPickups = { _, _ -> error("Pickup API is not connected") },
        onHomeClick = onHomeClick,
    )
}

internal const val IS_PICKUP_DEMO = false
