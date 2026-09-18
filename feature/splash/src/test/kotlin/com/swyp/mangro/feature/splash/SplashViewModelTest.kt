package com.swyp.mangro.feature.splash

import com.swyp.mangro.data.auth.model.AuthResult
import com.swyp.mangro.data.auth.model.LoginStatus
import com.swyp.mangro.data.auth.model.SignupConsents
import com.swyp.mangro.data.auth.repository.AuthRepository
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SplashViewModelTest {
    @Before fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After fun tearDown() = Dispatchers.resetMain()

    private fun repository(session: Flow<Boolean>) = object : AuthRepository {
        override fun logout(): kotlinx.coroutines.flow.Flow<com.swyp.mangro.data.auth.model.AuthResult<Unit>> = error("unused")
        override fun hasSession() = session
        override fun login(kakaoAccessToken: String): Flow<AuthResult<LoginStatus>> = error("unused")
        override fun signup(consents: SignupConsents): Flow<AuthResult<Unit>> = error("unused")
    }

    @Test fun storedSessionNavigatesHome() = runTest {
        val viewModel = SplashViewModel(repository(flowOf(true)))
        val event = async { viewModel.event.first() }
        assertEquals(SplashUiEvent.NavigateToHome, event.await())
    }

    @Test fun noSessionNavigatesLogin() = runTest {
        val viewModel = SplashViewModel(repository(flowOf(false)))
        val event = async { viewModel.event.first() }
        assertEquals(SplashUiEvent.NavigateToLogin, event.await())
    }

    @Test fun unreadableSessionNavigatesLogin() = runTest {
        val viewModel = SplashViewModel(repository(flow { throw IOException("storage") }))
        val event = async { viewModel.event.first() }
        assertEquals(SplashUiEvent.NavigateToLogin, event.await())
    }
}
