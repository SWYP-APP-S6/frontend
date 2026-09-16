package com.swyp.mangro.feature.consumer.hold.history

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.swyp.mangro.core.designsystem.component.appbar.ConsumerMenu

@Composable
fun HoldHistoryRoute(
    onNavigateToHoldDetail: (String) -> Unit,
    onNavigateToMenu: (ConsumerMenu) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HoldHistoryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is HoldHistoryUiEvent.NavigateToHoldDetail -> onNavigateToHoldDetail(event.id)
                is HoldHistoryUiEvent.NavigateToMenu -> onNavigateToMenu(event.menu)
            }
        }
    }

    HoldHistoryScreen(
        uiState = uiState,
        onAction = viewModel::handleAction,
        modifier = modifier,
    )
}
