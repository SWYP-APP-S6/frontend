package com.swyp.mangro.feature.consumer.home

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
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
        viewModel.onLocationPermissionChecked(
            isGranted = context.hasLocationPermission(),
            canShowIntro = !context.hasShownPermissionIntro(),
        )
        viewModel.refreshNearbyProducts()
    }

    LaunchedEffect(uiState.isPermissionIntroVisible) {
        if (uiState.isPermissionIntroVisible) context.markPermissionIntroShown()
    }

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                HomeUiEvent.RequestLocationPermission -> {
                    if (context.canShowPermissionDialog() || !context.hasRequestedLocationPermission()) {
                        context.markLocationPermissionRequested()
                        permissionLauncher.launch(LOCATION_PERMISSIONS)
                    } else {
                        context.openAppSettings()
                    }
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

private fun Context.hasLocationPermission(): Boolean = LOCATION_PERMISSIONS.any {
    ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
}

private const val PERMISSION_PREFS = "location_permission"
private const val KEY_REQUESTED = "requested"
private const val KEY_INTRO_SHOWN = "intro_shown"

private fun Context.permissionPrefs() = getSharedPreferences(PERMISSION_PREFS, Context.MODE_PRIVATE)

private fun Context.hasRequestedLocationPermission(): Boolean = permissionPrefs().getBoolean(KEY_REQUESTED, false)

private fun Context.markLocationPermissionRequested() {
    permissionPrefs().edit().putBoolean(KEY_REQUESTED, true).apply()
}

private fun Context.hasShownPermissionIntro(): Boolean = permissionPrefs().getBoolean(KEY_INTRO_SHOWN, false)

private fun Context.markPermissionIntroShown() {
    permissionPrefs().edit().putBoolean(KEY_INTRO_SHOWN, true).apply()
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
