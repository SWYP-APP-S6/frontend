package com.swyp.mangro.feature.auth.terms

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun TermsRoute(
    navigateToOnboarding: () -> Unit,
    navigateToTermsDetail: (Long) -> Unit,
    onBackClick: () -> Unit,
    viewModel: TermsViewModel = hiltViewModel(),
    navigateToLogin: () -> Unit = onBackClick,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.event.collect { event ->
            when (event) {
                TermsUiEvent.NavigateToLogin -> navigateToLogin()
                TermsUiEvent.NavigateToOnboarding -> navigateToOnboarding()
                is TermsUiEvent.NavigateToTermsDetail -> navigateToTermsDetail(event.id)
            }
        }
    }
    BackHandler(enabled = uiState.isSubmitting) {}
    TermsScreen(uiState = uiState, onAction = viewModel::handleAction, onBackClick = { if (!uiState.isSubmitting) onBackClick() })
}
