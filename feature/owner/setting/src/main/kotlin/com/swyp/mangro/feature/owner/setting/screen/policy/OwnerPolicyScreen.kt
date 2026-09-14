package com.swyp.mangro.feature.owner.setting.screen.policy

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.swyp.mangro.core.designsystem.R as DesignR
import com.swyp.mangro.core.designsystem.component.appbar.MangroDefaultStartAlignedTopAppBar
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.feature.owner.setting.R

@Composable
internal fun OwnerPolicyRoute(
    navigateBack: () -> Unit,
    viewModel: OwnerPolicyViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    BackHandler { viewModel.handleAction(OwnerPolicyAction.NavigationBackClicked) }
    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                OwnerPolicyEvent.NavigateBack -> navigateBack()
            }
        }
    }
    OwnerPolicyScreen(uiState = uiState, onAction = viewModel::handleAction)
}

@Composable
fun OwnerPolicyScreen(
    uiState: OwnerPolicyUiState,
    onAction: (OwnerPolicyAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MangroTheme.colors.surfaceNormal,
        topBar = {
            MangroDefaultStartAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(uiState.policy.titleRes),
                        style = MangroTheme.typography.title.titleM,
                        color = MangroTheme.colors.textTitle,
                    )
                },
                navigationIcon = {
                    Icon(
                        painter = painterResource(DesignR.drawable.ic_arrow_left),
                        contentDescription = stringResource(R.string.owner_setting_back),
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .size(24.dp)
                            .clickable(role = Role.Button, onClick = { onAction(OwnerPolicyAction.NavigationBackClicked) }),
                        tint = MangroTheme.colors.textTitle,
                    )
                },
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    WebView(context).apply { webViewClient = WebViewClient() }
                },
                update = { webView ->
                    val url = uiState.url?.takeIf { it.isNotBlank() } ?: "about:blank"
                    if (webView.tag != url) {
                        webView.tag = url
                        webView.loadUrl(url)
                    }
                },
                onRelease = { webView ->
                    webView.stopLoading()
                    webView.destroy()
                },
            )
            if (uiState.url.isNullOrBlank()) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = stringResource(R.string.owner_setting_policy_pending_title),
                        style = MangroTheme.typography.title.titleM,
                        color = MangroTheme.colors.textTitle,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = stringResource(R.string.owner_setting_policy_pending_body),
                        style = MangroTheme.typography.body.bodyM,
                        color = MangroTheme.colors.textSubtitle,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}
