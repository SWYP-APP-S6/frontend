package com.swyp.mangro

import com.swyp.mangro.feature.owner.home.screen.OwnerHomePickupState
import com.swyp.mangro.feature.owner.home.screen.OwnerHomeVisitor
import com.swyp.mangro.feature.owner.product.data.PickupSnapshot
import com.swyp.mangro.feature.owner.product.model.PickupStatus
import com.swyp.mangro.feature.owner.product.model.canCompletePickup
import com.swyp.mangro.feature.owner.product.model.pickupShortages
import kotlinx.collections.immutable.toPersistentList

internal fun PickupSnapshot.toHomePickups(now: Long): OwnerHomePickupState {
    val waiting = pickups.filter { it.statusAt(now) == PickupStatus.WAITING }
    return OwnerHomePickupState(
        storeName = storeName,
        visitors = waiting.sortedBy { it.deadline }.map { pickup ->
            OwnerHomeVisitor(
                id = pickup.id,
                customerName = pickup.customerName,
                productName = pickup.productName,
                quantity = pickup.quantity,
                pickupDeadlineMillis = pickup.deadline,
                canComplete = isDemo && canCompletePickup(pickup, pickups, stock, now),
            )
        }.toPersistentList(),
        completedCount = pickups.count { it.statusAt(now) == PickupStatus.COMPLETED },
        cancellationRequiredCount = pickupShortages(pickups, stock, now).sumOf { it.targets.size },
        hasNewPickup = waiting.any { it.isNew },
        needsPickupConfirmation = pickups.any { it.statusAt(now) == PickupStatus.EXPIRED },
    )
}
