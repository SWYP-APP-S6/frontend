package com.swyp.mangro

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.test.platform.app.InstrumentationRegistry
import com.swyp.mangro.core.designsystem.component.card.owner.OwnerPickupRequestItem
import com.swyp.mangro.core.designsystem.component.card.owner.OwnerPickupRequestStatus
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.core.designsystem.theme.OwnerMangroTypography
import com.swyp.mangro.feature.owner.product.model.OwnerPickupModel
import com.swyp.mangro.feature.owner.product.model.OwnerProductModel
import com.swyp.mangro.feature.owner.product.screen.list.ProductListAction
import com.swyp.mangro.feature.owner.product.screen.list.ProductListFilter
import com.swyp.mangro.feature.owner.product.screen.list.ProductListScreen
import com.swyp.mangro.feature.owner.product.screen.list.ProductListState
import com.swyp.mangro.feature.owner.product.screen.list.ProductListTab
import java.io.File
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** Figma 1731:4845, 1731:5136, 1731:5523. Fixtures never enter the production host. */
class OwnerStoreDesignTest {
    @get:Rule
    val compose = createAndroidComposeRule<ProductTestActivity>()
    private val actions = mutableListOf<ProductListAction>()

    private fun show(state: ProductListState) {
        compose.setContent {
            var current by remember { mutableStateOf(state) }
            val paging = remember(current.filter) {
                flowOf(PagingData.from(current.filteredPickups, LoadStates(LoadState.NotLoading(false), LoadState.NotLoading(true), LoadState.NotLoading(true))))
            }.collectAsLazyPagingItems()
            MangroTheme(typography = OwnerMangroTypography) {
                ProductListScreen(current.copy(filteredTotal = current.filteredPickups.size.toLong()), { action ->
                    actions += action
                    current = when (action) {
                        is ProductListAction.TabSelected -> current.copy(tab = action.tab)
                        is ProductListAction.FilterSelected -> current.copy(filter = action.filter)
                        else -> current
                    }
                }, pickups = paging)
            }
        }
    }

    @Test
    fun registeredProductsShowActualCancellationCountAndOpenDetail() {
        show(fixture())
        compose.onNodeWithText("등록된 상품 3").assertIsDisplayed()
        compose.onNodeWithText("취소가 필요한 찜이 2건 있어요.").assertIsDisplayed()
        screenshot("store-products")
        compose.onNodeWithText("복숭아 4입").performClick()
        compose.runOnIdle { assertEquals(ProductListAction.ProductClicked("1"), actions.last()) }
        compose.onNodeWithText("취소하기").performClick()
        compose.runOnIdle { assertEquals(ProductListAction.ReservationsCancelClicked, actions.last()) }
    }

    @Test
    fun pickupsShowStatusCardsAndForwardCompletionWithoutFakingSuccess() {
        show(fixture().copy(tab = ProductListTab.PICKUPS))
        compose.onNodeWithText("찜 현황 6").assertIsDisplayed()
        compose.onNodeWithText("픽업 완료했어요").performClick()
        compose.runOnIdle { assertEquals("new", (actions.last() as ProductListAction.PickupCompleteClicked).pickup.request.id) }
        compose.onNodeWithText("찜 만료").assertIsNotEnabled()
        screenshot("store-pickups")
        compose.onNodeWithText("픽업완료").performClick()
        compose.onNodeWithText("픽업 완료된 찜").assertIsNotEnabled()
        compose.onNodeWithText("해당 상품 1개").assertIsDisplayed()
    }

    @Test
    fun emptyFilteredPickupsKeepTabsAndCancellationNotice() {
        show(fixture().copy(tab = ProductListTab.PICKUPS, filter = ProductListFilter.COMPLETED, pickups = fixture().pickups.filter { it.request.status != OwnerPickupRequestStatus.COMPLETED }))
        compose.onNodeWithText("해당되는 상품이 없어요.").assertIsDisplayed()
        compose.onNodeWithText("해당 상품 0개").assertIsDisplayed()
        compose.onNodeWithText("취소가 필요한 찜이 2건 있어요.").assertIsDisplayed()
        screenshot("store-empty")
        compose.onNodeWithText("전체").performClick()
        compose.onNodeWithText("해당 상품 5개").assertIsDisplayed()
    }

    private fun screenshot(name: String) {
        compose.waitForIdle()
        val file = File(compose.activity.getExternalFilesDir(null), "$name.png")
        file.outputStream().use { compose.onRoot().captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    private fun fixture(): ProductListState {
        val products = listOf("복숭아 4입", "애호박", "콩나물 한 바구니").mapIndexed { index, name ->
            val file = File(compose.activity.cacheDir, "store_product_${index + 1}.png")
            InstrumentationRegistry.getInstrumentation().context.assets.open("figma/${file.name}").use { input -> file.outputStream().use { input.copyTo(it) } }
            OwnerProductModel("${index + 1}", name, listOf(file.toURI().toString()), 10000, 4000, 10, listOf(6, 2, 4)[index], listOf(4L, 3L, 3L)[index], 0, "20:00")
        }
        val now = System.currentTimeMillis()
        fun pickup(id: String, status: OwnerPickupRequestStatus, name: String, productId: String = "1", isNew: Boolean = false, timer: Boolean = false) = OwnerPickupModel(
            productId,
            OwnerPickupRequestItem(id, "09.02(수) 18시 20분", name, "18시 35분까지 픽업 예정", "복숭아 4입", 1, status, if (timer) now - 11 * 60_000 else null, if (timer) now + 4 * 60_000 else null, isNew),
        )
        return ProductListState(
            products = products,
            pickups = listOf(
                pickup("new", OwnerPickupRequestStatus.IN_PROGRESS, "닉네임최대몇글자까지", isNew = true),
                pickup("expired", OwnerPickupRequestStatus.EXPIRED, "양모펠트"),
                pickup("completed", OwnerPickupRequestStatus.COMPLETED, "윤지현", timer = true),
                pickup("cancelled", OwnerPickupRequestStatus.CANCELLED, "양모펠트", timer = true),
                pickup("unavailable1", OwnerPickupRequestStatus.UNAVAILABLE, "방문자1"),
                pickup("unavailable2", OwnerPickupRequestStatus.UNAVAILABLE, "방문자2", "3"),
            ),
        )
    }
}
