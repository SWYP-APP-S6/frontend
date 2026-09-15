package com.swyp.mangro.feature.consumer.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun HomeRoute(
    navigateToLocationSelector: () -> Unit,
    navigateToProductDetail: (String) -> Unit,
    navigateToWishList: () -> Unit,
    navigateToMy: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                HomeUiEvent.RequestLocationPermission -> {
                    // TODO: ActivityResultContracts.RequestPermission() 연동
                }
                is HomeUiEvent.NavigateToProductDetail -> navigateToProductDetail(event.productId)
                HomeUiEvent.NavigateToWishList -> navigateToWishList()
                HomeUiEvent.NavigateToMy -> navigateToMy()
            }
        }
    }

    HomeScreen(
        uiState = uiState,
        onAction = viewModel::handleAction,
    )
}
