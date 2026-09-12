package com.swyp.mangro.feature.owner.onboarding

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import androidx.test.platform.app.InstrumentationRegistry
import com.swyp.mangro.feature.owner.onboarding.model.StoreAddressModel
import com.swyp.mangro.feature.owner.onboarding.model.StoreBasicInfoModel
import com.swyp.mangro.feature.owner.onboarding.model.StoreCategoryModel
import com.swyp.mangro.feature.owner.onboarding.model.StoreRegistrationModel
import com.swyp.mangro.feature.owner.onboarding.screen.basic.OwnerBasicInfoAction
import com.swyp.mangro.feature.owner.onboarding.screen.basic.OwnerBasicInfoRoute
import com.swyp.mangro.feature.owner.onboarding.screen.basic.OwnerBasicInfoViewModel
import com.swyp.mangro.feature.owner.onboarding.screen.operating.OwnerOperatingInfoAction
import com.swyp.mangro.feature.owner.onboarding.screen.operating.OwnerOperatingInfoRoute
import com.swyp.mangro.feature.owner.onboarding.screen.operating.OwnerOperatingInfoViewModel
import com.swyp.mangro.feature.owner.onboarding.util.StoreRegistrationSubmitter
import java.io.File
import kotlinx.coroutines.CompletableDeferred
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class OwnerOnboardingScreenTest {
    @get:Rule val compose = createComposeRule()
    private lateinit var basicModel: OwnerBasicInfoViewModel
    private lateinit var operatingModel: OwnerOperatingInfoViewModel
    private val modelStore = ViewModelStore()
    private val address = StoreAddressModel("03965", "서울 마포구 망원로 12")

    private fun createModels(submit: suspend (StoreRegistrationModel) -> Unit = {}) {
        basicModel = OwnerBasicInfoViewModel(SavedStateHandle())
        operatingModel = OwnerOperatingInfoViewModel(
            SavedStateHandle(),
            StoreRegistrationSubmitter(submit),
            SavedStateHandle(mapOf(Constants.BASIC_INFO to StoreBasicInfoModel("청과마을", StoreCategoryModel("fruit", "과채류"), address))),
        )
        modelStore.put("basic", basicModel)
        modelStore.put("operating", operatingModel)
    }

    @After
    fun clearModels() {
        compose.runOnUiThread { modelStore.clear() }
    }

    private fun showBasic(onNext: (StoreBasicInfoModel) -> Unit = {}, onBack: () -> Unit = {}, onSearch: () -> Unit = {}) {
        createModels()
        compose.setContent {
            OwnerBasicInfoRoute(onNext, onBack, onSearch, basicModel)
        }
    }

    private fun fillBasic() {
        compose.runOnIdle {
            basicModel.handleAction(OwnerBasicInfoAction.NameChanged("청과마을"))
            basicModel.handleAction(OwnerBasicInfoAction.CategorySelected(basicModel.uiState.value.categories.first()))
            basicModel.handleAction(OwnerBasicInfoAction.AddressSelected(address))
        }
    }

    @Composable
    private fun OperatingRoute(onBack: () -> Unit = {}) {
        OwnerOperatingInfoRoute({}, onBack, operatingModel)
    }

    private fun show(submit: suspend (StoreRegistrationModel) -> Unit = {}) {
        createModels(submit)
        basicModel.handleAction(OwnerBasicInfoAction.NameChanged("청과마을"))
        basicModel.handleAction(OwnerBasicInfoAction.AddressSelected(address))
        basicModel.handleAction(OwnerBasicInfoAction.CategoriesReceived(listOf(StoreCategoryModel("fruit", "과채류"))))
        basicModel.handleAction(OwnerBasicInfoAction.CategorySelected(basicModel.uiState.value.categories.first()))
        compose.setContent { OperatingRoute() }
    }

    private fun operating() {
        compose.runOnIdle {
            operatingModel.handleAction(OwnerOperatingInfoAction.PhoneNumberChanged("02-1234-5678"))
            operatingModel.handleAction(OwnerOperatingInfoAction.OpeningTimeSelected(540))
            operatingModel.handleAction(OwnerOperatingInfoAction.ClosingTimeSelected(1200))
            (1..5).filterNot { it in operatingModel.uiState.value.businessDays }.forEach {
                operatingModel.handleAction(OwnerOperatingInfoAction.BusinessDayClicked(it))
            }
        }
    }

    @Test
    fun emptyFieldsDisableNext() {
        showBasic()
        compose.onNodeWithText("다음").assertIsNotEnabled()
        capture("step1")
    }

    @Test
    fun selectedAddressEnablesNextAndCallsNavigation() {
        var nextCalls = 0
        showBasic(onNext = { nextCalls++ })
        compose.onAllNodes(hasSetTextAction())[0].performTextInput("청과마을")
        compose.onNodeWithText("과채류").performScrollTo().performClick()
        compose.onNodeWithText("육류").performClick()
        compose.onNodeWithText("다음").assertIsNotEnabled()
        compose.runOnIdle { basicModel.handleAction(OwnerBasicInfoAction.AddressSelected(address)) }
        compose.onNodeWithText(address.address).assertExists()
        compose.onNodeWithText("다음").assertIsEnabled().performClick()
        compose.runOnIdle { assertEquals(1, nextCalls) }
    }

    @Test
    fun searchRequestsNavigationWithoutChangingExistingInput() {
        var searches = 0
        showBasic(onSearch = { searches++ })
        fillBasic()
        compose.runOnIdle { basicModel.handleAction(OwnerBasicInfoAction.DetailedAddressChanged("상가 1층")) }
        compose.onNodeWithText("검색").performScrollTo().performClick()
        compose.runOnIdle {
            assertEquals(1, searches)
            assertEquals(address, basicModel.uiState.value.address)
            assertEquals("상가 1층", basicModel.uiState.value.detailedAddress)
        }
    }

    @Test
    fun basicToolbarAndSystemBackCallNavigation() {
        var backs = 0
        showBasic(onBack = { backs++ })
        compose.onNodeWithContentDescription("뒤로가기").performClick()
        compose.runOnIdle { assertEquals(1, backs) }
        androidx.test.espresso.Espresso.pressBack()
        compose.runOnIdle { assertEquals(2, backs) }
    }

    @Test fun weekdaysToggleAndChangingStartPastEndDisablesSubmission() {
        show()
        operating()
        capture("step2")
        compose.onNodeWithText("일").performScrollTo().performClick()
        compose.runOnIdle {
            assertTrue(0 in operatingModel.uiState.value.businessDays)
            operatingModel.handleAction(OwnerOperatingInfoAction.OpeningTimeSelected(1260))
        }
        compose.onNodeWithText("등록 신청").assertIsNotEnabled()
        compose.runOnIdle { assertEquals(null, operatingModel.uiState.value.closingMinutes) }
    }

    @Test fun failureRetainsInputAndRetrySendsWholeRequest() {
        var attempts = 0
        var request: StoreRegistrationModel? = null
        show { value ->
            attempts++
            if (attempts == 1) error("test failure")
            request = value
        }
        operating()
        compose.onNodeWithText("등록 신청").performClick()
        compose.onNodeWithText("등록 신청을 완료하지 못했어요").assertExists()
        compose.onNodeWithText("확인").performClick()
        compose.onNodeWithTag("phone").assertTextContains("02-1234-5678")
        compose.onNodeWithText("등록 신청").performClick()
        compose.onNodeWithText("등록 신청이 접수됐어요").assertExists()
        compose.runOnIdle {
            assertEquals(2, attempts)
            assertEquals("0212345678", request?.phone)
            assertEquals(address, request?.address)
            assertEquals(setOf(1, 2, 3, 4, 5), request?.businessDays)
        }
    }

    @Test fun pendingRequestCannotBeSubmittedTwice() {
        val pending = CompletableDeferred<Unit>()
        var calls = 0
        show {
            calls++
            pending.await()
        }
        operating()
        compose.onNodeWithText("등록 신청").performClick()
        compose.onNodeWithText("등록 신청 중이에요").assertExists()
        compose.runOnIdle {
            assertEquals(1, calls)
            pending.complete(Unit)
        }
        compose.onNodeWithText("등록 신청이 접수됐어요").assertExists()
        compose.runOnIdle { assertEquals(1, calls) }
    }

    @Test
    fun hourlyDropdownFiltersEndAndClearsInvalidSelection() {
        show()
        compose.onNodeWithText("종료 시간").performScrollTo().assertIsNotEnabled()
        compose.onNodeWithText("시작 시간").performScrollTo().performClick()
        compose.onNodeWithText("09:30").assertDoesNotExist()
        compose.onNodeWithText("09:00").performScrollTo().performClick()
        compose.onNodeWithText("종료 시간").performScrollTo().performClick()
        compose.onNodeWithText("08:00").assertDoesNotExist()
        compose.onNodeWithText("13:00").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(780, operatingModel.uiState.value.closingMinutes) }
        compose.onNodeWithText("09:00").performClick()
        compose.onNodeWithText("14:00").performScrollTo().performClick()
        compose.onNodeWithText("종료 시간").assertExists()
        compose.runOnIdle { assertEquals(null, operatingModel.uiState.value.closingMinutes) }
    }

    @Test
    fun smallScreenWithLargeFontKeepsBasicFieldsReachable() {
        createModels()
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, 1.3f)) {
                Box(Modifier.requiredSize(320.dp, 640.dp)) {
                    OwnerBasicInfoRoute({}, {}, {}, basicModel)
                }
            }
        }
        fillBasic()
        compose.onAllNodes(hasSetTextAction())[1].performScrollTo().performTextInput("상가 1층")
        compose.onNodeWithText("다음").assertIsDisplayed().assertIsEnabled()
        compose.runOnIdle { assertEquals("상가 1층", basicModel.uiState.value.detailedAddress) }
        capture("small-large-font")
    }

    private fun capture(name: String) {
        compose.waitForIdle()
        val directory = File(InstrumentationRegistry.getInstrumentation().targetContext.getExternalFilesDir(null), "onboarding-screenshots").apply { mkdirs() }
        File(directory, "$name.png").outputStream().use {
            compose.onRoot().captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }
}
