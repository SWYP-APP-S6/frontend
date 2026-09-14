package com.swyp.mangro.feature.auth.login

sealed interface LoginUiAction {
    data object KakaoLoginClicked : LoginUiAction
    data object BrowseWithoutLoginClicked : LoginUiAction
    data object PrivacyPolicyClicked : LoginUiAction
}
