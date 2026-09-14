package com.swyp.mangro.feature.owner.home.screen

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

data class OwnerHomePickupState(
    val storeName: String = "",
    val visitors: PersistentList<OwnerHomeVisitor> = persistentListOf(),
    val completedCount: Int = 0,
    val cancellationRequiredCount: Int = 0,
    val hasNewPickup: Boolean = false,
    val needsPickupConfirmation: Boolean = false,
)
