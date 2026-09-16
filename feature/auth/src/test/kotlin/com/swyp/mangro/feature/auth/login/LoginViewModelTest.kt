package com.swyp.mangro.feature.auth.login

import com.swyp.mangro.data.auth.model.AuthFailure
import com.swyp.mangro.data.auth.model.AuthResult
import com.swyp.mangro.data.auth.model.LoginStatus
import com.swyp.mangro.data.auth.model.SignupConsents
import com.swyp.mangro.data.auth.repository.AuthRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {
    private val repository = object : AuthRepository {
        override fun logout(): kotlinx.coroutines.flow.Flow<com.swyp.mangro.data.auth.model.AuthResult<Unit>> = error("unused")
        override fun hasSession(): Flow<Boolean> = flowOf(false)
        override fun login(kakaoAccessToken: String): Flow<AuthResult<LoginStatus>> = flowOf(AuthResult.Success(LoginStatus.SIGNUP_REQUIRED))
        override fun signup(consents: SignupConsents): Flow<AuthResult<Unit>> = flowOf(AuthResult.Success(Unit))
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun repeatedClickLaunchesOnlyOnceAndSuccessNavigatesAfterAuthentication() = runTest {
        val viewModel = LoginViewModel(repository, KakaoLoginLauncher())
        val events = mutableListOf<LoginUiEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.event.collect { events.add(it) }
        }
        viewModel.handleAction(LoginUiAction.KakaoLoginClicked)
        viewModel.handleAction(LoginUiAction.KakaoLoginClicked)
        assertTrue(viewModel.uiState.value.isLoading)
        assertEquals(listOf(LoginUiEvent.LaunchKakaoLogin), events)

        viewModel.handleAction(LoginUiAction.KakaoLoginCompleted(KakaoLoginResult.Success, "kakao-raw"))
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(
            listOf(
                LoginUiEvent.LaunchKakaoLogin,
                LoginUiEvent.NavigateToTerms,
            ),
            events,
        )
    }

    @Test
    fun unsuccessfulResultsStayOnLoginAndAllowRetry() = runTest {
        for (result in listOf(KakaoLoginResult.Failed, KakaoLoginResult.Cancelled, KakaoLoginResult.NotConfigured)) {
            val viewModel = LoginViewModel(repository, KakaoLoginLauncher())
            val events = mutableListOf<LoginUiEvent>()
            val collection = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.event.collect { events.add(it) }
            }
            viewModel.handleAction(LoginUiAction.KakaoLoginClicked)
            viewModel.handleAction(LoginUiAction.KakaoLoginCompleted(result))
            assertFalse(viewModel.uiState.value.isLoading)
            assertEquals(listOf(LoginUiEvent.LaunchKakaoLogin, LoginUiEvent.ShowKakaoLoginResult(result)), events)
            viewModel.handleAction(LoginUiAction.KakaoLoginClicked)
            assertTrue(viewModel.uiState.value.isLoading)
            assertEquals(LoginUiEvent.LaunchKakaoLogin, events.last())
            collection.cancel()
        }
    }

    @Test
    fun serverVerificationKeepsLoadingAndIgnoresDuplicateCallback() = runTest {
        val pending = CompletableDeferred<AuthResult<LoginStatus>>()
        var calls = 0
        val repository = object : AuthRepository {
            override fun logout(): kotlinx.coroutines.flow.Flow<com.swyp.mangro.data.auth.model.AuthResult<Unit>> = error("unused")
            override fun hasSession(): Flow<Boolean> = flowOf(false)
            override fun login(kakaoAccessToken: String): Flow<AuthResult<LoginStatus>> = flow {
                assertEquals("kakao-raw", kakaoAccessToken)
                calls++
                emit(pending.await())
            }
            override fun signup(consents: SignupConsents): Flow<AuthResult<Unit>> = error("unused")
        }
        val viewModel = LoginViewModel(repository, KakaoLoginLauncher())
        val events = mutableListOf<LoginUiEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.event.collect { events.add(it) } }
        viewModel.handleAction(LoginUiAction.KakaoLoginClicked)
        repeat(2) { viewModel.handleAction(LoginUiAction.KakaoLoginCompleted(KakaoLoginResult.Success, "kakao-raw")) }
        assertTrue(viewModel.uiState.value.isLoading)
        assertEquals(1, calls)
        assertEquals(listOf(LoginUiEvent.LaunchKakaoLogin), events)
        pending.complete(AuthResult.Success(LoginStatus.AUTHENTICATED))
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(LoginUiEvent.NavigateToHome, events.last())
    }

    @Test
    fun serverRejectionDoesNotNavigateAfterSdkSuccess() = runTest {
        val repository = object : AuthRepository {
            override fun logout(): kotlinx.coroutines.flow.Flow<com.swyp.mangro.data.auth.model.AuthResult<Unit>> = error("unused")
            override fun hasSession(): Flow<Boolean> = flowOf(false)
            override fun login(kakaoAccessToken: String): Flow<AuthResult<LoginStatus>> = flowOf(AuthResult.Failure(AuthFailure.INVALID_OAUTH_TOKEN))
            override fun signup(consents: SignupConsents): Flow<AuthResult<Unit>> = error("unused")
        }
        val viewModel = LoginViewModel(repository, KakaoLoginLauncher())
        val events = mutableListOf<LoginUiEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.event.collect { events.add(it) } }
        viewModel.handleAction(LoginUiAction.KakaoLoginClicked)
        viewModel.handleAction(LoginUiAction.KakaoLoginCompleted(KakaoLoginResult.Success, "bad"))
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(listOf(LoginUiEvent.LaunchKakaoLogin, LoginUiEvent.ShowAuthFailure(AuthFailure.INVALID_OAUTH_TOKEN)), events)
    }
}
