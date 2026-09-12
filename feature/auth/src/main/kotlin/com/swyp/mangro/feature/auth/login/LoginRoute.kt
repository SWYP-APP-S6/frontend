package com.swyp.mangro.feature.auth.login

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun LoginRoute(
    navigateToHome: () -> Unit,
    navigateToPrivacyPolicy: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                LoginUiEvent.NavigateToHome -> navigateToHome()
                LoginUiEvent.NavigateToPrivacyPolicy -> navigateToPrivacyPolicy()
            }
        }
    }

    LoginScreen(
        uiState = uiState,
        onAction = viewModel::handleAction,
    )
}
