package com.swyp.mangro.feature.consumer.hold.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun HoldDetailRoute(
    onBackClick: () -> Unit,
    onNavigateToHoldHistory: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HoldDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is HoldDetailUiEvent.NavigateToHoldHistory -> onNavigateToHoldHistory()
            }
        }
    }

    HoldDetailScreen(
        uiState = uiState,
        onAction = viewModel::handleAction,
        onBackClick = onBackClick,
        modifier = modifier,
    )
}
