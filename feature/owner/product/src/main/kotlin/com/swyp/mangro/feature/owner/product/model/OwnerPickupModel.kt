package com.swyp.mangro.feature.owner.product.model

import com.swyp.mangro.core.designsystem.component.card.owner.OwnerPickupRequestItem

/** A reservation is a separate record; stock quantities are not reservation counts. */
data class OwnerPickupModel(
    val productId: String,
    val request: OwnerPickupRequestItem,
)
