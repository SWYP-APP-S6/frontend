package com.swyp.mangro.feature.owner.product.screen.pickup.detail

sealed interface PickupDetailEvent {
    data object NavigateBack : PickupDetailEvent
    data object NavigateToHome : PickupDetailEvent
}
