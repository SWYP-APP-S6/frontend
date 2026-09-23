package com.swyp.mangro.feature.consumer.myinfo

data class MyInfoUiState(
    val isLoading: Boolean = true,
    val isGuest: Boolean = false,
    val nickname: String = "",
    val phone: String = "",
    val hasProfileError: Boolean = false,
    val showLogoutConfirmation: Boolean = false,
    val isLoggingOut: Boolean = false,
    val hasLogoutError: Boolean = false,
    val showWithdrawConfirmation: Boolean = false,
    val isWithdrawing: Boolean = false,
    val hasWithdrawError: Boolean = false,
    val showHoldRemainDialog: Boolean = false,
) {
    val isBusy: Boolean get() = isLoggingOut || isWithdrawing
}
