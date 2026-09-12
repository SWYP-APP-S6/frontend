package com.swyp.mangro.feature.auth.login

sealed interface LoginUiEvent {
    data object NavigateToHome : LoginUiEvent
    data object NavigateToPrivacyPolicy : LoginUiEvent
}
