package com.swyp.mangro

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso.pressBack
import org.junit.Rule
import org.junit.Test

class OwnerNavigationTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun homeOpensProductManagementAndBackReturnsHome() {
        compose.onNodeWithText("점포 관리").performClick()
        compose.onNodeWithText("등록된 상품이 없어요").assertIsDisplayed()
        compose.onNodeWithContentDescription("뒤로").performClick()
        compose.onNodeWithText("홈").assertIsDisplayed()
        compose.onNodeWithText("점포 관리").performClick()
        pressBack()
        compose.onNodeWithText("홈").assertIsDisplayed()
    }

    @Test
    fun registrationBackRespectsDiscardConfirmationAndReturnsToList() {
        compose.onNodeWithText("점포 관리").performClick()
        compose.onNodeWithText("상품 등록").performClick()
        compose.onNodeWithText("다음").assertIsNotEnabled()
        pressBack()
        compose.onNodeWithText("작성을 그만둘까요?").assertIsDisplayed()
        compose.onNodeWithText("아니요").performClick()
        compose.onNodeWithText("다음").assertIsNotEnabled()
        pressBack()
        compose.onNodeWithText("네, 맞아요").performClick()
        compose.onNodeWithText("등록된 상품이 없어요").assertIsDisplayed()
    }
}
