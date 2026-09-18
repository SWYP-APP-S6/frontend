package com.swyp.mangro.feature.consumer.store.product

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
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
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is ProductDetailUiEvent.NavigateToStoreDetail -> onNavigateToStoreDetail()
                is ProductDetailUiEvent.WishConfirmed -> onNavigateToHold(event.holdId)
                is ProductDetailUiEvent.OpenMapDirections -> {
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
