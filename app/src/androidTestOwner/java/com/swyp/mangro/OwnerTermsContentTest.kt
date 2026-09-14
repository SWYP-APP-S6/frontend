package com.swyp.mangro

import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import com.swyp.mangro.data.owner.terms.model.OwnerTerm
import com.swyp.mangro.data.owner.terms.model.OwnerTermDetail
import com.swyp.mangro.data.owner.terms.model.TermsFailure
import com.swyp.mangro.data.owner.terms.model.TermsRequirement
import com.swyp.mangro.data.owner.terms.model.TermsResult
import com.swyp.mangro.data.owner.terms.repository.OwnerTermsRepository
import com.swyp.mangro.feature.auth.terms.TermsRoute
import com.swyp.mangro.feature.auth.terms.TermsViewModel
import com.swyp.mangro.feature.auth.terms.detail.TermDetailRoute
import com.swyp.mangro.feature.auth.terms.detail.TermDetailViewModel
import com.swyp.mangro.theme.MangroTheme
import java.util.concurrent.atomic.AtomicReference
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class OwnerTermsContentTest {
    @get:Rule val compose = createAndroidComposeRule<ProductTestActivity>()
    private val store = ViewModelStore()
    private val documents = listOf(
        OwnerTerm(1, "SERVICE", 1, "필수 이용약관", TermsRequirement.REQUIRED, "2026-09-16"),
        OwnerTerm(2, "MARKETING", 1, "선택 수신 동의", TermsRequirement.OPTIONAL, "2026-09-16"),
        OwnerTerm(3, "PRIVACY_POLICY", 1, "개인정보처리방침", TermsRequirement.NOTICE, "2026-09-16"),
    )
    private var detailResult: TermsResult<OwnerTermDetail> = TermsResult.Failure(TermsFailure.NOT_FOUND)
    private val repository = object : OwnerTermsRepository {
        override suspend fun fetchTerms() = TermsResult.Success(documents)
        override suspend fun fetchTerm(id: Long) = detailResult
    }

    @After fun tearDown() {
        compose.runOnIdle { store.clear() }
    }

    @Test fun noticeHasNoCheckboxAndOpensByDocumentId() {
        val model = TermsViewModel(SavedStateHandle(), repository).also { store.put("terms", it) }
        var opened: Long? = null
        compose.setContent { MangroTheme { TermsRoute({}, { opened = it }, {}, model) } }
        compose.onNodeWithText("전체동의").performClick()
        compose.onAllNodes(isToggleable()).assertCountEquals(3)
        compose.onNodeWithText("확인하기").assertIsEnabled()
        compose.onNodeWithText("개인정보처리방침").performClick()
        compose.runOnIdle { assertEquals(3L, opened) }
    }

    @Test fun missingDocumentOffersListRefresh() {
        val model = TermDetailViewModel(SavedStateHandle(mapOf("documentId" to 3L)), repository).also { store.put("detail", it) }
        var refreshed = false
        compose.setContent { MangroTheme { TermDetailRoute({}, { refreshed = true }, model) } }
        compose.onNodeWithText("현재 제공되지 않는 약관이에요").assertIsDisplayed()
        compose.onNodeWithText("약관 목록 새로고침").performClick()
        compose.runOnIdle { assertTrue(refreshed) }
    }

    @Test fun markdownBodyRendersInWebViewWithoutJavaScript() {
        detailResult = TermsResult.Success(OwnerTermDetail(documents.last(), "# 약관 본문\n\n**개인정보** 안내입니다."))
        val model = TermDetailViewModel(SavedStateHandle(mapOf("documentId" to 3L)), repository).also { store.put("detail", it) }
        compose.setContent { MangroTheme { TermDetailRoute({}, {}, model) } }
        compose.onNodeWithText("개인정보처리방침").assertIsDisplayed()
        lateinit var webView: WebView
        compose.waitUntil(10_000) {
            compose.runOnIdle {
                val found = findWebView(compose.activity.window.decorView)
                if (found != null) webView = found
                found != null && found.progress == 100 && found.contentHeight > 0
            }
        }
        val body = AtomicReference<String?>(null)
        compose.runOnIdle {
            assertFalse(webView.settings.javaScriptEnabled)
            assertFalse(webView.settings.allowFileAccess)
            webView.evaluateJavascript("document.body.innerText") { body.set(it) }
        }
        compose.waitUntil(5_000) { body.get() != null }
        assertTrue(requireNotNull(body.get()).contains("약관 본문"))
        assertFalse(requireNotNull(body.get()).contains("**"))
    }

    private fun findWebView(view: View): WebView? {
        if (view is WebView) return view
        if (view is ViewGroup) for (index in 0 until view.childCount) findWebView(view.getChildAt(index))?.let { return it }
        return null
    }
}
