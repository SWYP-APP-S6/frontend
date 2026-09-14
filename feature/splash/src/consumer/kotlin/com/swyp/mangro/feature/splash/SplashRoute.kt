package com.swyp.mangro.feature.splash

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun SplashRoute(
    navigateToLogin: () -> Unit,
    navigateToHome: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel(),
) {
    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                SplashUiEvent.NavigateToLogin -> navigateToLogin()
                SplashUiEvent.NavigateToHome -> navigateToHome()
            }
        }
    }

    SplashScreen()
}
