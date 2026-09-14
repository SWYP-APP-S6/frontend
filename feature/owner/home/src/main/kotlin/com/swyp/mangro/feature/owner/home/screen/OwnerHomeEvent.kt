package com.swyp.mangro.feature.owner.home.screen

sealed interface OwnerHomeEvent {
    data object NavigateToRegisterStore : OwnerHomeEvent
    data object NavigateToPickups : OwnerHomeEvent
    data object NavigateToCompletedPickups : OwnerHomeEvent
    data object NavigateToExpiredPickups : OwnerHomeEvent
    data object NavigateToCancellations : OwnerHomeEvent
    data class NavigateToPickup(val pickupId: String) : OwnerHomeEvent
    data class CompletePickup(val pickupId: String) : OwnerHomeEvent
    data object ShowNotificationsPreparing : OwnerHomeEvent
    data object NavigateToSettings : OwnerHomeEvent
    data object NavigateToStore : OwnerHomeEvent
    data object NavigateToProducts : OwnerHomeEvent
    data class NavigateToProduct(val productId: String) : OwnerHomeEvent
    data object NavigateToRegisterProduct : OwnerHomeEvent
}
