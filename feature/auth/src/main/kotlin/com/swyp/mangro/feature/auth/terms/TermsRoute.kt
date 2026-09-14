package com.swyp.mangro.feature.auth.terms

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun TermsRoute(
    navigateToHome: () -> Unit,
    navigateToTermsDetail: (TermsType) -> Unit,
    onBackClick: () -> Unit,
    viewModel: TermsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                TermsUiEvent.NavigateToHome -> navigateToHome()
                is TermsUiEvent.NavigateToTermsDetail -> navigateToTermsDetail(event.type)
            }
        }
    }

    TermsScreen(
        uiState = uiState,
        onAction = viewModel::handleAction,
        onBackClick = onBackClick,
    )
}
