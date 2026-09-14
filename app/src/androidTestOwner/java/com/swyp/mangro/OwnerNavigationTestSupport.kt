package com.swyp.mangro

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.core.os.bundleOf
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.swyp.mangro.feature.owner.onboarding.model.StoreBasicInfoModel
import com.swyp.mangro.feature.owner.onboarding.screen.operating.OwnerOperatingInfoDestination
import com.swyp.mangro.navigation.OwnerNavHost
import com.swyp.mangro.theme.MangroTheme

internal fun ComposeContentTestRule.loginToOwnerTerms() {
    waitUntil(timeoutMillis = 10_000) {
        onAllNodesWithText("카카오로 시작하기").fetchSemanticsNodes().isNotEmpty()
    }
    onNodeWithText("카카오로 시작하기").performClick()
    onNodeWithText("약관 동의가 필요해요").assertIsDisplayed()
}

internal fun ComposeContentTestRule.loginToOwnerRegistration() {
    loginToOwnerTerms()
    onNodeWithText("전체동의").performClick()
    onNodeWithText("확인하기").performClick()
    onNodeWithText("가게 정보를 등록해주시면").assertIsDisplayed()
}

/** Accepted server response fixture for home tests; production registration is still unconnected. */
internal fun ComposeContentTestRule.confirmAcceptedRegistration(navController: NavHostController) {
    runOnIdle {
        val destination = requireNotNull(navController.currentDestination?.parent?.findNode<OwnerOperatingInfoDestination>())
        navController.navigate(
            destination.id,
            bundleOf("onboarding_basic_info" to StoreBasicInfoModel(name = "확인 상점"), "dialog" to "Submitted"),
        )
    }
    onNodeWithText("등록 신청이 접수됐어요").assertIsDisplayed()
    onNodeWithText("확인").performClick()
    onNodeWithText("홈").assertIsDisplayed()
}

@Composable
internal fun OwnerTestNavHost(navController: NavHostController) {
    val viewModel: OwnerMainViewModel = hiltViewModel()
    val homePickups by viewModel.homePickups.collectAsStateWithLifecycle()
    MangroTheme {
        OwnerNavHost(
            products = emptyList(),
            homePickups = homePickups,
            storeClosingTime = "20:00",
            storeOpeningTime = "09:00",
            onSaveProducts = {},
            onCompletePickup = viewModel::completePickup,
            navController = navController,
        )
    }
}
