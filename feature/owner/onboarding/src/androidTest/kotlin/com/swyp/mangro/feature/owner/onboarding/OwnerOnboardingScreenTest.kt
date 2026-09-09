package com.swyp.mangro.feature.owner.onboarding

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class OwnerOnboardingScreenTest {
    @get:Rule val compose = createComposeRule()
    private lateinit var state: OwnerOnboardingState
    private val categories = listOf(StoreCategory("fruit", "과채류"), StoreCategory("meat", "육류"))
    private val address = StoreAddress("03965", "서울 마포구 망원로 12")

    private fun show(submit: suspend (StoreRegistration) -> Unit = {}) {
        compose.setContent {
            state = rememberOwnerOnboardingState()
            OwnerOnboardingScreen(categories, { listOf(address) }, submit, {}, {}, state = state)
        }
    }

    private fun basic() {
        compose.runOnIdle {
            state.name.setTextAndPlaceCursorAtEnd("청과마을")
            state.category = categories.first()
            state.address = address
        }
    }

    private fun operating() {
        basic()
        compose.runOnIdle {
            state.step = 2
            state.phone.setTextAndPlaceCursorAtEnd("02-1234-5678")
            state.openingMinutes = 540
            state.closingMinutes = 1200
            state.businessDays = setOf(1, 2, 3, 4, 5)
        }
    }

    @Test fun emptyFieldsDisableNextAndCaptureStepOne() {
        show()
        compose.onNodeWithTag("next").assertIsNotEnabled()
        capture("step1")
    }

    @Test fun categorySelectionAndAddressSearchEnableNext() {
        show()
        compose.onNodeWithTag("name").performTextInput("청과마을")
        compose.onNodeWithTag("category").performScrollTo().performClick()
        compose.onNodeWithText("육류").performClick()
        compose.onNodeWithText("검색").performScrollTo().performClick()
        compose.onNodeWithTag("address-query").performTextInput("망원")
        compose.onAllNodesWithText("검색")[1].performClick()
        compose.waitUntil { compose.onAllNodesWithText(address.address).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText(address.address).performClick()
        compose.onNodeWithTag("next").assertIsEnabled().performClick()
        compose.onNodeWithTag("phone").assertExists()
    }

    @Test fun backAndRestorationPreserveBothSteps() {
        val restoration = StateRestorationTester(compose)
        restoration.setContent {
            state = rememberOwnerOnboardingState()
            OwnerOnboardingScreen(categories, { listOf(address) }, {}, {}, {}, state = state)
        }
        operating()
        restoration.emulateSavedInstanceStateRestore()
        compose.onNodeWithTag("phone").assertTextContains("02-1234-5678")
        compose.onNodeWithContentDescription("뒤로가기").performClick()
        compose.onNodeWithTag("name").assertTextContains("청과마을")
        compose.onNodeWithTag("next").performClick()
        compose.onNodeWithTag("phone").assertTextContains("02-1234-5678")
        compose.onNodeWithTag("day-1").performScrollTo().assertIsSelected()
    }

    @Test fun weekdaysToggleAndInvalidHoursDisableSubmission() {
        show()
        operating()
        capture("step2")
        compose.onNodeWithTag("day-0").performScrollTo().performClick().assertIsSelected()
        compose.runOnIdle {
            assertTrue(0 in state.businessDays)
            state.closingMinutes = 540
        }
        compose.onNodeWithTag("next").assertIsNotEnabled()
        compose.onNodeWithText("종료 시간은 시작 시간보다 늦어야 해요.").assertExists()
    }

    @Test fun failureRetainsInputAndRetrySendsWholeRequest() {
        var attempts = 0
        var request: StoreRegistration? = null
        show { value ->
            attempts++
            if (attempts == 1) error("test failure")
            request = value
        }
        operating()
        compose.onNodeWithTag("next").performClick()
        compose.onNodeWithText("등록 신청을 완료하지 못했어요").assertExists()
        compose.onNodeWithText("확인").performClick()
        compose.onNodeWithTag("phone").assertTextContains("02-1234-5678")
        compose.onNodeWithTag("next").performClick()
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
        compose.onNodeWithTag("next").performClick()
        compose.onNodeWithText("등록 신청 중이에요").assertExists()
        compose.runOnIdle {
            assertEquals(1, calls)
            pending.complete(Unit)
        }
        compose.onNodeWithText("등록 신청이 접수됐어요").assertExists()
        compose.runOnIdle { assertEquals(1, calls) }
    }

    @Test
    fun nativeTimePickerConfirmsAndCancelsWithoutChangingExistingTime() {
        show()
        operating()
        compose.onNodeWithText("오전 09:00").performScrollTo().performClick()
        onView(withId(android.R.id.button2)).perform(click())
        compose.runOnIdle { assertEquals(540, state.openingMinutes) }
        compose.onNodeWithText("오후 08:00").performClick()
        onView(withId(android.R.id.button1)).perform(click())
        compose.runOnIdle { assertEquals(1200, state.closingMinutes) }
    }

    @Test
    fun smallScreenWithLargeFontKeepsFieldsAndCtaReachable() {
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, 1.3f)) {
                Box(Modifier.requiredSize(320.dp, 640.dp)) {
                    state = rememberOwnerOnboardingState()
                    OwnerOnboardingScreen(categories, { listOf(address) }, {}, {}, {}, state = state)
                }
            }
        }
        basic()
        compose.onNodeWithTag("detail").performScrollTo().performTextInput("상가 1층")
        compose.onNodeWithTag("next").assertIsDisplayed().performClick()
        operating()
        compose.onNodeWithTag("day-6").performScrollTo().performClick().assertIsSelected()
        compose.onNodeWithTag("next").assertIsDisplayed().assertIsEnabled()
        compose.onNodeWithText("오전 09:00").performScrollTo().assertIsDisplayed()
        listOf("오전 09:00", "오후 08:00").forEach { value ->
            val layouts = mutableListOf<TextLayoutResult>()
            compose.onNodeWithText(value).performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
            assertTrue(layouts.isNotEmpty())
            assertFalse(layouts.any { it.hasVisualOverflow })
        }
        capture("small-large-font")
    }

    @Test
    fun searchFailureEmptyResultAndCancelKeepExistingAddress() {
        var searches = 0
        compose.setContent {
            state = rememberOwnerOnboardingState()
            OwnerOnboardingScreen(categories, { if (++searches == 1) error("failure") else emptyList() }, {}, {}, {}, state = state)
        }
        basic()
        compose.onNodeWithText("검색").performScrollTo().performClick()
        compose.onNodeWithTag("address-query").performTextInput("없는 주소")
        compose.onAllNodesWithText("검색")[1].performClick()
        compose.onNodeWithText("주소를 불러오지 못했어요. 다시 검색해주세요.").assertExists()
        compose.onAllNodesWithText("검색")[1].performClick()
        compose.onNodeWithText("검색 결과가 없어요. 다른 주소로 검색해주세요.").assertExists()
        compose.onNodeWithText("취소").performClick()
        compose.runOnIdle { assertEquals(address, state.address) }
    }

    private fun capture(name: String) {
        compose.waitForIdle()
        val directory = File(InstrumentationRegistry.getInstrumentation().targetContext.getExternalFilesDir(null), "onboarding-screenshots").apply { mkdirs() }
        File(directory, "$name.png").outputStream().use {
            compose.onRoot().captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }
}
