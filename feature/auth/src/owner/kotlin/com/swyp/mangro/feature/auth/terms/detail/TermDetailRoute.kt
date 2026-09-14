package com.swyp.mangro.feature.auth.terms.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun TermDetailRoute(
    navigateBack: () -> Unit,
    refreshTerms: () -> Unit,
    viewModel: TermDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.event.collect {
            when (it) {
                TermDetailEvent.NavigateBack -> navigateBack()
                TermDetailEvent.RefreshTerms -> refreshTerms()
            }
        }
    }
    TermDetailScreen(state = state, onAction = viewModel::handleAction)
}
