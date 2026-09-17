package com.swyp.mangro.feature.owner.setting

import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.lifecycle.SavedStateHandle
import androidx.test.platform.app.InstrumentationRegistry
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.core.designsystem.theme.OwnerMangroTypography
import com.swyp.mangro.data.auth.model.AuthFailure
import com.swyp.mangro.data.auth.model.AuthResult
import com.swyp.mangro.data.auth.model.LoginStatus
import com.swyp.mangro.data.auth.model.SignupConsents
import com.swyp.mangro.data.auth.model.TermsDocument
import com.swyp.mangro.data.auth.model.TermsKind
import com.swyp.mangro.data.auth.repository.AuthRepository
import com.swyp.mangro.data.auth.repository.TermsRepository
import com.swyp.mangro.data.owner.store.model.OwnerStore
import com.swyp.mangro.data.owner.store.model.StoreApprovalStatus
import com.swyp.mangro.data.owner.store.model.StoreRegistration
import com.swyp.mangro.data.owner.store.repository.StoreRepository
import com.swyp.mangro.feature.owner.setting.model.OwnerPolicy
import com.swyp.mangro.feature.owner.setting.screen.OwnerSettingRoute
import com.swyp.mangro.feature.owner.setting.screen.OwnerSettingViewModel
import com.swyp.mangro.feature.owner.setting.screen.policy.OwnerPolicyRoute
import com.swyp.mangro.feature.owner.setting.screen.policy.OwnerPolicyViewModel
import java.io.File
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class OwnerSettingTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private fun waitFor(text: String) {
        compose.waitUntil(5000) { compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty() }
    }

    @Test fun storeAndLogoutUseRepositories() {
        var loggedOut = false
        var logoutCalls = 0
        val stores = object : StoreRepository {
            override fun fetchMyStore() = flowOf(Result.success(OwnerStore(1, "서버 상점", emptyList(), StoreApprovalStatus.APPROVED, "09:00", "20:00", "0212345678")))
            override fun register(registration: StoreRegistration): Flow<Result<Unit>> = error("unused")
        }
        val auth = object : AuthRepository {
            override fun logout(): Flow<AuthResult<Unit>> {
                logoutCalls++
                return flowOf(AuthResult.Success(Unit))
            }
            override fun hasSession(): Flow<Boolean> = error("unused")
            override fun login(kakaoAccessToken: String): Flow<AuthResult<LoginStatus>> = error("unused")
            override fun signup(consents: SignupConsents): Flow<AuthResult<Unit>> = error("unused")
        }
        lateinit var vm: OwnerSettingViewModel
        compose.runOnUiThread { vm = OwnerSettingViewModel(stores, auth) }
        compose.setContent { MangroTheme(typography = OwnerMangroTypography) { OwnerSettingRoute({ loggedOut = true }, {}, {}, {}, vm) } }
        waitFor("서버 상점")
        compose.onNodeWithText("0212345678").assertIsDisplayed()
        compose.onNodeWithText("로그아웃").performScrollTo()
        compose.waitForIdle()
        val output = InstrumentationRegistry.getArguments().getString("additionalTestOutputDir")
        val directory = File(output ?: compose.activity.filesDir.path).apply { mkdirs() }
        File(directory, "owner-setting.png").outputStream().use {
            InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot().compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        compose.onNodeWithText("로그아웃").performClick()
        waitFor("로그아웃 하시겠습니까?")
        compose.runOnIdle { assertEquals(0, logoutCalls) }
        compose.onNodeWithText("취소").performClick()
        compose.runOnIdle { assertEquals(0, logoutCalls) }
        compose.onNodeWithText("로그아웃").performClick()
        compose.onNode(hasText("로그아웃") and hasAnyAncestor(isDialog())).performClick()
        compose.waitUntil { loggedOut }
        compose.runOnIdle { assertEquals(1, logoutCalls) }
    }

    @Test fun policyFailureShowsRetryAndRecovers() {
        var failed = true
        val document = TermsDocument(7, TermsKind.SERVICE, "문서", 2, true, "재시도한 약관 본문")
        val repository = object : TermsRepository {
            override fun fetchTermsDocuments(): Flow<AuthResult<List<TermsDocument>>> = flowOf(if (failed) AuthResult.Failure(AuthFailure.NETWORK) else AuthResult.Success(listOf(document)))
            override fun fetchTermsDocument(id: Long) = flowOf(AuthResult.Success(document))
        }
        lateinit var vm: OwnerPolicyViewModel
        compose.runOnUiThread { vm = OwnerPolicyViewModel(SavedStateHandle(mapOf("policy" to OwnerPolicy.TERMS_OF_SERVICE)), repository) }
        compose.setContent { MangroTheme(typography = OwnerMangroTypography) { OwnerPolicyRoute({}, vm) } }
        waitFor("약관을 불러오지 못했습니다. 다시 시도해 주세요.")
        compose.runOnIdle { failed = false }
        compose.onNodeWithText("다시 시도").performClick()
        waitFor("재시도한 약관 본문")
        compose.onNodeWithText("재시도한 약관 본문").assertIsDisplayed()
    }

    @Test fun serviceTermsDisplayServerMarkdown() = policy(OwnerPolicy.TERMS_OF_SERVICE, TermsKind.SERVICE)

    @Test fun privacyPolicyDisplaysItsOwnDocument() = policy(OwnerPolicy.PRIVACY_POLICY, TermsKind.PRIVACY_POLICY)
    private fun policy(policy: OwnerPolicy, kind: TermsKind) {
        var requestedId = 0L
        val document = TermsDocument(7, kind, "문서", 2, true, "서버 약관 본문")
        val repository = object : TermsRepository {
            override fun fetchTermsDocuments() = flowOf(AuthResult.Success(listOf(document.copy(content = ""))))
            override fun fetchTermsDocument(id: Long): Flow<AuthResult<TermsDocument>> {
                requestedId = id
                return flowOf(AuthResult.Success(document))
            }
        }
        lateinit var vm: OwnerPolicyViewModel
        compose.runOnUiThread { vm = OwnerPolicyViewModel(SavedStateHandle(mapOf("policy" to policy)), repository) }
        compose.setContent { MangroTheme(typography = OwnerMangroTypography) { OwnerPolicyRoute({}, vm) } }
        waitFor("서버 약관 본문")
        compose.onNodeWithText("서버 약관 본문").assertIsDisplayed()
        compose.runOnIdle {
            assertEquals(7L, requestedId)
            assertTrue(!vm.uiState.value.hasError)
        }
    }
}
