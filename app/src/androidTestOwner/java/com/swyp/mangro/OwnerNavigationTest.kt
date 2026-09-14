package com.swyp.mangro

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.test.espresso.Espresso.pressBack
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class OwnerNavigationTest {
    @get:Rule
    val compose = createAndroidComposeRule<ProductTestActivity>()

    private val restoration by lazy { StateRestorationTester(compose) }

    @Before
    fun openHome() {
        lateinit var navController: NavHostController
        restoration.setContent {
            navController = rememberNavController()
            OwnerTestNavHost(navController)
        }
        compose.enterOwnerRegistration(navController)
        compose.confirmAcceptedRegistration(navController)
    }

    @Test
    fun homeOpensProductManagementAndBackReturnsHome() {
        compose.onNodeWithText("점포 관리").performClick()
        compose.onNodeWithText("해당되는 상품이 없어요.").assertIsDisplayed()
        compose.onAllNodesWithText("점포 관리")[1].assertIsSelected()
        compose.onNodeWithText("찜 현황 10").performClick()
        compose.onNodeWithText("픽업완료").performClick()
        compose.onNodeWithText("홈").performClick()
        compose.onNodeWithText("홈").assertIsDisplayed()
        compose.onNodeWithText("점포 관리").performClick()
        compose.onNodeWithText("찜 현황 10").assertIsSelected()
        compose.onNodeWithText("픽업완료").assertIsSelected()
        pressBack()
        compose.onNodeWithText("홈").assertIsSelected()
    }

    @Test
    fun registrationFromHomeRespectsDiscardConfirmation() {
        compose.onNodeWithText("첫 상품 등록하러 가기").performScrollTo().performClick()
        compose.onNodeWithText("다음").assertIsNotEnabled()
        pressBack()
        compose.onNodeWithText("작성을 그만둘까요?").assertIsDisplayed()
        compose.onNodeWithText("아니요").performClick()
        compose.onNodeWithText("다음").assertIsNotEnabled()
        pressBack()
        compose.onNodeWithText("네, 맞아요").performClick()
        compose.onNodeWithText("홈").assertIsSelected()
    }
}
