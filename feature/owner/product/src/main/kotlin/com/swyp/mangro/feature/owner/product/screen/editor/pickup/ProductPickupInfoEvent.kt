package com.swyp.mangro.feature.owner.product.screen.editor.pickup

import com.swyp.mangro.feature.owner.product.model.OwnerProductModel
sealed interface ProductPickupInfoEvent {
    data object EditBasicInfo : ProductPickupInfoEvent
    data object Back : ProductPickupInfoEvent
    data class Save(val product: OwnerProductModel) : ProductPickupInfoEvent
}
