package com.swyp.mangro.feature.splash

sealed interface SplashUiEvent {
    data object NavigateToLogin : SplashUiEvent
    data object NavigateToHome : SplashUiEvent
}
