package com.swyp.mangro.feature.auth.login

sealed interface LoginUiEvent {
    data object LaunchKakaoLogin : LoginUiEvent
    data object NavigateToHome : LoginUiEvent
    data object NavigateToPrivacyPolicy : LoginUiEvent
    data object NavigateToTerms : LoginUiEvent
}
