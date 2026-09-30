package com.swyp.mangro.feature.owner.product.screen.list

sealed interface ProductListEvent {
    data class OpenProduct(val id: String) : ProductListEvent
    data class OpenPickup(val id: String) : ProductListEvent
    data class CancelReservations(val productIds: List<String>) : ProductListEvent
}
