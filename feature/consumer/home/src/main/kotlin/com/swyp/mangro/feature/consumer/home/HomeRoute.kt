package com.swyp.mangro.feature.consumer.home

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private val LOCATION_PERMISSIONS = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION,
)

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
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { result ->
        viewModel.onLocationPermissionChecked(isGranted = result.values.any { it })
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.onLocationPermissionChecked(isGranted = context.hasLocationPermission())
        viewModel.refreshOnResume()
    }

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is HomeUiEvent.RequestLocationPermission -> {
                    if (context.canShowPermissionDialog() || !event.hasRequestedBefore) {
                        permissionLauncher.launch(LOCATION_PERMISSIONS)
                    } else {
                        context.openAppSettings()
                    }
                }
                is HomeUiEvent.NavigateToProductDetail -> navigateToProductDetail(event.productId)
                HomeUiEvent.NavigateToWishList -> navigateToWishList()
                HomeUiEvent.NavigateToMy -> navigateToMy()
                is HomeUiEvent.NavigateToHold -> navigateToHold(event.holdId)
                is HomeUiEvent.ShowToast ->
                    Toast.makeText(context, event.messageRes, Toast.LENGTH_SHORT).show()
            }
        }
    }

    HomeScreen(
        uiState = uiState,
        onAction = viewModel::handleAction,
    )
}

private fun Context.hasLocationPermission(): Boolean = LOCATION_PERMISSIONS.any {
    ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
}

private fun Context.canShowPermissionDialog(): Boolean {
    val activity = findActivity() ?: return false
    return LOCATION_PERMISSIONS.any { ActivityCompat.shouldShowRequestPermissionRationale(activity, it) }
}

private fun Context.findActivity(): Activity? {
    var current = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}

private fun Context.openAppSettings() {
    startActivity(
        Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null),
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
    )
}
