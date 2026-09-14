package com.swyp.mangro.feature.splash

import androidx.lifecycle.ViewModelStore
import com.swyp.mangro.data.owner.auth.AuthFailure
import com.swyp.mangro.data.owner.auth.AuthResult
import com.swyp.mangro.data.owner.auth.OwnerSession
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SplashViewModelTest {
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

    @Test fun offlineRestorationStaysOnSplashAndRetryNavigatesAfterSuccess() = runTest {
        auth.restoreResult = AuthResult.Failure(AuthFailure.NETWORK)
        val model = SplashViewModel(auth).also { store.put("splash", it) }
        val events = mutableListOf<SplashUiEvent>()
        backgroundScope.launch { model.event.collect { events.add(it) } }
        runCurrent()
        assertTrue(events.isEmpty())
        assertEquals(SplashUiState(isLoading = false, hasError = true), model.uiState.value)
        auth.restoreResult = AuthResult.Success(OwnerSession.AUTHENTICATED)
        model.handleAction(SplashUiAction.RetryClicked)
        runCurrent()
        assertEquals(listOf(SplashUiEvent.NavigateToHome), events)
    }

    @Test fun expiredRefreshRoutesToLogin() = runTest {
        auth.restoreResult = AuthResult.Failure(AuthFailure.UNAUTHORIZED)
        val model = SplashViewModel(auth).also { store.put("splash", it) }
        val events = mutableListOf<SplashUiEvent>()
        backgroundScope.launch { model.event.collect { events.add(it) } }
        runCurrent()
        assertEquals(listOf(SplashUiEvent.NavigateToLogin), events)
    }
}
