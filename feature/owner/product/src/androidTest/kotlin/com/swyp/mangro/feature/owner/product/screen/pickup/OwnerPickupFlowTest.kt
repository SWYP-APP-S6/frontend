package com.swyp.mangro.feature.owner.product.screen.pickup

import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.SavedStateHandle
import androidx.test.platform.app.InstrumentationRegistry
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.core.designsystem.theme.OwnerMangroTypography
import com.swyp.mangro.data.owner.product.model.HoldStatus
import com.swyp.mangro.feature.owner.product.ManagementFakeRepository
import com.swyp.mangro.feature.owner.product.screen.list.ProductListAction
import com.swyp.mangro.feature.owner.product.screen.list.ProductListRoute
import com.swyp.mangro.feature.owner.product.screen.list.ProductListTab
import com.swyp.mangro.feature.owner.product.screen.list.ProductListViewModel
import com.swyp.mangro.feature.owner.product.screen.pickup.cancellation.PickupCancellationRoute
import com.swyp.mangro.feature.owner.product.screen.pickup.cancellation.PickupCancellationViewModel
import com.swyp.mangro.feature.owner.product.screen.pickup.detail.PickupDetailRoute
import com.swyp.mangro.feature.owner.product.screen.pickup.detail.PickupDetailViewModel
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class OwnerPickupFlowTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private fun waitFor(text: String) {
        compose.waitUntil(5000) { compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty() }
    }

    @Test fun groupedDetailShowsAllProductsAndCompletesOnce() {
        val repo = ManagementFakeRepository()
        lateinit var vm: PickupDetailViewModel
        compose.runOnUiThread { vm = PickupDetailViewModel(SavedStateHandle(mapOf("pickupId" to "8")), repo) }
        compose.setContent { MangroTheme(typography = OwnerMangroTypography) { PickupDetailRoute({}, {}, vm) } }
        waitFor("12,000원")
        compose.onNodeWithText("복숭아 4입 * 1개\n사과 * 2개").assertExists()
        capture("detail")
        compose.onNodeWithText("픽업 완료했어요").performClick()
        waitFor("픽업 완료된 찜")
        compose.onNodeWithText("픽업 완료된 찜").assertIsNotEnabled()
        compose.runOnIdle { assertEquals(1, repo.writes) }
    }

    @Test fun detailDoesNotFetchCancellationCandidatesOrBlockSuggestedHolds() {
        val repo = ManagementFakeRepository()
        lateinit var vm: PickupDetailViewModel
        compose.runOnUiThread { vm = PickupDetailViewModel(SavedStateHandle(mapOf("pickupId" to "8")), repo) }
        compose.setContent { MangroTheme(typography = OwnerMangroTypography) { PickupDetailRoute({}, {}, vm) } }
        waitFor("12,000원")
        compose.onNodeWithText("픽업 완료했어요").assertIsEnabled()
        compose.runOnIdle {
            assertEquals(0, repo.writes)
            assertEquals(0, repo.cancellationReads)
        }
    }

    @Test fun cancellationUsesSuggestedSelectionAndServerNotice() {
        val repo = ManagementFakeRepository()
        lateinit var vm: PickupCancellationViewModel
        compose.runOnUiThread { vm = PickupCancellationViewModel(SavedStateHandle(), repo) }
        compose.setContent { MangroTheme(typography = OwnerMangroTypography) { PickupCancellationRoute({}, vm) } }
        waitFor("1건 취소하기")
        compose.onNodeWithText("1건 취소하기").performClick()
        waitFor("서버에서 내려온 안내 메시지")
        capture("cancellation-confirmation")
        compose.onNodeWithText("취소하고 안내 보내기").performClick()
        waitFor("지금은 취소해야 할 주문이 없어요.")
        compose.onNodeWithText("0건 취소하기").assertIsNotEnabled()
        compose.runOnIdle { assertEquals(1, repo.writes) }
    }

    @Test fun listLoadsServerHoldsAndFilters() {
        val repo = ManagementFakeRepository()
        lateinit var vm: ProductListViewModel
        compose.runOnUiThread { vm = ProductListViewModel(SavedStateHandle(mapOf("store_tab" to "PICKUPS")), repo) }
        compose.setContent { MangroTheme(typography = OwnerMangroTypography) { ProductListRoute(emptyList(), {}, {}, {}, {}, vm) } }
        waitFor("방문손님님")
        compose.runOnIdle {
            assertEquals(1, repo.holdReads)
            assertEquals(0, repo.cancellationReads)
        }
        capture("holds")
        compose.onNodeWithText("픽업완료", useUnmergedTree = true).performClick()
        waitFor("해당되는 상품이 없어요.")
        compose.onNodeWithText("해당되는 상품이 없어요.").assertIsDisplayed()
        compose.runOnIdle { assertEquals(listOf(null, HoldStatus.COMPLETED), repo.requestedStatuses) }
    }

    @Test fun registeredProductsWaitForCatalogApiAndOnlyPickupTabLoadsHolds() {
        val repo = ManagementFakeRepository()
        lateinit var vm: ProductListViewModel
        compose.runOnUiThread { vm = ProductListViewModel(SavedStateHandle(), repo) }
        compose.setContent { MangroTheme(typography = OwnerMangroTypography) { ProductListRoute(emptyList(), {}, {}, {}, {}, vm) } }
        waitFor("등록 상품 목록은 준비 중이에요.")
        compose.runOnIdle {
            assertEquals(0, repo.holdReads)
            assertEquals(0, repo.cancellationReads)
            vm.handleAction(ProductListAction.TabSelected(ProductListTab.PICKUPS))
        }
        waitFor("방문손님님")
        compose.runOnIdle {
            assertEquals(1, repo.holdReads)
            assertEquals(0, repo.cancellationReads)
        }
    }
    private fun capture(name: String) {
        compose.waitForIdle()
        val output = InstrumentationRegistry.getArguments().getString("additionalTestOutputDir")
        val directory = File(output ?: compose.activity.filesDir.path, "owner-management-screenshots").apply { mkdirs() }
        val file = File(directory, "$name.png")
        file.outputStream().use { InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot().compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
}
