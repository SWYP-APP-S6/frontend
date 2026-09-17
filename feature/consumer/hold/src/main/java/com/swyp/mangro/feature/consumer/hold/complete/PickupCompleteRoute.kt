package com.swyp.mangro.feature.consumer.hold.complete

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.swyp.mangro.core.designsystem.component.appbar.ConsumerMenu

@Composable
fun PickupCompleteRoute(
    onBackClick: () -> Unit,
    onNavigateToRecipeDetail: (Long) -> Unit,
    onNavigateToMenu: (ConsumerMenu) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PickupCompleteViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is PickupCompleteUiEvent.NavigateToRecipeDetail ->
                    onNavigateToRecipeDetail(event.recipeId)
                is PickupCompleteUiEvent.NavigateToMenu ->
                    onNavigateToMenu(event.menu)
            }
        }
    }

    PickupCompleteScreen(
        uiState = uiState,
        onAction = viewModel::handleAction,
        onBackClick = onBackClick,
        modifier = modifier,
    )
}
