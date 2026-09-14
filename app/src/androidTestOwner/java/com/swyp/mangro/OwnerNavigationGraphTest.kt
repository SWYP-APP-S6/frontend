package com.swyp.mangro

import androidx.compose.runtime.getValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.test.espresso.Espresso.pressBack
import com.swyp.mangro.feature.owner.product.model.OwnerProductModel
import com.swyp.mangro.feature.owner.product.screen.pickup.cancellation.OwnerPickupCancellationDestination
import com.swyp.mangro.navigation.OwnerNavHost
import com.swyp.mangro.theme.MangroTheme
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class OwnerNavigationGraphTest {
    @get:Rule
    val compose = createAndroidComposeRule<ProductTestActivity>()
    private val restoration by lazy { StateRestorationTester(compose) }
    private lateinit var navController: NavHostController

    @Before
    fun showGraph() {
        restoration.setContent {
            val viewModel: OwnerMainViewModel = hiltViewModel()
            val homePickups by viewModel.homePickups.collectAsStateWithLifecycle()
            navController = rememberNavController()
            MangroTheme {
                OwnerNavHost(
                    products = listOf(OwnerProductModel("test-product", "테스트 상품", emptyList(), 10000, 4000, 5, 5, pickupEndTime = "20:00")),
                    homePickups = homePickups,
                    storeClosingTime = "20:00",
                    storeOpeningTime = "09:00",
                    onSaveProducts = {},
                    onCompletePickup = viewModel::completePickup,
                    navController = navController,
                )
            }
        }
        compose.loginToOwnerRegistration()
    }

    @Test
    fun homeShortcutsOverrideFiltersWhileBottomTabsRestoreThem() {
        compose.confirmAcceptedRegistration(navController)
        compose.onNode(hasText("픽업 완료") and hasText("1건")).performScrollTo().performClick()
        compose.onNodeWithText("찜 현황 10").assertIsSelected()
        compose.onNodeWithText("픽업완료").assertIsSelected()
        tab("홈").performClick()
        tab("점포 관리").performClick()
        compose.onNodeWithText("픽업완료").assertIsSelected()
        tab("홈").performClick()
        compose.onNodeWithText("방문 예정").performScrollTo().performClick()
        compose.onNodeWithText("찜 현황 10").assertIsSelected()
        compose.onNodeWithText("전체").assertIsSelected()
        tab("홈").performClick()
        compose.onNodeWithText("판매중").performScrollTo().performClick()
        compose.onNodeWithText("등록된 상품 1").assertIsSelected()
    }

    @Test
    fun cancellationDestinationRetainsItsProductScopeAfterNavigationStateRestoration() {
        compose.confirmAcceptedRegistration(navController)
        compose.runOnIdle {
            navController.navigate(OwnerPickupCancellationDestination(listOf("peach")))
        }
        compose.onNodeWithText("송유나 님 * 2").assertIsDisplayed()
        compose.onNodeWithText("건우건어물 님 * 2").assertDoesNotExist()
        restoration.emulateSavedInstanceStateRestore()
        compose.onNodeWithText("송유나 님 * 2").assertIsDisplayed()
        compose.onNodeWithText("건우건어물 님 * 2").assertDoesNotExist()
        pressBack()
        tab("홈").assertIsSelected()
    }

    @Test
    fun submittedRegistrationReturnsHomeWithoutLeavingOnboardingInTheBackStack() {
        compose.confirmAcceptedRegistration(navController)
        tab("홈").assertIsSelected()
        compose.runOnIdle { assertNull(navController.previousBackStackEntry) }
    }

    private fun tab(label: String) = compose.onNode(hasText(label) and isSelectable())
}
