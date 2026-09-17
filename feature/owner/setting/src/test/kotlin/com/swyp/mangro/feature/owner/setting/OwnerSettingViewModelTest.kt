package com.swyp.mangro.feature.owner.setting

import com.swyp.mangro.data.auth.model.AuthFailure
import com.swyp.mangro.data.auth.model.AuthResult
import com.swyp.mangro.data.auth.model.LoginStatus
import com.swyp.mangro.data.auth.model.SignupConsents
import com.swyp.mangro.data.auth.repository.AuthRepository
import com.swyp.mangro.data.owner.store.model.OwnerStore
import com.swyp.mangro.data.owner.store.model.StoreApprovalStatus
import com.swyp.mangro.data.owner.store.model.StoreRegistration
import com.swyp.mangro.data.owner.store.repository.StoreRepository
import com.swyp.mangro.feature.owner.setting.screen.OwnerSettingAction
import com.swyp.mangro.feature.owner.setting.screen.OwnerSettingEvent
import com.swyp.mangro.feature.owner.setting.screen.OwnerSettingViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
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
class OwnerSettingViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before fun setup() = Dispatchers.setMain(dispatcher)

    @After fun teardown() = Dispatchers.resetMain()
    private var storeResult: Result<OwnerStore> = Result.success(OwnerStore(1, "실제 상점", emptyList(), StoreApprovalStatus.PENDING, "09:00", "20:00", "0212345678"))
    private val stores = object : StoreRepository {
        override fun fetchMyStore() = flowOf(storeResult)
        override fun register(registration: StoreRegistration): Flow<Result<Unit>> = error("unused")
    }
    private var calls = 0
    private var logout = CompletableDeferred<AuthResult<Unit>>()
    private val auth = object : AuthRepository {
        override fun logout() = flow {
            calls++
            emit(logout.await())
        }
        override fun hasSession(): Flow<Boolean> = error("unused")
        override fun login(kakaoAccessToken: String): Flow<AuthResult<LoginStatus>> = error("unused")
        override fun signup(consents: SignupConsents): Flow<AuthResult<Unit>> = error("unused")
    }

    @Test fun storeLoadsAndFailureCanRetry() = runTest(dispatcher) {
        val vm = OwnerSettingViewModel(stores, auth)
        storeResult = Result.failure(IllegalStateException())
        vm.refresh()
        advanceUntilIdle()
        assertTrue(vm.uiState.value.hasStoreError)
        storeResult = Result.success(OwnerStore(1, "실제 상점", emptyList(), StoreApprovalStatus.PENDING, "09:00", "20:00", "0212345678"))
        vm.refresh()
        advanceUntilIdle()
        assertFalse(vm.uiState.value.hasStoreError)
        assertEquals("실제 상점", vm.uiState.value.storeName)
        assertEquals("0212345678", vm.uiState.value.storePhone)
    }

    @Test fun logoutRequiresConfirmationAndDismissDoesNotLogout() = runTest(dispatcher) {
        val vm = OwnerSettingViewModel(stores, auth)
        vm.handleAction(OwnerSettingAction.LogoutConfirmed)
        vm.handleAction(OwnerSettingAction.LogoutClicked)
        advanceUntilIdle()
        assertTrue(vm.uiState.value.showLogoutConfirmation)
        assertEquals(0, calls)
        vm.handleAction(OwnerSettingAction.LogoutDismissed)
        vm.handleAction(OwnerSettingAction.LogoutConfirmed)
        advanceUntilIdle()
        assertFalse(vm.uiState.value.showLogoutConfirmation)
        assertEquals(0, calls)
    }

    @Test fun logoutDeduplicatesAndNavigatesOnlyAfterSuccess() = runTest(dispatcher) {
        val vm = OwnerSettingViewModel(stores, auth)
        val event = backgroundScope.async { vm.event.first() }
        vm.handleAction(OwnerSettingAction.LogoutClicked)
        vm.handleAction(OwnerSettingAction.LogoutConfirmed)
        vm.handleAction(OwnerSettingAction.LogoutClicked)
        vm.handleAction(OwnerSettingAction.LogoutConfirmed)
        advanceUntilIdle()
        assertEquals(1, calls)
        assertFalse(event.isCompleted)
        logout.complete(AuthResult.Success(Unit))
        advanceUntilIdle()
        assertEquals(OwnerSettingEvent.NavigateToLogin, event.await())
    }

    @Test fun logoutFailureAllowsRetry() = runTest(dispatcher) {
        val vm = OwnerSettingViewModel(stores, auth)
        logout.complete(AuthResult.Failure(AuthFailure.NETWORK))
        vm.handleAction(OwnerSettingAction.LogoutClicked)
        vm.handleAction(OwnerSettingAction.LogoutConfirmed)
        advanceUntilIdle()
        assertTrue(vm.uiState.value.hasLogoutError)
        assertFalse(vm.uiState.value.isLoggingOut)
        vm.handleAction(OwnerSettingAction.LogoutErrorDismissed)
        assertFalse(vm.uiState.value.hasLogoutError)
        logout = CompletableDeferred(AuthResult.Success(Unit))
        vm.handleAction(OwnerSettingAction.LogoutClicked)
        vm.handleAction(OwnerSettingAction.LogoutConfirmed)
        advanceUntilIdle()
        assertEquals(2, calls)
        assertEquals(OwnerSettingEvent.NavigateToLogin, vm.event.first())
    }
}
