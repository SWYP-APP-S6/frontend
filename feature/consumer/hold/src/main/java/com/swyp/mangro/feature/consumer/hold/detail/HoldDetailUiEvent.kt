package com.swyp.mangro.feature.consumer.hold.detail

import androidx.annotation.StringRes

sealed interface HoldDetailUiEvent {
    data object NavigateToHoldHistory : HoldDetailUiEvent
    data class ShowToast(@StringRes val messageRes: Int) : HoldDetailUiEvent
}
