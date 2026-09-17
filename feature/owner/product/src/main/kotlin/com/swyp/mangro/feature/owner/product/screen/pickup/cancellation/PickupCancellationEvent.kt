package com.swyp.mangro.feature.owner.product.screen.pickup.cancellation

sealed interface PickupCancellationEvent {
    data object HoldsChanged : PickupCancellationEvent
    data object NavigateBack : PickupCancellationEvent
}
