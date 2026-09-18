package com.swyp.mangro.feature.consumer.myinfo.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.swyp.mangro.core.designsystem.component.appbar.ConsumerMenu
import com.swyp.mangro.feature.consumer.myinfo.MyInfoScreen
import com.swyp.mangro.feature.consumer.myinfo.MyInfoUiEvent
import com.swyp.mangro.feature.consumer.myinfo.MyInfoViewModel
import kotlinx.serialization.Serializable

@Serializable
data object MyInfoDestination

fun NavGraphBuilder.myInfoScreen(
    onNavigateToMenu: (ConsumerMenu) -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToPolicy: (String) -> Unit,
) {
    composable<MyInfoDestination> {
        val viewModel: MyInfoViewModel = hiltViewModel()
        val state by viewModel.uiState.collectAsStateWithLifecycle()

        BackHandler { if (!state.isLoggingOut) onNavigateToMenu(ConsumerMenu.HOME) }

        LaunchedEffect(Unit) {
            viewModel.event.collect { event ->
                when (event) {
                    MyInfoUiEvent.NavigateToLogin -> onNavigateToLogin()
                    is MyInfoUiEvent.NavigateToMenu -> onNavigateToMenu(event.menu)
                    is MyInfoUiEvent.NavigateToPolicy -> onNavigateToPolicy(event.kind.name)
                }
            }
        }

        MyInfoScreen(
            uiState = state,
            onAction = viewModel::handleAction,
        )
    }
}
