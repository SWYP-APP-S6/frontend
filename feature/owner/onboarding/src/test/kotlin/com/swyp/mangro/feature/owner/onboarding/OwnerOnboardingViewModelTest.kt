package com.swyp.mangro.feature.owner.onboarding

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import com.swyp.mangro.data.auth.model.AuthFailure
import com.swyp.mangro.data.auth.model.AuthResult
import com.swyp.mangro.data.auth.model.LoginStatus
import com.swyp.mangro.data.auth.model.SignupConsents
import com.swyp.mangro.data.auth.repository.AuthRepository
import com.swyp.mangro.data.owner.store.model.StoreRegistration
import com.swyp.mangro.data.owner.store.repository.StoreRepository
import com.swyp.mangro.feature.owner.onboarding.model.StoreAddressModel
import com.swyp.mangro.feature.owner.onboarding.model.StoreBasicInfoModel
import com.swyp.mangro.feature.owner.onboarding.model.StoreCategoryModel
import com.swyp.mangro.feature.owner.onboarding.screen.basic.OwnerBasicInfoAction
import com.swyp.mangro.feature.owner.onboarding.screen.basic.OwnerBasicInfoEvent
import com.swyp.mangro.feature.owner.onboarding.screen.basic.OwnerBasicInfoViewModel
import com.swyp.mangro.feature.owner.onboarding.screen.operating.OwnerOperatingInfoAction
import com.swyp.mangro.feature.owner.onboarding.screen.operating.OwnerOperatingInfoDialog
import com.swyp.mangro.feature.owner.onboarding.screen.operating.OwnerOperatingInfoViewModel
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
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OwnerOnboardingViewModelTest {
    private val consents = SignupConsents(true, true, false, false, false)
    private var signupCalls = 0
    private var signupResult: AuthResult<Unit> = AuthResult.Success(Unit)
    private var pendingSignup: CompletableDeferred<AuthResult<Unit>>? = null
    private val order = mutableListOf<String>()
    private val authRepository = object : AuthRepository {
        override fun hasSession() = flowOf(false)
        override fun login(kakaoAccessToken: String): Flow<AuthResult<LoginStatus>> = error("unused")
        override fun signup(consents: SignupConsents) = flow {
            signupCalls++
            order.add("signup")
            assertEquals(this@OwnerOnboardingViewModelTest.consents, consents)
            emit(pendingSignup?.await() ?: signupResult)
        }
    }
    private var pending: CompletableDeferred<Result<Unit>>? = null
    private var calls = 0
    private var submitted: StoreRegistration? = null
    private val repository = object : StoreRepository {
        override fun register(registration: StoreRegistration) = flow {
            order.add("store")
            calls++
            submitted = registration
            emit(pending?.await() ?: Result.failure(IllegalStateException("server")))
        }
    }
    private val dispatcher = StandardTestDispatcher()
    private val store = ViewModelStore()
    private val category = StoreCategoryModel.options[1]
    private val basicInfo =
        StoreBasicInfoModel("청과마을", category, StoreAddressModel("03965", "서울 마포구 망원로 12"), "1층")

    @Before fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After fun tearDown() {
        store.clear()
        Dispatchers.resetMain()
    }

    private fun operating(handle: SavedStateHandle = SavedStateHandle(mapOf(Constants.BASIC_INFO to basicInfo, Constants.SIGNUP_CONSENTS to consents))): OwnerOperatingInfoViewModel = OwnerOperatingInfoViewModel(
        handle,
        repository,
        authRepository,
    ).also { store.put("operating", it) }

    private fun fill(model: OwnerOperatingInfoViewModel) {
        model.handleAction(OwnerOperatingInfoAction.PhoneNumberChanged("02-1234-5678"))
        model.handleAction(OwnerOperatingInfoAction.OpeningTimeSelected(540))
        model.handleAction(OwnerOperatingInfoAction.ClosingTimeSelected(1200))
        model.handleAction(OwnerOperatingInfoAction.BusinessDayClicked(1))
    }

    @Test fun fixedCategoryCanBeSelectedWithoutInitializationAndUnknownCategoryIsRejected() = runTest {
        val model = OwnerBasicInfoViewModel(SavedStateHandle()).also { store.put("basic", it) }
        val before = model.uiState.value
        model.handleAction(OwnerBasicInfoAction.CategorySelected(category))
        assertEquals(category, model.uiState.value.category)
        model.handleAction(OwnerBasicInfoAction.CategorySelected(StoreCategoryModel("unknown", "미등록")))
        model.handleAction(OwnerBasicInfoAction.NameChanged("가게"))
        assertEquals("", before.name)
        assertEquals("가게", model.uiState.value.name)
        assertEquals(category, model.uiState.value.category)
    }

    @Test fun invalidBasicInfoCannotNavigateAndInputRestores() = runTest {
        val handle = SavedStateHandle()
        val model = OwnerBasicInfoViewModel(handle).also { store.put("basic", it) }
        val event = async { model.event.first() }
        model.handleAction(OwnerBasicInfoAction.NextClicked)
        runCurrent()
        assertFalse(event.isCompleted)
        model.handleAction(OwnerBasicInfoAction.NameChanged("  청과마을  "))
        model.handleAction(OwnerBasicInfoAction.CategorySelected(category))
        model.handleAction(OwnerBasicInfoAction.AddressSelected(basicInfo.address!!))
        advanceUntilIdle()
        assertFalse(event.isCompleted)
        event.cancel()
        val restored = OwnerBasicInfoViewModel(SavedStateHandle(handle.keys().associateWith { handle.get<Any?>(it) }))
        store.put("restoredBasic", restored)
        assertEquals("  청과마을  ", restored.uiState.value.name)
        assertTrue(restored.uiState.value.isNextEnabled)
        restored.handleAction(OwnerBasicInfoAction.NextClicked)
        assertEquals(OwnerBasicInfoEvent.NavigateToOperatingInfo(basicInfo.copy(detailedAddress = "")), restored.event.first())
    }

    @Test fun restoredSampleCategoryMustBeSelectedAgainFromServerCategories() = runTest {
        val oldInfo = basicInfo.copy(category = StoreCategoryModel("debug-1", "과채류"))
        val model = OwnerBasicInfoViewModel(SavedStateHandle(mapOf("basicInfo" to oldInfo)))
        store.put("restoredBasic", model)
        assertEquals(oldInfo.name, model.uiState.value.name)
        assertEquals(null, model.uiState.value.category)
        assertFalse(model.uiState.value.isNextEnabled)
        model.handleAction(OwnerBasicInfoAction.CategorySelected(StoreCategoryModel.options[1]))
        assertTrue(model.uiState.value.isNextEnabled)
    }

    @Test fun nextEventContainsOnlyTheLatestValidatedSnapshot() = runTest {
        val model = OwnerBasicInfoViewModel(SavedStateHandle()).also { store.put("basic", it) }
        model.handleAction(OwnerBasicInfoAction.CategorySelected(category))
        model.handleAction(OwnerBasicInfoAction.AddressSelected(basicInfo.address!!))
        model.handleAction(OwnerBasicInfoAction.NameChanged("  첫 가게  "))
        model.handleAction(OwnerBasicInfoAction.DetailedAddressChanged("  1층  "))
        model.handleAction(OwnerBasicInfoAction.NextClicked)
        val first = model.event.first() as OwnerBasicInfoEvent.NavigateToOperatingInfo
        model.handleAction(OwnerBasicInfoAction.NameChanged("수정한 가게"))
        model.handleAction(OwnerBasicInfoAction.NextClicked)
        val second = model.event.first() as OwnerBasicInfoEvent.NavigateToOperatingInfo
        assertEquals("첫 가게", first.basicInfo.name)
        assertEquals("1층", first.basicInfo.detailedAddress)
        assertEquals("수정한 가게", second.basicInfo.name)
    }

    @Test fun changedBasicInfoReplacesSnapshotWithoutLosingOperatingInputAndRestores() = runTest {
        val handle = SavedStateHandle(mapOf(Constants.BASIC_INFO to basicInfo))
        val model = operating(handle)
        assertEquals(basicInfo, model.uiState.value.basicInfo)
        fill(model)
        val edited = basicInfo.copy(name = "수정한 가게", detailedAddress = "2층")
        handle[Constants.BASIC_INFO] = edited
        val restored = operating(SavedStateHandle(handle.keys().associateWith { handle.get<Any?>(it) }))
        assertEquals("수정한 가게", restored.uiState.value.registration.name)
        assertEquals("2층", restored.uiState.value.registration.detailAddress)
        assertEquals("02-1234-5678", restored.uiState.value.phoneNumber)
        assertEquals(540, restored.uiState.value.openingMinutes)
        assertTrue(restored.uiState.value.isSubmitEnabled)
    }

    @Test fun latestNavigationInfoTakesPriorityOverRestoredRegistration() = runTest {
        val handle = SavedStateHandle(mapOf(Constants.BASIC_INFO to basicInfo))
        fill(operating(handle))
        val edited = basicInfo.copy(name = "다시 수정한 가게")
        handle[Constants.BASIC_INFO] = edited
        val restored = operating(handle)
        assertEquals(edited, restored.uiState.value.basicInfo)
        assertEquals(540, restored.uiState.value.openingMinutes)
        assertEquals(1200, restored.uiState.value.closingMinutes)
        assertEquals("02-1234-5678", restored.uiState.value.phoneNumber)
    }

    @Test fun invalidSubmissionLeavesStateUnchanged() = runTest {
        val model = operating()
        val before = model.uiState.value
        model.handleAction(OwnerOperatingInfoAction.SubmitClicked)
        assertEquals(before, model.uiState.value)
        assertEquals(0, signupCalls)
        assertEquals(0, calls)
    }

    @Test fun failedRegistrationKeepsInputAndCanRetry() = runTest {
        val model = operating()
        fill(model)
        val registration = model.uiState.value.registration
        model.handleAction(OwnerOperatingInfoAction.SubmitClicked)
        advanceUntilIdle()
        assertEquals(OwnerOperatingInfoDialog.Error, model.uiState.value.dialog)
        assertEquals(registration, model.uiState.value.registration)
        model.handleAction(OwnerOperatingInfoAction.DialogDismissed)
        model.handleAction(OwnerOperatingInfoAction.SubmitClicked)
        advanceUntilIdle()
        assertEquals(OwnerOperatingInfoDialog.Error, model.uiState.value.dialog)
        assertFalse(model.uiState.value.isLoading)
        assertEquals(2, calls)
        assertEquals(1, signupCalls)
        assertEquals(listOf("signup", "store", "store"), order)
    }

    @Test fun submissionWaitsForServerAndCompletesOnceAfterConfirmation() = runTest {
        pending = CompletableDeferred()
        pendingSignup = CompletableDeferred()
        val model = operating()
        fill(model)
        model.handleAction(OwnerOperatingInfoAction.SubmitClicked)
        model.handleAction(OwnerOperatingInfoAction.SubmitClicked)
        runCurrent()
        assertTrue(model.uiState.value.isLoading)
        assertEquals(1, signupCalls)
        assertEquals(0, calls)
        pendingSignup!!.complete(AuthResult.Success(Unit))
        runCurrent()
        assertEquals(1, calls)
        assertEquals(listOf("signup", "store"), order)
        assertEquals("FRUIT", submitted?.categoryId)
        assertEquals("0212345678", submitted?.phone)
        assertEquals(setOf(1), submitted?.businessDays)
        val event = async { model.event.first() }
        model.handleAction(OwnerOperatingInfoAction.NavigationBackClicked)
        runCurrent()
        assertFalse(event.isCompleted)
        pending!!.complete(Result.success(Unit))
        runCurrent()
        assertFalse(model.uiState.value.isLoading)
        assertEquals(OwnerOperatingInfoDialog.Submitted, model.uiState.value.dialog)
        assertFalse(event.isCompleted)
        model.handleAction(OwnerOperatingInfoAction.CompletionConfirmed)
        model.handleAction(OwnerOperatingInfoAction.CompletionConfirmed)
        runCurrent()
        assertEquals(com.swyp.mangro.feature.owner.onboarding.screen.operating.OwnerOperatingInfoEvent.CompleteOnboarding, event.await())
        val duplicate = async { model.event.first() }
        runCurrent()
        assertFalse(duplicate.isCompleted)
        duplicate.cancel()
    }

    @Test fun signupFailureDoesNotRegisterStoreAndCanRetry() = runTest {
        signupResult = AuthResult.Failure(AuthFailure.NETWORK)
        val model = operating()
        fill(model)
        model.handleAction(OwnerOperatingInfoAction.SubmitClicked)
        advanceUntilIdle()
        assertEquals(1, signupCalls)
        assertEquals(0, calls)
        assertEquals(OwnerOperatingInfoDialog.Error, model.uiState.value.dialog)
        signupResult = AuthResult.Success(Unit)
        model.handleAction(OwnerOperatingInfoAction.DialogDismissed)
        model.handleAction(OwnerOperatingInfoAction.SubmitClicked)
        advanceUntilIdle()
        assertEquals(2, signupCalls)
        assertEquals(1, calls)
    }

    @Test fun expiredSignupSessionReturnsToLoginWithoutStoreRequest() = runTest {
        signupResult = AuthResult.Failure(AuthFailure.SIGNUP_REQUIRED)
        val model = operating()
        fill(model)
        model.handleAction(OwnerOperatingInfoAction.SubmitClicked)
        advanceUntilIdle()
        assertEquals(com.swyp.mangro.feature.owner.onboarding.screen.operating.OwnerOperatingInfoEvent.LoginRequired, model.event.first())
        assertEquals(0, calls)
        assertFalse(model.uiState.value.isLoading)
    }

    @Test fun hourlySelectionRejectsEarlierEndAndClearsEndWhenStartMovesPastIt() = runTest {
        val model = operating()
        model.handleAction(OwnerOperatingInfoAction.ClosingTimeSelected(540))
        assertEquals(null, model.uiState.value.closingMinutes)
        model.handleAction(OwnerOperatingInfoAction.OpeningTimeSelected(540))
        model.handleAction(OwnerOperatingInfoAction.ClosingTimeSelected(480))
        model.handleAction(OwnerOperatingInfoAction.ClosingTimeSelected(570))
        assertEquals(null, model.uiState.value.closingMinutes)
        model.handleAction(OwnerOperatingInfoAction.ClosingTimeSelected(540))
        assertEquals(540, model.uiState.value.closingMinutes)
        model.handleAction(OwnerOperatingInfoAction.OpeningTimeSelected(570))
        assertEquals(540, model.uiState.value.openingMinutes)
        model.handleAction(OwnerOperatingInfoAction.OpeningTimeSelected(600))
        assertEquals(null, model.uiState.value.closingMinutes)
        model.handleAction(OwnerOperatingInfoAction.ClosingTimeSelected(1380))
        assertEquals(1380, model.uiState.value.closingMinutes)
    }

    @Test fun operatingInputRestoresThroughSavedState() = runTest {
        val handle = SavedStateHandle(mapOf(Constants.BASIC_INFO to basicInfo))
        val model = operating(handle)
        fill(model)
        val restored = operating(SavedStateHandle(handle.keys().associateWith { handle.get<Any?>(it) }))
        assertEquals("02-1234-5678", restored.uiState.value.phoneNumber)
        assertEquals(1200, restored.uiState.value.closingMinutes)
        assertEquals(setOf(1), restored.uiState.value.businessDays)
        restored.handleAction(OwnerOperatingInfoAction.DialogDismissed)
        assertEquals(1200, restored.uiState.value.closingMinutes)
        restored.handleAction(OwnerOperatingInfoAction.OpeningTimeSelected(1500))
        assertEquals(540, restored.uiState.value.openingMinutes)
    }
}
