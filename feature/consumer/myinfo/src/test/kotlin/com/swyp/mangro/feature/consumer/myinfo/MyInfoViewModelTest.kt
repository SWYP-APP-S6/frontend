package com.swyp.mangro.feature.consumer.myinfo

import com.swyp.mangro.data.auth.model.AuthFailure
import com.swyp.mangro.data.auth.model.AuthResult
import com.swyp.mangro.data.auth.model.LoginStatus
import com.swyp.mangro.data.auth.model.SignupConsents
import com.swyp.mangro.data.auth.repository.AuthRepository
import com.swyp.mangro.data.user.model.UserProfile
import com.swyp.mangro.data.user.repository.UserRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
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
class MyInfoViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private var hasSession = false
    private var profileCalls = 0
    private var logoutCalls = 0
    private var profile = Result.success(UserProfile(1, "CONSUMER", "망그로", null, false, "", ""))
    private var logoutResult = CompletableDeferred<AuthResult<Unit>>()
    private val auth = object : AuthRepository {
        override fun hasSession() = flowOf(hasSession)
        override fun logout() = flow {
            logoutCalls++
            emit(logoutResult.await())
        }
        override fun login(kakaoAccessToken: String): Flow<AuthResult<LoginStatus>> = error("unused")
        override fun signup(consents: SignupConsents): Flow<AuthResult<Unit>> = error("unused")
    }
    private val users = object : UserRepository {
        override fun fetchMe() = flow {
            profileCalls++
            emit(profile)
        }
    }

    @Before fun setup() = Dispatchers.setMain(dispatcher)

    @After fun teardown() = Dispatchers.resetMain()

    @Test fun guestSkipsProfileAndCanLinkKakaoWithoutLogout() = runTest(dispatcher) {
        val vm = MyInfoViewModel(auth, users)
        advanceUntilIdle()
        assertTrue(vm.uiState.value.isGuest)
        assertFalse(vm.uiState.value.isLoading)
        assertEquals(0, profileCalls)
        vm.handleAction(MyInfoUiAction.LogoutClicked)
        vm.handleAction(MyInfoUiAction.LogoutConfirmed)
        vm.handleAction(MyInfoUiAction.LinkKakaoClicked)
        assertEquals(MyInfoUiEvent.NavigateToLogin, vm.event.first())
        assertFalse(vm.uiState.value.showLogoutConfirmation)
        assertEquals(0, logoutCalls)
    }

    @Test fun profileFailureDoesNotTurnMemberIntoGuestAndCanRetry() = runTest(dispatcher) {
        hasSession = true
        profile = Result.failure(IllegalStateException())
        val vm = MyInfoViewModel(auth, users)
        advanceUntilIdle()
        assertFalse(vm.uiState.value.isGuest)
        assertTrue(vm.uiState.value.hasProfileError)
        profile = Result.success(UserProfile(1, "CONSUMER", "망그로", null, false, "", ""))
        vm.handleAction(MyInfoUiAction.RetryClicked)
        advanceUntilIdle()
        assertFalse(vm.uiState.value.hasProfileError)
        assertEquals("망그로", vm.uiState.value.nickname)
        assertEquals("", vm.uiState.value.phone)
    }

    @Test fun logoutRequiresConfirmationAndPreventsDuplicateRequests() = runTest(dispatcher) {
        hasSession = true
        val vm = MyInfoViewModel(auth, users)
        advanceUntilIdle()
        vm.handleAction(MyInfoUiAction.LogoutConfirmed)
        vm.handleAction(MyInfoUiAction.LogoutClicked)
        vm.handleAction(MyInfoUiAction.LogoutDismissed)
        vm.handleAction(MyInfoUiAction.LogoutConfirmed)
        advanceUntilIdle()
        assertEquals(0, logoutCalls)
        vm.handleAction(MyInfoUiAction.LogoutClicked)
        vm.handleAction(MyInfoUiAction.LogoutConfirmed)
        vm.handleAction(MyInfoUiAction.LogoutConfirmed)
        advanceUntilIdle()
        assertEquals(1, logoutCalls)
        assertTrue(vm.uiState.value.isLoggingOut)
        logoutResult.complete(AuthResult.Success(Unit))
        advanceUntilIdle()
        assertEquals(MyInfoUiEvent.NavigateToLogin, vm.event.first())
    }

    @Test fun logoutFailureCanBeDismissedAndRetried() = runTest(dispatcher) {
        hasSession = true
        val vm = MyInfoViewModel(auth, users)
        advanceUntilIdle()
        logoutResult.complete(AuthResult.Failure(AuthFailure.NETWORK))
        vm.handleAction(MyInfoUiAction.LogoutClicked)
        vm.handleAction(MyInfoUiAction.LogoutConfirmed)
        advanceUntilIdle()
        assertTrue(vm.uiState.value.hasLogoutError)
        assertFalse(vm.uiState.value.isLoggingOut)
        vm.handleAction(MyInfoUiAction.LogoutErrorDismissed)
        logoutResult = CompletableDeferred(AuthResult.Success(Unit))
        vm.handleAction(MyInfoUiAction.LogoutClicked)
        vm.handleAction(MyInfoUiAction.LogoutConfirmed)
        advanceUntilIdle()
        assertEquals(2, logoutCalls)
        assertEquals(MyInfoUiEvent.NavigateToLogin, vm.event.first())
    }
}
