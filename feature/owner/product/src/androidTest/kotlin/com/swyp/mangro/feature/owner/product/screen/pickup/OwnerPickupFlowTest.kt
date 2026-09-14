package com.swyp.mangro.feature.owner.product.screen.pickup

import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import androidx.test.platform.app.InstrumentationRegistry
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.core.designsystem.theme.OwnerMangroTypography
import com.swyp.mangro.feature.owner.product.data.OwnerPickupStore
import com.swyp.mangro.feature.owner.product.data.PickupSnapshot
import com.swyp.mangro.feature.owner.product.model.Pickup
import com.swyp.mangro.feature.owner.product.model.PickupStatus
import com.swyp.mangro.feature.owner.product.screen.list.OwnerProductListDestination
import com.swyp.mangro.feature.owner.product.screen.list.ProductListRoute
import com.swyp.mangro.feature.owner.product.screen.list.ProductListViewModel
import com.swyp.mangro.feature.owner.product.screen.pickup.cancellation.OwnerPickupCancellationDestination
import com.swyp.mangro.feature.owner.product.screen.pickup.cancellation.PickupCancellationRoute
import com.swyp.mangro.feature.owner.product.screen.pickup.cancellation.PickupCancellationViewModel
import com.swyp.mangro.feature.owner.product.screen.pickup.detail.OwnerPickupDetailDestination
import com.swyp.mangro.feature.owner.product.screen.pickup.detail.PickupDetailRoute
import com.swyp.mangro.feature.owner.product.screen.pickup.detail.PickupDetailViewModel
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class OwnerPickupFlowTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private val now = System.currentTimeMillis()
    private fun pickup(id: String, product: String, minutesAgo: Int, quantity: Int = 1) = Pickup(
        id = id,
        productId = product,
        productName = if (product == "peach") "복숭아 4입" else "아오리사과 6입(특)",
        customerName = id,
        quantity = quantity,
        unitPrice = 30000,
        requestedAt = now - minutesAgo * 60_000,
        deadline = now + (15 - minutesAgo) * 60_000,
        notificationsEnabled = id != "송유나",
        isNew = id == "닉네임최대몇글자까지",
    )

    @Composable
    private inline fun <reified T : ViewModel> model(key: String, crossinline create: () -> T): T = remember(key) {
        ViewModelProvider(
            compose.activity,
            object : ViewModelProvider.Factory {
                override fun <V : ViewModel> create(modelClass: Class<V>): V {
                    @Suppress("UNCHECKED_CAST")
                    return create() as V
                }
            },
        )[key, T::class.java]
    }

    private fun show(store: OwnerPickupStore, cancellation: Boolean = false) {
        compose.setContent {
            MangroTheme(typography = OwnerMangroTypography) {
                val nav = rememberNavController()
                NavHost(nav, startDestination = if (cancellation) OwnerPickupCancellationDestination() else OwnerProductListDestination()) {
                    composable<OwnerProductListDestination> {
                        ProductListRoute(
                            products = emptyList(),
                            onSelect = {},
                            onMenuClick = {},
                            onPickupClick = { nav.navigate(OwnerPickupDetailDestination(it)) },
                            onCancelReservations = { nav.navigate(OwnerPickupCancellationDestination()) },
                            viewModel = model("list") { ProductListViewModel(SavedStateHandle(mapOf("store_tab" to "PICKUPS")), store) },
                        )
                    }
                    composable<OwnerPickupDetailDestination> { entry ->
                        val id = entry.toRoute<OwnerPickupDetailDestination>().pickupId
                        PickupDetailRoute(
                            navigateBack = { nav.popBackStack() },
                            navigateToHome = { nav.popBackStack() },
                            viewModel = model("detail-$id") { PickupDetailViewModel(SavedStateHandle(mapOf("pickupId" to id)), store) },
                        )
                    }
                    composable<OwnerPickupCancellationDestination> {
                        PickupCancellationRoute(
                            navigateBack = { nav.popBackStack() },
                            viewModel = model("cancel") { PickupCancellationViewModel(SavedStateHandle(), store) },
                        )
                    }
                }
            }
        }
    }

    @Test
    fun listDetailCompletionAndEmptyFilterShareState() {
        val store = OwnerPickupStore(PickupSnapshot(listOf(pickup("닉네임최대몇글자까지", "peach", 7)), mapOf("peach" to 1), "청과 마을", "02-123-4567", true))
        show(store)
        compose.waitUntil(5_000) { compose.onAllNodesWithText("닉네임최대몇글자까지님").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("닉네임최대몇글자까지님").assertIsDisplayed()
        capture("01-list")
        compose.onNodeWithText("픽업완료", useUnmergedTree = true).performClick()
        compose.onNodeWithText("해당되는 상품이 없어요.").assertIsDisplayed()
        capture("02-list-empty")
        compose.onNodeWithText("전체", useUnmergedTree = true).performClick()
        compose.onNodeWithText("닉네임최대몇글자까지님").performClick()
        compose.onNodeWithText("찜 상세").assertIsDisplayed()
        compose.onNodeWithText("30,000원").assertIsDisplayed()
        capture("03-detail")
        compose.onNodeWithText("픽업 완료했어요").performClick()
        compose.onNodeWithText("픽업 완료된 찜").assertIsNotEnabled()
        compose.onNodeWithText("00:00").assertIsDisplayed()
        capture("04-detail-completed")
        compose.onNodeWithContentDescription("뒤로").performClick()
        compose.onNodeWithText("픽업완료", useUnmergedTree = true).performClick()
        compose.onNodeWithText("닉네임최대몇글자까지님").assertIsDisplayed()
        compose.runOnIdle { assertEquals(0, store.snapshot.value.stock["peach"]) }
    }

    @Test
    fun expiredDetailDisablesCompletionAndAllowsReturningHome() {
        val store = OwnerPickupStore(
            PickupSnapshot(
                pickups = listOf(pickup("만료손님", "peach", 16)),
                stock = mapOf("peach" to 1),
                storeName = "청과 마을",
                isDemo = true,
            ),
        )
        show(store)
        compose.waitUntil(5_000) { compose.onAllNodesWithText("만료손님님").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("만료손님님").performClick()
        compose.onNodeWithText("주문 만료").assertIsDisplayed()
        compose.onNodeWithText("주문 만료된 찜").assertIsNotEnabled()
        compose.onNodeWithText("00:00").assertIsDisplayed()
        capture("08-detail-expired")
        compose.onNodeWithText("홈으로 돌아가기").performClick()
        compose.onNodeWithText("만료손님님").assertIsDisplayed()
        compose.runOnIdle {
            assertEquals(1, store.snapshot.value.stock["peach"])
            assertEquals(PickupStatus.EXPIRED, store.snapshot.value.pickups.single().statusAt(System.currentTimeMillis()))
        }
    }

    @Test
    fun cancellationSelectionSheetDismissalAndSubmission() {
        val store = OwnerPickupStore(
            PickupSnapshot(
                listOf(pickup("배정손님", "peach", 10), pickup("송유나", "peach", 8, 2), pickup("윤지현", "peach", 6), pickup("배정손님2", "apple", 10), pickup("건우건어물", "apple", 8, 2), pickup("맛있으면짖는개", "apple", 6)),
                mapOf("peach" to 1, "apple" to 1),
                "맹그로청과",
                "02-123-4567",
                true,
            ),
        )
        show(store, cancellation = true)
        compose.onNodeWithText("4건 취소하기").assertIsDisplayed()
        compose.onNodeWithText("송유나 님 * 2").assertIsDisplayed()
        capture("05-cancellation")
        compose.onNodeWithText("윤지현 님 * 1").performClick()
        compose.onNodeWithText("3건 취소하기").performClick()
        compose.onNodeWithText("안내 메시지 미리보기").assertIsDisplayed()
        compose.onNodeWithText("돌아가기").performClick()
        compose.onNodeWithText("3건 취소하기").assertIsDisplayed()
        compose.onNodeWithText("윤지현 님 * 1").performClick()
        compose.onNodeWithText("4건 취소하기").performClick()
        compose.onNodeWithText("총 4건의 찜을 취소할까요?").assertIsDisplayed()
        capture("06-confirmation")
        compose.onNodeWithText("취소하고 안내 보내기").performClick()
        compose.onNodeWithText("지금은 취소해야 할 주문이 없어요.").assertIsDisplayed()
        compose.onNodeWithText("0건 취소하기").assertIsNotEnabled()
        capture("07-cancellation-empty")
        compose.runOnIdle { assertEquals(4, store.snapshot.value.pickups.count { it.status == PickupStatus.UNAVAILABLE }) }
    }

    private fun capture(name: String) {
        compose.waitForIdle()
        // Navigation 전환 후 실제 디스플레이 프레임이 반영된 뒤 캡처한다.
        Thread.sleep(500)
        val file = File(compose.activity.getExternalFilesDir(null), "pickup-$name.png")
        file.outputStream().use { InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot().compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
}
