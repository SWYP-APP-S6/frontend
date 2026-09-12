package com.swyp.mangro.feature.owner.onboarding

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import com.swyp.mangro.feature.owner.onboarding.model.StoreAddressModel
import com.swyp.mangro.feature.owner.onboarding.model.StoreBasicInfoModel
import com.swyp.mangro.feature.owner.onboarding.model.StoreCategoryModel
import com.swyp.mangro.feature.owner.onboarding.screen.basic.OwnerBasicInfoAction
import com.swyp.mangro.feature.owner.onboarding.screen.basic.OwnerBasicInfoEvent
import com.swyp.mangro.feature.owner.onboarding.screen.basic.OwnerBasicInfoViewModel
import com.swyp.mangro.feature.owner.onboarding.screen.operating.OwnerOperatingInfoAction
import com.swyp.mangro.feature.owner.onboarding.screen.operating.OwnerOperatingInfoDialog
import com.swyp.mangro.feature.owner.onboarding.screen.operating.OwnerOperatingInfoViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
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

    private fun operating(handle: SavedStateHandle = SavedStateHandle(mapOf(Constants.BASIC_INFO to basicInfo))): OwnerOperatingInfoViewModel = OwnerOperatingInfoViewModel(
        handle,
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
    }

    @Test fun unconnectedRegistrationKeepsInputAndDoesNotReportSuccess() = runTest {
        val model = operating()
        fill(model)
        val registration = model.uiState.value.registration
        model.handleAction(OwnerOperatingInfoAction.SubmitClicked)
        assertEquals(OwnerOperatingInfoDialog.Error, model.uiState.value.dialog)
        assertEquals(registration, model.uiState.value.registration)
        model.handleAction(OwnerOperatingInfoAction.DialogDismissed)
        model.handleAction(OwnerOperatingInfoAction.SubmitClicked)
        assertEquals(OwnerOperatingInfoDialog.Error, model.uiState.value.dialog)
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
