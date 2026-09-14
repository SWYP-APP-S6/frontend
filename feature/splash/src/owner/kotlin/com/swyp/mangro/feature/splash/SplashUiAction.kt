package com.swyp.mangro.feature.splash

sealed interface SplashUiAction {
    data object RetryClicked : SplashUiAction
}
