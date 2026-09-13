package com.swyp.mangro.feature.owner.product.screen.list

import com.swyp.mangro.core.designsystem.component.appbar.OwnerMenu

sealed interface ProductListEvent {
    data class OpenProduct(val id: String) : ProductListEvent
    data class OpenPickup(val id: String) : ProductListEvent
    data class CompletePickup(val id: String) : ProductListEvent
    data class OpenMenu(val menu: OwnerMenu) : ProductListEvent
    data class CancelReservations(val productIds: List<String>) : ProductListEvent
}
