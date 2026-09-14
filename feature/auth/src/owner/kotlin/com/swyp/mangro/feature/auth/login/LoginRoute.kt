package com.swyp.mangro.feature.auth.login

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun LoginRoute(
    navigateToHome: () -> Unit,
    navigateToPrivacyPolicy: () -> Unit,
    navigateToTerms: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                LoginUiEvent.LaunchKakaoLogin -> launchOwnerKakaoLogin(context, viewModel::handleAction)
                LoginUiEvent.NavigateToHome -> navigateToHome()
                LoginUiEvent.NavigateToPrivacyPolicy -> navigateToPrivacyPolicy()
                LoginUiEvent.NavigateToTerms -> navigateToTerms()
            }
        }
    }

    LoginScreen(
        uiState = uiState,
        onAction = viewModel::handleAction,
    )
}
