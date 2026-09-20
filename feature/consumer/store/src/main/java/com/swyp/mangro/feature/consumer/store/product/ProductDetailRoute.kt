package com.swyp.mangro.feature.consumer.store.product

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.swyp.mangro.core.designsystem.component.dialog.MangroDialogContainer

@Composable
fun ProductDetailRoute(
    onBackClick: () -> Unit,
    onNavigateToStoreDetail: () -> Unit,
    onNavigateToHold: (String) -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProductDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showLoginRequiredDialog by rememberSaveable { androidx.compose.runtime.mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is ProductDetailUiEvent.NavigateToStoreDetail -> onNavigateToStoreDetail()
                is ProductDetailUiEvent.WishConfirmed -> onNavigateToHold(event.holdId)
                is ProductDetailUiEvent.ShowLoginRequiredDialog -> showLoginRequiredDialog = true
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

    MangroDialogContainer(
        show = showLoginRequiredDialog,
        onDismissRequest = { showLoginRequiredDialog = false },
        title = { androidx.compose.material3.Text("로그인이 필요합니다") },
        content = { androidx.compose.material3.Text("찜하기는 로그인 후 이용할 수 있어요.") },
        actions = {
            com.swyp.mangro.core.designsystem.component.MangroButton(
                text = "로그인하기",
                onClick = {
                    showLoginRequiredDialog = false
                    onNavigateToLogin()
                },
                style = com.swyp.mangro.core.designsystem.component.MangroButtonStyle.ACTIVE,
                modifier = Modifier.fillMaxWidth(),
            )
        },
    )

    ProductDetailScreen(
        uiState = uiState,
        onAction = viewModel::handleAction,
        onBackClick = onBackClick,
        modifier = modifier,
    )
}
