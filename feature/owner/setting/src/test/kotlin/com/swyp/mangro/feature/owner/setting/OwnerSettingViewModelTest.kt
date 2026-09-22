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
import com.swyp.mangro.data.user.repository.UserRepository
import com.swyp.mangro.feature.owner.setting.model.SettingsMenu
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
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OwnerSettingViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before fun setup() = Dispatchers.setMain(dispatcher)

    @After fun teardown() = Dispatchers.resetMain()

    private val stores = object : StoreRepository {
        override fun fetchMyStore() = flowOf(
            Result.success(
                OwnerStore(1, "실제 상점", emptyList(), StoreApprovalStatus.PENDING, "09:00", "20:00", "0212345678"),
            ),
        )

        override fun register(registration: StoreRegistration): Flow<Result<Unit>> = error("unused")
    }

    private var logoutCalls = 0
    private var logout = CompletableDeferred<AuthResult<Unit>>()
    private val auth = object : AuthRepository {
        override fun logout() = flow {
            logoutCalls++
            emit(logout.await())
        }

        override fun hasSession(): Flow<Boolean> = error("unused")
        override fun login(kakaoAccessToken: String): Flow<AuthResult<LoginStatus>> = error("unused")
        override fun signup(consents: SignupConsents): Flow<AuthResult<Unit>> = error("unused")
        override fun guestLogin(): Flow<AuthResult<Unit>> = error("unused")
        override fun isGuestSession(): Flow<Boolean> = error("unused")
    }

    private var withdrawCalls = 0
    private var withdraw = CompletableDeferred<Result<Unit>>()
    private val users = object : UserRepository {
        override fun fetchMe() = error("unused")

        override fun withdrawUser() = flow {
            withdrawCalls++
            withdraw.await().getOrThrow()
            emit(Unit)
        }
    }

    private fun viewModel() = OwnerSettingViewModel(stores, auth, users)

    @Test fun storeLoadsOnInitialization() = runTest(dispatcher) {
        val vm = viewModel()

        advanceUntilIdle()

        assertFalse(vm.uiState.value.hasStoreError)
        assertEquals("실제 상점", vm.uiState.value.storeName)
        assertEquals("0212345678", vm.uiState.value.storePhone)
    }

    @Test fun logoutMenuRequestsConfirmation() = runTest(dispatcher) {
        val vm = viewModel()

        vm.handleAction(OwnerSettingAction.SettingsMenuClicked(SettingsMenu.LOGOUT))

        assertEquals(OwnerSettingEvent.ShowLogoutConfirmDialog, vm.event.first())
        assertEquals(0, logoutCalls)
    }

    @Test fun logoutDeduplicatesAndNavigatesOnlyAfterSuccess() = runTest(dispatcher) {
        val vm = viewModel()
        val event = backgroundScope.async { vm.event.first() }

        vm.handleAction(OwnerSettingAction.LogoutConfirmed)
        vm.handleAction(OwnerSettingAction.LogoutConfirmed)
        advanceUntilIdle()

        assertEquals(1, logoutCalls)
        assertFalse(event.isCompleted)

        logout.complete(AuthResult.Success(Unit))
        advanceUntilIdle()

        assertEquals(OwnerSettingEvent.NavigateToLogin, event.await())
    }

    @Test fun logoutFailureShowsErrorAndAllowsRetry() = runTest(dispatcher) {
        val vm = viewModel()
        logout.complete(AuthResult.Failure(AuthFailure.NETWORK))

        vm.handleAction(OwnerSettingAction.LogoutConfirmed)
        advanceUntilIdle()

        assertEquals(OwnerSettingEvent.ShowLogoutErrorDialog, vm.event.first())
        assertFalse(vm.uiState.value.isLoggingOut)

        logout = CompletableDeferred(AuthResult.Success(Unit))
        vm.handleAction(OwnerSettingAction.LogoutConfirmed)
        advanceUntilIdle()

        assertEquals(2, logoutCalls)
        assertEquals(OwnerSettingEvent.NavigateToLogin, vm.event.first())
    }

    @Test fun withdrawMenuRequestsConfirmationAndSuccessNavigatesToLogin() = runTest(dispatcher) {
        val vm = viewModel()

        vm.handleAction(OwnerSettingAction.SettingsMenuClicked(SettingsMenu.WITHDRAW))
        assertEquals(OwnerSettingEvent.ShowWithdrawConfirmDialog, vm.event.first())
        assertEquals(0, withdrawCalls)

        val event = backgroundScope.async { vm.event.first() }
        vm.handleAction(OwnerSettingAction.WithdrawConfirmed)
        advanceUntilIdle()
        assertEquals(1, withdrawCalls)
        assertFalse(event.isCompleted)

        withdraw.complete(Result.success(Unit))
        advanceUntilIdle()

        assertEquals(OwnerSettingEvent.NavigateToLogin, event.await())
    }

    @Test fun withdrawFailureShowsError() = runTest(dispatcher) {
        val vm = viewModel()
        withdraw.complete(Result.failure(IllegalStateException("withdraw failed")))

        vm.handleAction(OwnerSettingAction.WithdrawConfirmed)
        advanceUntilIdle()

        assertEquals(1, withdrawCalls)
        assertEquals(OwnerSettingEvent.ShowWithdrawErrorDialog, vm.event.first())
    }
}
