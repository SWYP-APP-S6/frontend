package com.swyp.mangro

import android.graphics.Bitmap
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso.pressBack
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import org.junit.Rule
import org.junit.Test

class OwnerSettingFlowTest {
    @get:Rule
    val compose = createAndroidComposeRule<OwnerNavigationTestActivity>()

    @Test
    fun settingsCanBeOpenedFromBothTabsAndReselectedWithoutDuplicatingBackStack() {
        tab("설정").performClick()
        compose.onNodeWithText("상점 정보").assertIsDisplayed()
        tab("설정").assertIsSelected()
        capture("owner-setting-main")
        tab("설정").performClick()
        tab("점포 관리").performClick()
        tab("점포 관리").assertIsSelected()
        tab("설정").performClick()
        compose.onNodeWithText("상점 정보").assertIsDisplayed()
        tab("홈").performClick()
        tab("홈").assertIsSelected()
        tab("설정").performClick()
        pressBack()
        tab("홈").assertIsSelected()
    }

    @Test
    fun eachPolicyRestoresItsTitleAfterRecreation() {
        tab("설정").performClick()
        compose.onNodeWithText("서비스 이용약관").performClick()
        assertPolicy("서비스 이용약관")
        capture("owner-setting-terms")
        compose.onNodeWithContentDescription("뒤로").performClick()
        compose.onNodeWithText("개인정보 처리방침").performClick()
        assertPolicy("개인정보 처리방침")
        compose.activityRule.scenario.recreate()
        assertPolicy("개인정보 처리방침")
        capture("owner-setting-privacy")
        pressBack()
        compose.onNodeWithText("상점 정보").assertIsDisplayed()
        tab("설정").assertIsSelected()
    }

    private fun tab(label: String) = compose.onNode(hasText(label) and isSelectable())

    private fun assertPolicy(title: String) {
        compose.onNodeWithText(title).assertIsDisplayed()
    }

    private fun capture(name: String) {
        compose.waitForIdle()
        // Wait for the navigation transition to reach the display before capturing.
        Thread.sleep(500)
        val bitmap = InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
        File(compose.activity.getExternalFilesDir(null), "$name.png").outputStream().use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        bitmap.recycle()
    }
}
