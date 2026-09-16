package com.swyp.mangro.feature.consumer.hold.detail

sealed interface HoldDetailUiAction {
    data object OnCancelClick : HoldDetailUiAction
}
