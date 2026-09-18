package com.swyp.mangro.feature.consumer.store.product

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ProductDetailRoute(
    onBackClick: () -> Unit,
    onNavigateToStoreDetail: () -> Unit,
    onNavigateToHold: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProductDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is ProductDetailUiEvent.NavigateToStoreDetail -> onNavigateToStoreDetail()
                is ProductDetailUiEvent.WishConfirmed -> onNavigateToHold(event.holdId)
            }
        }
    }

    ProductDetailScreen(
        uiState = uiState,
        onAction = viewModel::handleAction,
        onBackClick = onBackClick,
        modifier = modifier,
    )
}
