package com.swyp.mangro.feature.owner.product.screen.pickup.cancellation

sealed interface PickupCancellationAction {
    data object Refresh : PickupCancellationAction
    data class SelectionChanged(val id: String, val selected: Boolean) : PickupCancellationAction
    data object CancelClicked : PickupCancellationAction
    data object ConfirmationDismissed : PickupCancellationAction
    data object ConfirmationClicked : PickupCancellationAction
    data object NavigationBackClicked : PickupCancellationAction
}
