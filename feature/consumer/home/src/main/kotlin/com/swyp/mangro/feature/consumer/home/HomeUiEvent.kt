package com.swyp.mangro.feature.consumer.home

sealed interface HomeUiEvent {
    data class RequestLocationPermission(val hasRequestedBefore: Boolean) : HomeUiEvent
    data class NavigateToProductDetail(val productId: String) : HomeUiEvent
    data object NavigateToWishList : HomeUiEvent
    data object NavigateToMy : HomeUiEvent
    data class NavigateToHold(val holdId: String) : HomeUiEvent
}
