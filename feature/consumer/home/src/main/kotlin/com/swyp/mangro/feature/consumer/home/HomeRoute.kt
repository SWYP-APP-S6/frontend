package com.swyp.mangro.feature.consumer.home

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun HomeRoute(
    navigateToProductDetail: (String) -> Unit,
    navigateToWishList: () -> Unit,
    navigateToMy: () -> Unit,
    navigateToHold: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        viewModel.updateLocationPermission(isGranted)
    }

    LaunchedEffect(Unit) {
        val alreadyGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        viewModel.updateLocationPermission(alreadyGranted)
    }

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                HomeUiEvent.RequestLocationPermission -> {
                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
                is HomeUiEvent.NavigateToProductDetail -> navigateToProductDetail(event.productId)
                HomeUiEvent.NavigateToWishList -> navigateToWishList()
                HomeUiEvent.NavigateToMy -> navigateToMy()
                is HomeUiEvent.NavigateToHold -> navigateToHold(event.holdId)
            }
        }
    }

    HomeScreen(
        uiState = uiState,
        onAction = viewModel::handleAction,
    )
}
