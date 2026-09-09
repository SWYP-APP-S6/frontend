package com.swyp.mangro.feature.owner.home.screen.model

sealed interface OwnerHomeAction {
    data object RegisterProduct : OwnerHomeAction
    data object ViewProducts : OwnerHomeAction
    data class ViewProduct(val productId: String) : OwnerHomeAction
    data object ViewPickups : OwnerHomeAction
    data object ViewNewPickups : OwnerHomeAction
    data object ViewCompletedPickups : OwnerHomeAction
    data object ConfirmPickups : OwnerHomeAction
    data object ViewCancellations : OwnerHomeAction
    data object DismissAttention : OwnerHomeAction
    data object ViewNotifications : OwnerHomeAction
    data class ViewPickup(val pickupId: String) : OwnerHomeAction
    data class CompletePickup(val pickupId: String) : OwnerHomeAction
}
