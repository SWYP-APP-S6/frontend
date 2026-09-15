package com.swyp.mangro.feature.consumer.hold.hold

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun HoldRoute(
    onNavigateToProductDetail: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HoldViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is HoldUiEvent.NavigateToProductDetail -> onNavigateToProductDetail()
                is HoldUiEvent.OpenMapDirections -> {}
                is HoldUiEvent.CopyAddress -> {}
                is HoldUiEvent.OpenDialer -> {}
            }
        }
    }

    HoldScreen(
        uiState = uiState,
        onAction = viewModel::handleAction,
        onCloseClick = onNavigateToProductDetail,
        modifier = modifier,
    )
}
