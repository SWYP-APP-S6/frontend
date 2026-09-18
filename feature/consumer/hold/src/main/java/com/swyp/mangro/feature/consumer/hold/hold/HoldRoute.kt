package com.swyp.mangro.feature.consumer.hold.hold

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun HoldRoute(
    onNavigateToProductDetail: () -> Unit,
    onNavigateToHomeList: () -> Unit,
    onNavigateToPickupComplete: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HoldViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is HoldUiEvent.NavigateToProductDetail -> onNavigateToProductDetail()
                is HoldUiEvent.NavigateToHomeList -> onNavigateToHomeList()
                is HoldUiEvent.NavigateToPickupComplete -> onNavigateToPickupComplete(event.holdId)
                is HoldUiEvent.OpenMapDirections -> {
                    val store = event.storeInfo
                    val uri = (
                        "nmap://route/walk?dlat=${store.latitude}&dlng=${store.longitude}" +
                            "&dname=${Uri.encode(store.name)}&appname=${context.packageName}"
                        ).toUri()
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    try {
                        context.startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        val marketUri = "market://details?id=com.nhn.android.nmap".toUri()
                        context.startActivity(Intent(Intent.ACTION_VIEW, marketUri))
                    }
                }
                is HoldUiEvent.CopyAddress -> {
                    clipboardManager.setText(AnnotatedString(event.address))
                }
                is HoldUiEvent.OpenDialer -> {
                    val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${event.phoneNumber}"))
                    context.startActivity(dialIntent)
                }
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
