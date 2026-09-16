package com.swyp.mangro.feature.auth.login

import com.swyp.mangro.data.auth.model.AuthFailure

sealed interface LoginUiEvent {
    data class ShowAuthFailure(val reason: AuthFailure) : LoginUiEvent
    data object LaunchKakaoLogin : LoginUiEvent
    data class ShowKakaoLoginResult(val result: KakaoLoginResult) : LoginUiEvent
    data object NavigateToHome : LoginUiEvent
    data object NavigateToPrivacyPolicy : LoginUiEvent
    data object NavigateToTerms : LoginUiEvent
}
