package com.swyp.mangro.feature.owner.onboarding.screen.address

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebViewAssetLoader
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import com.swyp.mangro.core.designsystem.R as DesignR
import com.swyp.mangro.core.designsystem.component.appbar.MangroDefaultStartAlignedTopAppBar
import com.swyp.mangro.core.designsystem.screen.UnStableNetworkConnectionScreen
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.feature.owner.onboarding.Constants.POSTCODE_ORIGIN
import com.swyp.mangro.feature.owner.onboarding.Constants.POSTCODE_URL
import com.swyp.mangro.feature.owner.onboarding.R
import com.swyp.mangro.feature.owner.onboarding.model.StoreAddressModel
import kotlinx.serialization.Serializable
import org.json.JSONObject

@Serializable
data object AddressSearchDestination

@Composable
internal fun AddressSearchWebView(
    onDismiss: () -> Unit,
    onSelect: (StoreAddressModel) -> Unit,
) {
    var loading by remember { mutableStateOf(true) }
    var failed by remember { mutableStateOf(false) }

    BackHandler(onBack = onDismiss)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            MangroDefaultStartAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.address_search),
                        color = MangroTheme.colors.grayScale900,
                        style = MangroTheme.typography.heading.headingS.copy(fontSize = 18.sp),
                    )
                },
                navigationIcon = {
                    Icon(
                        painter = painterResource(DesignR.drawable.ic_arrow_left),
                        contentDescription = stringResource(R.string.back),
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .size(24.dp)
                            .clickable(role = Role.Button, onClick = onDismiss),
                        tint = MangroTheme.colors.textTitle,
                    )
                },
            )
        },
        containerColor = MangroTheme.colors.surfaceNormal,
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
                .imePadding(),
        ) {
            if (!failed) {
                AndroidView(
                    factory = { context ->
                        createAddressSearchWebView(
                            context = context,
                            onReady = { loading = false },
                            onError = { failed = true },
                            onSelect = onSelect,
                        ).apply { loadUrl(POSTCODE_URL) }
                    },
                    modifier = Modifier.fillMaxSize(),
                    onRelease = {
                        it.stopLoading()
                        it.destroy()
                    },
                )

                if (loading) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = MangroTheme.colors.primaryNormal,
                    )
                }
            } else {
                UnStableNetworkConnectionScreen {
                    loading = true
                    failed = false
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled", "RequiresFeature", "MissingOnRenderProcessGone")
internal fun createAddressSearchWebView(
    context: Context,
    onReady: () -> Unit,
    onError: () -> Unit,
    onSelect: (StoreAddressModel) -> Unit,
): WebView = WebView(context).apply {
    layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    if (!WebViewFeature.isFeatureSupported(WebViewFeature.WEB_MESSAGE_LISTENER)) {
        post { onError() }
        return@apply
    }

    settings.javaScriptEnabled = true
    settings.domStorageEnabled = true
    settings.allowFileAccess = false
    settings.allowContentAccess = false

    val assetLoader = WebViewAssetLoader
        .Builder()
        .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(context))
        .build()

    webViewClient = object : WebViewClient() {
        override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? = assetLoader.shouldInterceptRequest(request.url)

        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean = if (request.isForMainFrame) request.url.toString() != POSTCODE_URL else request.url.scheme != "https"

        override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
            if (request.isForMainFrame) onError()
        }

        override fun onReceivedHttpError(view: WebView, request: WebResourceRequest, errorResponse: WebResourceResponse) {
            if (request.isForMainFrame) onError()
        }

        override fun onRenderProcessGone(view: WebView, detail: RenderProcessGoneDetail): Boolean {
            onError()
            return true
        }
    }

    WebViewCompat.addWebMessageListener(this, "MangroPostcode", setOf(POSTCODE_ORIGIN)) { _, message, origin, isMainFrame, _ ->
        if (isMainFrame && origin.toString() == POSTCODE_ORIGIN) {
            val payload = runCatching { JSONObject(message.data.orEmpty()) }.getOrNull()
            when (payload?.optString("type")) {
                "ready" -> onReady()

                "error" -> onError()

                "complete" -> {
                    val postalCode = (payload.opt("zonecode") as? String).orEmpty().trim()
                    val address = (payload.opt("address") as? String).orEmpty().trim()
                    if (postalCode.matches(Regex("[0-9]{5}")) && address.isNotBlank()) {
                        onSelect(StoreAddressModel(postalCode, address))
                    } else {
                        onError()
                    }
                }
            }
        }
    }
}
