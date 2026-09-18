package com.swyp.mangro.feature.consumer.home

sealed interface HomeUiEvent {
    data object RequestLocationPermission : HomeUiEvent
    data class NavigateToProductDetail(val productId: String) : HomeUiEvent
    data object NavigateToWishList : HomeUiEvent
    data object NavigateToMy : HomeUiEvent
    data class NavigateToHold(val holdId: String) : HomeUiEvent
}
