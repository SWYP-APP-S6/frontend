package com.swyp.mangro

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StartupNavigationTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun launch_reachesLoginAfterSplash() {
        val loginText = composeRule.activity.getString(
            com.swyp.mangro.feature.auth.R.string.login_browse_without_login_button,
        )

        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText(loginText).fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithText(loginText).assertIsDisplayed()
        composeRule.onNodeWithText(
            composeRule.activity.getString(com.swyp.mangro.feature.auth.R.string.login_greeting),
        ).assertIsDisplayed()
        composeRule.onNodeWithText(
            composeRule.activity.getString(com.swyp.mangro.feature.auth.R.string.login_message_closing),
        ).assertIsDisplayed()
    }
}
