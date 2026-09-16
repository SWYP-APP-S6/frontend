package com.swyp.mangro.feature.consumer.hold.detail

data class HoldDetailUiState(
    val detail: HoldDetailInfo? = null,
    val isCancelButtonEnabled: Boolean = false,
    val isLoading: Boolean = false,
)
