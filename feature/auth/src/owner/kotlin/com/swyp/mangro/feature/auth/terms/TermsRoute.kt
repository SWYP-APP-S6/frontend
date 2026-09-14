package com.swyp.mangro.feature.auth.terms

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun TermsRoute(
    navigateToHome: () -> Unit,
    navigateToTermsDetail: (Long) -> Unit,
    onBackClick: () -> Unit,
    viewModel: TermsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.event.collect { event ->
            when (event) {
                TermsUiEvent.NavigateToHome -> navigateToHome()
                is TermsUiEvent.NavigateToTermsDetail -> navigateToTermsDetail(event.id)
            }
        }
    }
    TermsScreen(uiState = uiState, onAction = viewModel::handleAction, onBackClick = onBackClick)
}
