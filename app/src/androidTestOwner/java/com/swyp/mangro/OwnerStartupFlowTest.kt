package com.swyp.mangro

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

/** Run on a signed-out installation. Actual Kakao consent requires a dedicated test account. */
class OwnerStartupFlowTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun signedOutStartupAndPolicyReturnRemainOnLoginAfterRecreation() {
        compose.waitUntil(timeoutMillis = 10_000) {
            compose.onAllNodesWithText("카카오로 시작하기").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("개인정보처리방침").performClick()
        compose.onNodeWithText("약관 페이지를 준비하고 있어요").assertIsDisplayed()
        compose.onNodeWithContentDescription("뒤로").performClick()
        compose.onNodeWithText("카카오로 시작하기").assertIsDisplayed()
        compose.activityRule.scenario.recreate()
        compose.onNodeWithText("카카오로 시작하기").assertIsDisplayed()
    }
}
