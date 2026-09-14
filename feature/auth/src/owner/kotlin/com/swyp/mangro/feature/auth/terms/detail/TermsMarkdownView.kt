package com.swyp.mangro.feature.auth.terms.detail

import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.viewinterop.AndroidView
import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer

internal fun termsHtml(markdown: String): String {
    val extensions = listOf(TablesExtension.create())
    val html = HtmlRenderer.builder().extensions(extensions).escapeHtml(true).sanitizeUrls(true).build().render(Parser.builder().extensions(extensions).build().parse(markdown))
    return """<!doctype html><html lang="ko"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1"><meta http-equiv="Content-Security-Policy" content="default-src 'none'; style-src 'unsafe-inline'"><style>body{margin:20px;color:#252525;font-family:sans-serif;font-size:16px;line-height:1.7;overflow-wrap:anywhere}h1,h2,h3{line-height:1.4}pre{white-space:pre-wrap}img{max-width:100%}table{border-collapse:collapse;width:100%;font-size:14px}th,td{border:1px solid #ddd;padding:8px;text-align:left;vertical-align:top}th{background:#f5f5f5}</style></head><body>$html</body></html>"""
}

@Composable
internal fun TermsMarkdownView(markdown: String, modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current
    val html = remember(markdown) { termsHtml(markdown) }
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = false
                settings.allowFileAccess = false
                settings.allowContentAccess = false
                settings.blockNetworkLoads = true
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                        if (request.url.host == "appassets.androidplatform.net" && request.url.fragment != null) return false
                        if (request.url.scheme in setOf("https", "http", "mailto", "tel")) {
                            runCatching { uriHandler.openUri(request.url.toString()) }
                        }
                        return true
                    }
                }
            }
        },
        update = { view ->
            if (view.tag != html) {
                view.tag = html
                view.loadDataWithBaseURL("https://appassets.androidplatform.net/terms/", html, "text/html", "UTF-8", null)
            }
        },
        onRelease = {
            it.stopLoading()
            it.destroy()
        },
    )
}
