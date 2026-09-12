package com.swyp.mangro

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.swyp.mangro.feature.owner.pickup.OwnerPickupRoute
import com.swyp.mangro.feature.owner.pickup.Pickup
import com.swyp.mangro.feature.owner.pickup.PickupStatus
import com.swyp.mangro.theme.MangroTheme
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OwnerPickupFlowTest {
    @get:Rule
    val compose = createComposeRule()

    private fun pickup(id: String = "1", name: String = "테스트손님", quantity: Int = 1, status: PickupStatus = PickupStatus.WAITING): Pickup {
        val now = System.currentTimeMillis()
        return Pickup(id, "peach", "복숭아 4입", name, quantity, 4_000, now - 60_000, now + 600_000, status)
    }

    @Test
    fun completeFromDetailUpdatesListAndReturnsHome() {
        var homeCount = 0
        var completionCount = 0
        val initial = pickup(quantity = 2)
        compose.setContent {
            var pickups by remember { mutableStateOf(listOf(initial)) }
            MangroTheme {
                OwnerPickupRoute(pickups, mapOf("peach" to 2), "청과 마을", "02-123-4567", {
                    completionCount++
                    pickups = pickups.map { it.copy(status = PickupStatus.COMPLETED) }
                }, { _, _ -> }, { homeCount++ })
            }
        }
        compose.onNodeWithText("테스트손님님").performClick()
        compose.onNodeWithText("8,000원").assertIsDisplayed()
        compose.onNodeWithText("픽업 완료했어요").performClick()
        compose.onNodeWithText("홈으로 돌아가기").assertIsDisplayed()
        compose.onNodeWithContentDescription("뒤로 가기").performClick()
        compose.onNodeWithText("픽업완료").performClick()
        compose.onNodeWithText("테스트손님님").assertIsDisplayed()
        compose.onNodeWithText("테스트손님님").performClick()
        compose.onNodeWithText("홈으로 돌아가기").performClick()
        compose.onNodeWithText("찜 현황 1").assertIsDisplayed()
        compose.runOnIdle {
            assertEquals(1, completionCount)
            assertEquals(1, homeCount)
        }
    }

    @Test
    fun cancelPreviewSelectionAndSubmissionReachEmptyState() {
        val initial = pickup()
        var submittedIds = emptySet<String>()
        var sentMessage = ""
        compose.setContent {
            var pickups by remember { mutableStateOf(listOf(initial)) }
            MangroTheme {
                OwnerPickupRoute(pickups, mapOf("peach" to 0), "청과 마을", "02-123-4567", {}, { ids, message ->
                    submittedIds = ids
                    sentMessage = message
                    pickups = pickups.map { it.copy(status = PickupStatus.UNAVAILABLE) }
                }, {})
            }
        }
        compose.onNodeWithText("취소하기").performClick()
        compose.onNodeWithContentDescription("테스트손님 님 찜 취소 선택").performClick()
        compose.onNodeWithText("0건 취소하기").assertIsNotEnabled()
        compose.onNodeWithContentDescription("테스트손님 님 찜 취소 선택").performClick()
        compose.onNodeWithText("1건 취소하기").performClick()
        compose.onNodeWithText("안내 메시지 미리보기").assertIsDisplayed()
        compose.onNodeWithText("돌아가기").performScrollTo().performClick()
        compose.onNodeWithText("1건 취소하기").performClick()
        compose.onNodeWithText("취소하고 안내 보내기").performScrollTo().performClick()
        compose.onNodeWithText("지금은 취소해야 할 주문이 없어요.").assertIsDisplayed()
        compose.onNodeWithText("0건 취소하기").assertIsNotEnabled()
        compose.runOnIdle {
            assertEquals(setOf("1"), submittedIds)
            assertEquals("[청과 마을] 죄송합니다. 매장 재고 부족으로 인해 찜이 취소되었습니다. 결제된 금액은 없습니다. 02-123-4567", sentMessage)
        }
    }

    @Test
    fun requestFailureKeepsConfirmationAndAllowsRetry() {
        var attempts = 0
        compose.setContent {
            MangroTheme {
                OwnerPickupRoute(listOf(pickup()), mapOf("peach" to 0), "청과 마을", "02-123-4567", {}, { _, _ ->
                    attempts++
                    error("Test failure")
                }, {})
            }
        }
        compose.onNodeWithText("취소하기").performClick()
        compose.onNodeWithText("1건 취소하기").performClick()
        compose.onNodeWithText("취소하고 안내 보내기").performScrollTo().performClick()
        compose.onNodeWithText("총 1건의 찜을 취소할까요?").assertExists()
        compose.onNodeWithText("취소하고 안내 보내기").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(2, attempts) }
    }

    @Test
    fun processingDisablesRepeatedPickup() {
        val completion = CompletableDeferred<Unit>()
        var calls = 0
        val initial = pickup()
        compose.setContent {
            MangroTheme {
                OwnerPickupRoute(listOf(initial), mapOf("peach" to 1), "청과 마을", "", {
                    calls++
                    completion.await()
                }, { _, _ -> }, {})
            }
        }
        compose.onNodeWithText("테스트손님님").performClick()
        compose.onNodeWithText("픽업 완료했어요").performClick()
        compose.onNodeWithText("처리 중…").assertIsNotEnabled()
        compose.onNodeWithText("홈으로 돌아가기").assertIsNotEnabled()
        compose.runOnIdle {
            assertEquals(1, calls)
            completion.complete(Unit)
        }
    }

    @Test
    fun expiredRequestIsDisabledAndEmptyFilterWorks() {
        val now = System.currentTimeMillis()
        val expired = pickup().copy(requestedAt = now - 20_000, deadline = now - 1)
        compose.setContent {
            MangroTheme { OwnerPickupRoute(listOf(expired), mapOf("peach" to 1), "청과 마을", "", {}, { _, _ -> }, {}) }
        }
        compose.onNodeWithText("테스트손님님").performClick()
        compose.onNodeWithText("주문 만료").assertIsDisplayed()
        compose.onNodeWithText("주문 만료된 찜").assertIsNotEnabled()
        compose.onNodeWithContentDescription("뒤로 가기").performClick()
        compose.onNodeWithText("픽업완료").performClick()
        compose.onNodeWithText("해당되는 상품이 없어요.").assertIsDisplayed()
        compose.onNodeWithText("테스트손님님").assertDoesNotExist()
    }
}
