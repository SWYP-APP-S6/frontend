package com.swyp.mangro.feature.auth.login

sealed interface LoginUiAction {
    class KakaoLoginCompleted(val result: KakaoLoginResult, val accessToken: String? = null) : LoginUiAction
    data object KakaoLoginClicked : LoginUiAction
    data object BrowseWithoutLoginClicked : LoginUiAction
    data object PrivacyPolicyClicked : LoginUiAction
}
