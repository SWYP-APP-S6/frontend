package com.swyp.mangro.feature.auth.login

sealed interface LoginUiAction {
    data object KakaoLoginClicked : LoginUiAction
    data object PrivacyPolicyClicked : LoginUiAction
    class KakaoTokenReceived(val token: String) : LoginUiAction
    data class KakaoLoginFailed(val cancelled: Boolean) : LoginUiAction
}
