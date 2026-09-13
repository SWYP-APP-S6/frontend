package com.swyp.mangro

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.core.designsystem.theme.OwnerMangroTypography
import com.swyp.mangro.feature.owner.product.model.OwnerProductModel
import com.swyp.mangro.feature.owner.product.navigation.OwnerProductListDestination
import com.swyp.mangro.feature.owner.product.navigation.ownerProductNavGraph
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class OwnerProductFlowTest {
    @get:Rule
    val compose = createComposeRule()

    private var saved = emptyList<OwnerProductModel>()
    private var cancellations = emptyList<String>()

    private fun show(initial: List<OwnerProductModel> = listOf(sample())) {
        compose.setContent {
            var products by remember { mutableStateOf(initial) }
            MangroTheme(typography = OwnerMangroTypography) {
                ProductTestNavHost(products, "20:00", { changes ->
                    saved = changes
                    products = products.map { old -> changes.find { it.id == old.id } ?: old }
                }, { cancellations = it })
            }
        }
    }

    @Test
    fun emptyCatalogAndRequiredRegistrationFields() {
        show(emptyList())
        compose.onNodeWithText("등록된 상품이 없어요").assertIsDisplayed()
        compose.onNodeWithText("상품 등록").performClick()
        compose.onNodeWithText("다음").assertIsNotEnabled()
        compose.onNodeWithText("품목명 입력").performScrollTo()
        compose.onAllNodes(hasSetTextAction())[0].performTextReplacement("복숭아 4입")
        compose.onNodeWithText("다음").assertIsNotEnabled()
    }

    @Test
    fun editingPreservesDraftAndSavesPreview() {
        show()
        compose.onNodeWithText("복숭아 4입").performClick()
        compose.onNodeWithText("상품 정보 수정").performScrollTo().performClick()
        compose.onAllNodes(hasSetTextAction())[0].performScrollTo().performTextReplacement("복숭아 6입")
        compose.onNodeWithText("다음").performClick()
        compose.onAllNodes(hasSetTextAction())[1].performScrollTo().performTextReplacement("3000")
        compose.onNodeWithText("다음").performClick()
        compose.onNodeWithText("등록하기").performClick()
        compose.onNodeWithText("상품 미리보기").assertIsDisplayed()
        compose.onNodeWithText("수정하기").performClick()
        compose.onNodeWithText("복숭아 6입").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("다음").performClick()
        compose.onNodeWithText("다음").performClick()
        compose.onNodeWithText("등록하기").performClick()
        compose.onNodeWithText("저장하기").performClick()
        compose.runOnIdle {
            assertEquals("복숭아 6입", saved.single().name)
            assertEquals(3000, saved.single().salePrice)
            assertEquals("20:00", saved.single().pickupEndTime)
        }
        compose.onNodeWithText("상품 관리 상세").assertIsDisplayed()
        compose.onNodeWithText("복숭아 6입").performScrollTo().assertIsDisplayed()
        compose.onNodeWithContentDescription("뒤로").performClick()
        compose.onNodeWithText("점포 관리").assertIsDisplayed()
    }

    @Test
    fun zeroQuantityRequiresConfirmationAndHidesProduct() {
        show(listOf(sample().copy(remainingQuantity = 1, reservedQuantity = 0)))
        compose.onNodeWithText("복숭아 4입").performClick()
        compose.onAllNodesWithContentDescription("수량 감소")[0].performScrollTo().performClick()
        compose.onNodeWithText("저장하기").performClick()
        compose.onNodeWithText("아니요").performClick()
        compose.runOnIdle { assertEquals(emptyList<OwnerProductModel>(), saved) }
        compose.onNodeWithText("저장하기").performClick()
        compose.onNodeWithText("네, 맞아요").performClick()
        compose.runOnIdle {
            assertEquals(0, saved.single().remainingQuantity)
            assertEquals(false, saved.single().isVisibleToCustomers)
        }
    }

    @Test
    fun directStockInputCanBeDeferredWithoutSaving() {
        show()
        compose.onNodeWithText("재고 재확인").performClick()
        compose.onNodeWithText("직접 입력").performScrollTo().performClick()
        compose.onAllNodes(hasSetTextAction())[0].performTextReplacement("-1")
        compose.onNodeWithText("적용").assertIsNotEnabled()
        compose.onAllNodes(hasSetTextAction())[0].performTextReplacement("2")
        compose.onNodeWithText("적용").performClick()
        compose.onNodeWithText("나중에 하기").performClick()
        compose.runOnIdle { assertEquals(emptyList<OwnerProductModel>(), saved) }
        compose.onNodeWithText("재고 재확인").performClick()
        compose.onNodeWithText("5").assertIsDisplayed()
    }

    @Test
    fun stockShortageSavesThenPassesProductIdToCancellation() {
        show()
        compose.onNodeWithText("재고 재확인").performClick()
        compose.onNodeWithText("직접 입력").performScrollTo().performClick()
        compose.onAllNodes(hasSetTextAction())[0].performTextReplacement("1")
        compose.onNodeWithText("적용").performClick()
        compose.onNodeWithText("저장하기").performClick()
        compose.onNodeWithText("네, 맞아요").performClick()
        compose.onNodeWithText("찜 취소하기").performClick()
        compose.runOnIdle {
            assertEquals(1, saved.single().remainingQuantity)
            assertEquals(listOf("peach"), cancellations)
        }
    }

    @Test
    fun draftSurvivesSavedStateRestoration() {
        val restoration = StateRestorationTester(compose)
        restoration.setContent {
            MangroTheme(typography = OwnerMangroTypography) {
                ProductTestNavHost(listOf(sample()), "20:00", {}, {})
            }
        }
        compose.onNodeWithText("복숭아 4입").performClick()
        compose.onNodeWithText("상품 정보 수정").performScrollTo().performClick()
        compose.onAllNodes(hasSetTextAction())[0].performScrollTo().performTextReplacement("복숭아 새 이름")
        restoration.emulateSavedInstanceStateRestore()
        compose.onNodeWithText("복숭아 새 이름").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("다음").performClick()
        compose.onNodeWithText("다음").performClick()
        compose.onNodeWithText("등록하기").performClick()
        compose.onNodeWithText("상품 미리보기").assertIsDisplayed()
    }

    @Composable
    private fun ProductTestNavHost(
        products: List<OwnerProductModel>,
        storeClosingTime: String,
        onSave: (List<OwnerProductModel>) -> Unit,
        onCancel: (List<String>) -> Unit,
    ) {
        val navController = rememberNavController()
        NavHost(navController, startDestination = OwnerProductListDestination) {
            ownerProductNavGraph(navController, products, storeClosingTime, onSave, onCancel)
        }
    }

    private fun sample() = OwnerProductModel("peach", "복숭아 4입", listOf("android.resource://com.swyp.mangro.owner/drawable/ic_camera_add"), 10000, 4000, 10, 5, 3, 2, "20:00")
}
