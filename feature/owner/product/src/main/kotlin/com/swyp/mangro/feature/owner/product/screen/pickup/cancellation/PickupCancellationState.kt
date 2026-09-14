package com.swyp.mangro.feature.owner.product.screen.pickup.cancellation

import com.swyp.mangro.feature.owner.product.model.PickupShortage

data class PickupCancellationState(
    val shortages: List<PickupShortage> = emptyList(),
    val excludedIds: Set<String> = emptySet(),
    val storeName: String = "",
    val storePhone: String = "",
    val showConfirmation: Boolean = false,
    val confirmationIds: Set<String> = emptySet(),
    val hasError: Boolean = false,
) {
    val targets get() = shortages.flatMap { it.targets }
    val selectedIds get() = if (showConfirmation) confirmationIds else targets.map { it.id }.filterNot { it in excludedIds }.toSet()
    val selectedGroups get() = shortages.map { it.productName to it.targets.count { pickup -> pickup.id in selectedIds } }.filter { it.second > 0 }
}
