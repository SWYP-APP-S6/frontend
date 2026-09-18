package com.swyp.mangro.feature.consumer.myinfo

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.data.auth.model.TermsKind
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class MyInfoScreenTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test fun guestCanLinkKakaoAndReadPoliciesWithoutLogout() {
        val actions = mutableListOf<MyInfoUiAction>()
        compose.setContent {
            MangroTheme { MyInfoScreen(MyInfoUiState(isLoading = false, isGuest = true), actions::add) }
        }
        compose.onNodeWithText("비회원으로 이용 중이에요").assertIsDisplayed()
        compose.onNodeWithText("카카오로 계속 연동하기").performClick()
        compose.onNodeWithText("로그아웃").assertDoesNotExist()
        compose.onNodeWithText("서비스 이용약관").performScrollTo().performClick()
        compose.onNodeWithText("개인정보 처리방침").performScrollTo().performClick()
        compose.runOnIdle {
            assertEquals(
                listOf(MyInfoUiAction.LinkKakaoClicked, MyInfoUiAction.PolicyClicked(TermsKind.SERVICE), MyInfoUiAction.PolicyClicked(TermsKind.PRIVACY_POLICY)),
                actions,
            )
        }
    }

    @Test fun memberSeesProfileAndLogoutWithoutKakaoLink() {
        val actions = mutableListOf<MyInfoUiAction>()
        compose.setContent {
            MangroTheme { MyInfoScreen(MyInfoUiState(isLoading = false, nickname = "망그로", phone = "01012345678"), actions::add) }
        }
        compose.onNodeWithText("망그로").assertIsDisplayed()
        compose.onNodeWithText("01012345678").assertIsDisplayed()
        compose.onNodeWithText("카카오로 계속 연동하기").assertDoesNotExist()
        compose.onNodeWithText("로그아웃").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(listOf(MyInfoUiAction.LogoutClicked), actions) }
    }
}
