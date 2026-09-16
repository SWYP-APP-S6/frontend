package com.swyp.mangro.feature.consumer.hold.detail

sealed interface HoldDetailUiEvent {
    data object NavigateToHoldHistory : HoldDetailUiEvent
}
