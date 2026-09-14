package com.swyp.mangro.feature.auth.login

import androidx.lifecycle.ViewModelStore
import com.swyp.mangro.data.owner.auth.AuthFailure
import com.swyp.mangro.data.owner.auth.AuthResult
import com.swyp.mangro.data.owner.auth.OwnerSession
import com.swyp.mangro.feature.auth.terms.FakeOwnerAuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val store = ViewModelStore()
    private val auth = FakeOwnerAuthRepository()

    @Before fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @After fun teardown() {
        store.clear()
        Dispatchers.resetMain()
    }
    private fun model() = LoginViewModel(auth).also { store.put("login", it) }

    @Test fun newUserGoesToTermsAndDuplicateClickDoesNotLaunchSdkAgain() = runTest {
        val model = model()
        val events = mutableListOf<LoginUiEvent>()
        backgroundScope.launch { model.event.collect { events.add(it) } }
        model.handleAction(LoginUiAction.KakaoLoginClicked)
        model.handleAction(LoginUiAction.KakaoLoginClicked)
        runCurrent()
        assertEquals(listOf(LoginUiEvent.LaunchKakaoLogin), events)
        model.handleAction(LoginUiAction.KakaoTokenReceived("sdk-token"))
        runCurrent()
        assertEquals(LoginUiEvent.NavigateToTerms, events.last())
        assertFalse(model.uiState.value.isLoading)
    }

    @Test fun existingMemberSkipsTerms() = runTest {
        auth.loginResult = AuthResult.Success(OwnerSession.AUTHENTICATED)
        val model = model()
        val events = mutableListOf<LoginUiEvent>()
        backgroundScope.launch { model.event.collect { events.add(it) } }
        model.handleAction(LoginUiAction.KakaoLoginClicked)
        model.handleAction(LoginUiAction.KakaoTokenReceived("sdk-token"))
        runCurrent()
        assertEquals(LoginUiEvent.NavigateToHome, events.last())
    }

    @Test fun cancellationIsQuietAndServerFailureShowsRetryableError() = runTest {
        val model = model()
        model.handleAction(LoginUiAction.KakaoLoginClicked)
        model.handleAction(LoginUiAction.KakaoLoginFailed(cancelled = true))
        assertEquals(LoginUiState(), model.uiState.value)
        auth.loginResult = AuthResult.Failure(AuthFailure.NETWORK)
        model.handleAction(LoginUiAction.KakaoLoginClicked)
        model.handleAction(LoginUiAction.KakaoTokenReceived("sdk-token"))
        runCurrent()
        assertEquals(LoginUiState(hasError = true), model.uiState.value)
    }
}
