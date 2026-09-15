package com.swyp.mangro.feature.consumer.hold.hold

import com.swyp.mangro.core.model.store.StoreInfo

sealed interface HoldUiEvent {
    data object NavigateToProductDetail : HoldUiEvent
    data class OpenMapDirections(val storeInfo: StoreInfo) : HoldUiEvent
    data class CopyAddress(val address: String) : HoldUiEvent
    data class OpenDialer(val phoneNumber: String) : HoldUiEvent
}
