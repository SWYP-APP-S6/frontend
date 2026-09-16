package com.swyp.mangro

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.test.platform.app.InstrumentationRegistry
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.core.designsystem.theme.OwnerMangroTypography
import com.swyp.mangro.feature.owner.product.model.OwnerProductModel
import com.swyp.mangro.feature.owner.product.navigation.OwnerProductEditorDestination
import com.swyp.mangro.feature.owner.product.navigation.ownerProductNavGraph
import com.swyp.mangro.feature.owner.product.screen.list.OwnerProductListDestination
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class OwnerProductFlowTest {
    @get:Rule
    val compose = createAndroidComposeRule<ProductTestActivity>()

    private var saved = emptyList<OwnerProductModel>()
    private var cancellations = emptyList<String>()

    private fun show(initial: List<OwnerProductModel> = listOf(sample()), startInEditor: Boolean = false) {
        compose.setContent {
            var products by remember { mutableStateOf(initial) }
            MangroTheme(typography = OwnerMangroTypography) {
                ProductTestNavHost(products, "20:00", { changes ->
                    saved = changes
                    products = products.map { old -> changes.find { it.id == old.id } ?: old }
                }, { cancellations = it }, startInEditor)
            }
        }
    }

    @Test
    fun emptyCatalogAndRequiredRegistrationFields() {
        show(listOf(), startInEditor = true)
        compose.onNodeWithText("다음").assertIsNotEnabled()
        compose.onNodeWithText("예시) 복숭아 4입").performScrollTo()
        compose.onAllNodes(hasSetTextAction())[0].performTextReplacement("복숭아 4입")
        compose.onNodeWithText("다음").assertIsNotEnabled()
    }

    @Test
    fun zeroQuantityRequiresConfirmationAndHidesProduct() {
        show(listOf(sample().copy(remainingQuantity = 1, reservedQuantity = 0)))
        compose.onNodeWithText("복숭아 4입").performClick()
        compose.onAllNodesWithContentDescription("수량 감소")[0].performScrollTo().performClick()
        compose.onNodeWithText("저장하기").performClick()
        compose.onNodeWithText("지금 판매 가능한 수량이\n0개가 맞나요?").assertIsDisplayed()
        captureSheet("quantity-zero")
        compose.onNodeWithText("아니요").performClick()
        compose.runOnIdle { assertEquals(emptyList<OwnerProductModel>(), saved) }
        compose.onNodeWithText("저장하기").performClick()
        compose.onNodeWithText("네, 맞아요").performClick()
        compose.onNode(isDialog()).assertIsDisplayed()
        compose.onNodeWithText("저장이 완료되었습니다.").assertIsDisplayed()
        compose.runOnIdle {
            assertEquals(0, saved.single().remainingQuantity)
            assertEquals(false, saved.single().isVisibleToCustomers)
        }
    }

    @Test
    fun shortageConfirmationShowsWarningAndCanBeDeferred() {
        show(listOf(sample().copy(remainingQuantity = 2, reservedQuantity = 3)))
        compose.onNodeWithText("복숭아 4입").performClick()
        compose.onAllNodesWithContentDescription("수량 감소")[0].performScrollTo().performClick()
        compose.onNodeWithText("저장하기").performClick()
        compose.onNodeWithText("지금 판매 가능한 수량이\n1개가 맞나요?").assertIsDisplayed()
        compose.onNodeWithText("재고가 찜된 수보다 부족해져요.").assertIsDisplayed()
        captureSheet("quantity-less")
        compose.onNodeWithText("네, 맞아요").performClick()
        compose.onNodeWithText("선착순을 기준으로 부족한 수량만큼 찜을 취소해야 해요.").assertIsDisplayed()
        compose.runOnIdle { assertEquals(2, saved.single().shortageQuantity) }
        captureSheet("reservation-cancel")
        compose.onNodeWithText("나중에 하기").performClick()
        compose.onNodeWithText("나중에 하기").assertDoesNotExist()
        compose.runOnIdle { assertEquals(emptyList<String>(), cancellations) }
    }

    private fun captureSheet(name: String) {
        compose.waitForIdle()
        val bitmap = InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
        File(compose.activity.getExternalFilesDir(null), "product-sheet-$name.png").outputStream().use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        bitmap.recycle()
    }

    @Test
    fun storeTabsAndFiltersShowCurrentPickupSamples() {
        show()
        compose.onNodeWithText("등록된 상품 1").assertIsDisplayed()
        compose.onNodeWithText("찜 현황 10").performClick()
        compose.onNodeWithText("픽업완료").performClick()
        compose.onNodeWithText("해당 상품 1개").assertIsDisplayed()
        compose.onNodeWithText("등록된 상품 1").performClick()
        compose.onNodeWithText("전체").performClick()
        compose.onNodeWithText("복숭아 4입").assertIsDisplayed()
    }

    @Test
    fun registrationDraftSurvivesSavedStateRestoration() {
        val restoration = StateRestorationTester(compose)
        restoration.setContent {
            MangroTheme(typography = OwnerMangroTypography) {
                ProductTestNavHost(emptyList(), "20:00", {}, {}, startInEditor = true)
            }
        }
        compose.onAllNodes(hasSetTextAction())[0].performScrollTo().performTextReplacement("복숭아 새 이름")
        restoration.emulateSavedInstanceStateRestore()
        compose.onNodeWithText("복숭아 새 이름").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("다음").assertIsNotEnabled()
    }

    @Composable
    private fun ProductTestNavHost(
        products: List<OwnerProductModel>,
        storeClosingTime: String,
        onSave: (List<OwnerProductModel>) -> Unit,
        onCancel: (List<String>) -> Unit,
        startInEditor: Boolean = false,
    ) {
        val navController = rememberNavController()
        NavHost(navController, startDestination = if (startInEditor) OwnerProductEditorDestination else OwnerProductListDestination) {
            ownerProductNavGraph(navController, products, onSave, onCancel, {}, {})
        }
    }

    private fun sample() = OwnerProductModel("peach", "복숭아 4입", listOf("android.resource://com.swyp.mangro.owner/drawable/ic_camera_add"), 10000, 4000, 10, 5, 3, 2, "20:00")
}
