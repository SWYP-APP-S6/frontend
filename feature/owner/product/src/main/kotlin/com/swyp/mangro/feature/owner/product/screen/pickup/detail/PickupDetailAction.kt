package com.swyp.mangro.feature.owner.product.screen.pickup.detail

sealed interface PickupDetailAction {
    data object CompleteClicked : PickupDetailAction
    data object HomeClicked : PickupDetailAction
    data object NavigationBackClicked : PickupDetailAction
}
