package com.swyp.mangro.feature.owner.home.screen.model

import com.swyp.mangro.core.designsystem.component.card.owner.OwnerProduct
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

data class OwnerHomeUiState(
    val hasRegisteredProduct: Boolean = false,
    val storeName: String = "",
    val storeCategory: String = "",
    val expectedVisitCount: Int = 0,
    val completedPickupCount: Int = 0,
    val sellingCount: Int = 0,
    val hasNewPickup: Boolean = false,
    val cancellationRequiredCount: Int = 0,
    val needsPickupConfirmation: Boolean = false,
    val isAttentionDismissed: Boolean = false,
    val visitors: PersistentList<OwnerHomeVisitor> = persistentListOf(),
    val products: PersistentList<OwnerProduct> = persistentListOf(),
) {
    val hasAttention: Boolean
        get() = cancellationRequiredCount > 0 || needsPickupConfirmation

    val showAttention: Boolean
        get() = hasAttention && !isAttentionDismissed
}

data class OwnerHomeVisitor(
    val id: String,
    val customerName: String,
    val productName: String,
    val quantity: Int,
    val pickupDeadlineMillis: Long,
)

internal fun remainingPickupMinutes(deadlineMillis: Long, nowMillis: Long): Long {
    val remaining = (deadlineMillis - nowMillis).coerceAtLeast(0L)
    return remaining / 60_000 + if (remaining % 60_000 > 0) 1 else 0
}
