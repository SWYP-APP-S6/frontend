package com.swyp.mangro.feature.owner.product.screen.pickup.detail

import com.swyp.mangro.data.owner.product.model.HoldItem
import com.swyp.mangro.feature.owner.product.model.Pickup
import com.swyp.mangro.feature.owner.product.model.PickupStatus

data class PickupDetailState(
    val pickup: Pickup? = null,
    val items: List<HoldItem> = emptyList(),
    val totalPrice: Long = 0,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val storeName: String = "",
    val now: Long = 0,
    val canComplete: Boolean = false,
    val hasError: Boolean = false,
) {
    val status: PickupStatus? get() = pickup?.statusAt(now)
    val remaining: Long get() = if (status == PickupStatus.WAITING) ((pickup?.deadline ?: now) - now).coerceAtLeast(0) else 0
}
