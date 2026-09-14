package com.swyp.mangro.feature.owner.onboarding

import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebViewAssetLoader
import com.swyp.mangro.feature.owner.onboarding.Constants.POSTCODE_URL
import com.swyp.mangro.feature.owner.onboarding.model.StoreAddressModel
import com.swyp.mangro.feature.owner.onboarding.screen.address.createAddressSearchWebView
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

class AddressSearchWebViewTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun bundledPageLoadsAndReturnsSelectedRoadAddress() = verifyAddress("R", "서울 마포구 망원로 12")

    @Test
    fun bundledPageLoadsAndReturnsSelectedJibunAddress() = verifyAddress("J", "서울 마포구 망원동 1")

    private fun verifyAddress(selectedType: String, expectedAddress: String) {
        val ready = AtomicBoolean(false)
        val failed = AtomicBoolean(false)
        val selected = AtomicReference<StoreAddressModel?>()
        compose.setContent {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    createAddressSearchWebView(context, { ready.set(true) }, { failed.set(true) }, { selected.set(it) }).apply {
                        val loader = WebViewAssetLoader.Builder()
                            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(context))
                            .build()
                        // Replace only the remote service; load the actual packaged HTML and native bridge.
                        webViewClient = object : WebViewClient() {
                            override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
                                if (request.url.host == "t1.kakaocdn.net") {
                                    val script = """
                                        window.kakao = { Postcode: function(options) {
                                            this.embed = function(container) {
                                                if (!container || container.clientHeight <= 0) throw Error('Missing container');
                                                options.onresize({height: 400});
                                                options.oncomplete({
                                                    userSelectedType: '$selectedType', zonecode: '03965',
                                                    roadAddress: '서울 마포구 망원로 12', jibunAddress: '서울 마포구 망원동 1'
                                                });
                                            };
                                        }};
                                    """.trimIndent()
                                    return WebResourceResponse("text/javascript", "UTF-8", script.byteInputStream())
                                }
                                return loader.shouldInterceptRequest(request.url)
                            }
                        }
                        loadUrl(POSTCODE_URL)
                    }
                },
                onRelease = { it.destroy() },
            )
        }
        compose.waitUntil(10_000) { failed.get() || (ready.get() && selected.get() != null) }
        assertFalse(failed.get())
        assertEquals(StoreAddressModel("03965", expectedAddress), selected.get())
    }
}
