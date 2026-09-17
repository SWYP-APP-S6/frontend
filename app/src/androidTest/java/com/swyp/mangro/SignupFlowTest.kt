package com.swyp.mangro

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.data.auth.model.AuthFailure
import com.swyp.mangro.data.auth.model.AuthResult
import com.swyp.mangro.data.auth.model.LoginStatus
import com.swyp.mangro.data.auth.model.SignupConsents
import com.swyp.mangro.data.auth.model.TermsDocument
import com.swyp.mangro.data.auth.model.TermsKind
import com.swyp.mangro.data.auth.repository.AuthRepository
import com.swyp.mangro.data.auth.repository.TermsRepository
import com.swyp.mangro.feature.auth.terms.TermsRoute
import com.swyp.mangro.feature.auth.terms.TermsViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

class SignupFlowTest {
    @get:Rule val compose = createComposeRule()

    private val terms = object : TermsRepository {
        override fun fetchTermsDocuments() = flowOf(
            AuthResult.Success(
                TermsKind.entries.filter { it != TermsKind.PRIVACY_POLICY }.mapIndexed { index, kind ->
                    TermsDocument(index + 1L, kind, kind.name, 1, kind != TermsKind.MARKETING)
                },
            ),
        )
        override fun fetchTermsDocument(id: Long): Flow<AuthResult<TermsDocument>> = error("unused")
    }

    @Test fun consentAndSuccessfulSignupAreRequiredBeforeCompletion() {
        org.junit.Assume.assumeTrue(BuildConfig.FLAVOR == "consumer")
        val pending = CompletableDeferred<AuthResult<Unit>>()
        var completed = false
        var calls = 0
        val repository = object : AuthRepository {
            override fun logout(): kotlinx.coroutines.flow.Flow<com.swyp.mangro.data.auth.model.AuthResult<Unit>> = error("unused")
            override fun hasSession() = flowOf(false)
            override fun login(kakaoAccessToken: String): Flow<AuthResult<LoginStatus>> = error("unused")
            override fun signup(consents: SignupConsents): Flow<AuthResult<Unit>> = flow {
                calls++
                assertEquals(SignupConsents(true, true, true, true, true), consents)
                emit(pending.await())
            }
        }
        lateinit var model: TermsViewModel
        compose.runOnUiThread { model = TermsViewModel(repository, terms) }
        compose.setContent {
            MangroTheme { TermsRoute(onBackClick = {}, onSignupCompleted = { completed = true }, navigateToLogin = {}, navigateToTermsDetail = {}, viewModel = model) }
        }
        compose.onNodeWithText("확인하기").assertIsNotEnabled()
        compose.onNodeWithText("전체동의").performClick()
        compose.onNodeWithText("확인하기").assertIsEnabled().performClick()
        compose.onNodeWithText("확인하기").assertIsNotEnabled()
        compose.runOnIdle {
            assertFalse(completed)
            assertEquals(1, calls)
            pending.complete(AuthResult.Success(Unit))
        }
        compose.waitUntil(5_000) { completed }
    }

    @Test fun expiredSignupSessionReturnsToLogin() {
        org.junit.Assume.assumeTrue(BuildConfig.FLAVOR == "consumer")
        var login = false
        var completed = false
        val repository = object : AuthRepository {
            override fun logout(): kotlinx.coroutines.flow.Flow<com.swyp.mangro.data.auth.model.AuthResult<Unit>> = error("unused")
            override fun hasSession() = flowOf(false)
            override fun login(kakaoAccessToken: String): Flow<AuthResult<LoginStatus>> = error("unused")
            override fun signup(consents: SignupConsents): Flow<AuthResult<Unit>> = flowOf(AuthResult.Failure(AuthFailure.SIGNUP_REQUIRED))
        }
        lateinit var model: TermsViewModel
        compose.runOnUiThread { model = TermsViewModel(repository, terms) }
        compose.setContent { MangroTheme { TermsRoute(onBackClick = {}, onSignupCompleted = { completed = true }, navigateToLogin = { login = true }, navigateToTermsDetail = {}, viewModel = model) } }
        compose.onNodeWithText("전체동의").performClick()
        compose.onNodeWithText("확인하기").performClick()
        compose.waitUntil(5_000) { login }
        compose.runOnIdle { assertFalse(completed) }
    }

    @Test fun ownerConsentOpensOnboardingWithoutCallingSignup() {
        org.junit.Assume.assumeTrue(BuildConfig.FLAVOR == "owner")
        var completed = false
        var received: SignupConsents? = null
        val repository = object : AuthRepository {
            override fun logout(): kotlinx.coroutines.flow.Flow<com.swyp.mangro.data.auth.model.AuthResult<Unit>> = error("unused")
            override fun hasSession() = flowOf(false)
            override fun login(kakaoAccessToken: String): Flow<AuthResult<LoginStatus>> = error("unused")
            override fun signup(consents: SignupConsents): Flow<AuthResult<Unit>> = error("Owner must enter store information first")
        }
        lateinit var model: TermsViewModel
        compose.runOnUiThread { model = TermsViewModel(repository, terms) }
        compose.setContent {
            MangroTheme { TermsRoute(onBackClick = {}, onSignupCompleted = { completed = true }, navigateToLogin = {}, navigateToTermsDetail = {}, viewModel = model, onOwnerOnboarding = { received = it }) }
        }
        compose.onNodeWithText("확인하기").assertIsNotEnabled()
        compose.onNodeWithText("전체동의").performClick()
        compose.onNodeWithText("확인하기").performClick()
        compose.waitUntil(5_000) { received != null }
        compose.runOnIdle {
            assertEquals(SignupConsents(true, true, true, true, true), received)
            assertFalse(completed)
        }
    }
}
