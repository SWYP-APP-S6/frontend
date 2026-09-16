package com.swyp.mangro

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class OwnerStartupNavigationTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun launchShowsKakaoLogin() {
        val loginText = compose.activity.getString(com.swyp.mangro.core.designsystem.R.string.kakao_login)
        compose.waitUntil(10_000) { compose.onAllNodesWithText(loginText).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText(
            compose.activity.getString(com.swyp.mangro.core.designsystem.R.string.kakao_login),
        ).assertIsDisplayed()
    }
}
