package com.swyp.mangro.feature.owner.home.screen

sealed interface OwnerHomeEvent {
    data class ShowMessage(val message: Int) : OwnerHomeEvent
    data object NavigateToSettings : OwnerHomeEvent
    data class NavigateToPickups(val completedOnly: Boolean) : OwnerHomeEvent
    data object NavigateToProducts : OwnerHomeEvent
    data class NavigateToPickup(val pickupId: String) : OwnerHomeEvent
    data class NavigateToProduct(val productId: String) : OwnerHomeEvent
    data object NavigateToRegisterProduct : OwnerHomeEvent
}
