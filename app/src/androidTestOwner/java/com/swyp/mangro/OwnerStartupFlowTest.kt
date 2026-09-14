package com.swyp.mangro

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.test.espresso.Espresso.pressBack
import org.junit.Rule
import org.junit.Test

class OwnerStartupFlowTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun loginPolicyReturnsToLoginAndOnboardingBackReturnsThroughTerms() {
        compose.waitUntil(timeoutMillis = 10_000) {
            compose.onAllNodesWithText("카카오로 시작하기").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("개인정보처리방침").performClick()
        compose.onNodeWithText("약관 페이지를 준비하고 있어요").assertIsDisplayed()
        compose.onNodeWithContentDescription("뒤로").performClick()
        compose.onNodeWithText("카카오로 시작하기").assertIsDisplayed()
        compose.loginToOwnerRegistration()
        compose.activityRule.scenario.recreate()
        compose.onNodeWithText("가게 정보를 등록해주시면").assertIsDisplayed()
        compose.onNodeWithContentDescription("뒤로가기").performClick()
        compose.onNodeWithText("약관 동의가 필요해요").assertIsDisplayed()
        compose.onNodeWithText("확인하기").assertIsEnabled()
        pressBack()
        compose.onNodeWithText("카카오로 시작하기").assertIsDisplayed()
    }

    @Test
    fun loginOpensStoreRegistrationAndAddressBackPreservesTheForm() {
        compose.loginToOwnerRegistration()
        compose.onNodeWithText("가게 정보를 등록해주시면").assertIsDisplayed()
        compose.onAllNodes(hasSetTextAction())[0].performTextReplacement("네비게이션 확인 상점")
        compose.onNodeWithText("검색").performClick()
        compose.onNodeWithText("주소 검색").assertIsDisplayed()
        pressBack()
        compose.onNodeWithText("네비게이션 확인 상점").assertIsDisplayed()
        compose.onNodeWithContentDescription("뒤로가기").performClick()
        compose.onNodeWithText("약관 동의가 필요해요").assertIsDisplayed()
        compose.onNodeWithText("확인하기").assertIsEnabled()
        pressBack()
        compose.onNodeWithText("카카오로 시작하기").assertIsDisplayed()
    }

    @Test
    fun requiredConsentGatesOnboardingAndMarketingIsOptional() {
        compose.loginToOwnerTerms()
        compose.onNodeWithText("확인하기").assertIsNotEnabled()
        compose.onNodeWithText("전체동의").performClick()
        compose.onNodeWithText("확인하기").assertIsEnabled()
        // Checkbox order: all, service, privacy, location, third party, marketing.
        compose.onAllNodes(isToggleable())[5].performClick().assertIsOff()
        compose.onNodeWithText("확인하기").assertIsEnabled()
        compose.onAllNodes(isToggleable())[4].performClick().assertIsOff()
        compose.onNodeWithText("확인하기").assertIsNotEnabled()
        compose.activityRule.scenario.recreate()
        compose.onAllNodes(isToggleable())[4].assertIsOff()
        compose.onAllNodes(isToggleable())[5].assertIsOff()
        compose.onNodeWithText("확인하기").assertIsNotEnabled()
        compose.onAllNodes(isToggleable())[4].performClick().assertIsOn()
        compose.onNodeWithText("확인하기").performClick()
        compose.onNodeWithText("가게 정보를 등록해주시면").assertIsDisplayed()
        pressBack()
        compose.onNodeWithText("약관 동의가 필요해요").assertIsDisplayed()
        compose.onAllNodes(isToggleable())[5].assertIsOff()
    }
}
