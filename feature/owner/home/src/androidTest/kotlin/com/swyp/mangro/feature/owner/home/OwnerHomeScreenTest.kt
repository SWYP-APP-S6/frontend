package com.swyp.mangro.feature.owner.home

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.core.designsystem.theme.OwnerMangroTypography
import com.swyp.mangro.feature.owner.home.screen.OwnerHomeScreen
import com.swyp.mangro.feature.owner.home.screen.model.OwnerHomeAction
import com.swyp.mangro.feature.owner.home.screen.model.OwnerHomeUiState
import com.swyp.mangro.feature.owner.home.screen.model.OwnerHomeVisitor
import java.io.File
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class OwnerHomeScreenTest {
    @get:Rule
    val compose = createComposeRule()

    private fun show(state: OwnerHomeUiState, onAction: (OwnerHomeAction) -> Unit = {}) {
        compose.setContent {
            MangroTheme(typography = OwnerMangroTypography) {
                val packageName = LocalContext.current.packageName
                val displayState = state.copy(
                    products = state.products.map {
                        it.copy(imageUrl = it.imageUrl.replace("com.swyp.mangro.feature.owner.home", packageName))
                    }.toPersistentList(),
                )
                OwnerHomeScreen(displayState, onAction, bottomBar = { })
            }
        }
    }

    private fun capture(name: String) {
        val directory = InstrumentationRegistry.getInstrumentation().targetContext.getExternalFilesDir("home-screenshots")!!
        directory.mkdirs()
        File(directory, "$name.png").outputStream().use {
            compose.onRoot().captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }

    @Test
    fun firstRegistrationOpensRegistration() {
        var action: OwnerHomeAction? = null
        show(OwnerHomeSamples.welcome) { action = it }
        compose.onNodeWithText("안녕하세요, 사장님!").assertIsDisplayed()
        capture("welcome")
        compose.onNodeWithText("첫 상품 등록하러 가기").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(OwnerHomeAction.RegisterProduct, action) }
    }

    @Test
    fun emptySectionsAndRegistrationRemainAccessible() {
        var action: OwnerHomeAction? = null
        show(OwnerHomeSamples.empty) { action = it }
        capture("empty")
        compose.onNodeWithText("지금은 방문 예정인 손님이 없어요.").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("상품 등록하기").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(OwnerHomeAction.RegisterProduct, action) }
    }

    @Test
    fun dismissingNoticeKeepsProblemStateAndDoesNotShowAllClear() {
        compose.setContent {
            var state by remember { mutableStateOf(OwnerHomeSamples.operating()) }
            MangroTheme(typography = OwnerMangroTypography) {
                OwnerHomeScreen(
                    state,
                    onAction = {
                        if (it == OwnerHomeAction.DismissAttention) state = state.copy(isAttentionDismissed = true)
                    },
                )
            }
        }
        compose.onNodeWithContentDescription("확인 필요 안내 닫기").performClick()
        compose.onNodeWithText("확인이 필요한 문제가 있어요.").assertDoesNotExist()
        compose.onNodeWithText("지금 확인해야 할 문제는 없어요.").assertDoesNotExist()
        compose.onNodeWithText("청과마을").assertIsDisplayed()
    }

    @Test
    fun pickupCompleteAndDetailSendCorrectId() {
        val actions = mutableListOf<OwnerHomeAction>()
        show(OwnerHomeSamples.operating().copy(hasNewPickup = false, cancellationRequiredCount = 0, needsPickupConfirmation = false), actions::add)
        compose.onNodeWithText("윤지현 님").performScrollTo().performClick()
        compose.onNodeWithContentDescription("윤지현 님 픽업 완료").performScrollTo().performClick()
        compose.runOnIdle {
            assertEquals(listOf(OwnerHomeAction.ViewPickup("pickup-1"), OwnerHomeAction.CompletePickup("pickup-1")), actions)
        }
    }

    @Test
    fun expiredPickupCannotBeCompleted() {
        show(
            OwnerHomeSamples.empty.copy(
                visitors = persistentListOf(
                    OwnerHomeVisitor(
                        "expired",
                        "만료손님",
                        "애호박",
                        1,
                        0,
                    ),
                ),
            ),
        )
        compose.onNodeWithContentDescription("만료손님 님 픽업 완료").performScrollTo().assertIsNotEnabled()
        compose.onNodeWithText("시간 만료").assertIsDisplayed()
    }

    @Test
    fun productSelectionSendsProductId() {
        var action: OwnerHomeAction? = null
        show(OwnerHomeSamples.operating().copy(visitors = persistentListOf())) { action = it }
        compose.onNodeWithTag("owner-home-list").performScrollToNode(hasText("애호박"))
        capture("products")
        compose.onNodeWithText("애호박").performClick()
        compose.runOnIdle { assertEquals(OwnerHomeAction.ViewProduct("product-2"), action) }
    }

    @Test
    fun alertsSendDistinctActions() {
        val actions = mutableListOf<OwnerHomeAction>()
        show(OwnerHomeSamples.operating(), actions::add)
        capture("operating")
        compose.onNodeWithText("확인하기").performClick()
        compose.onNodeWithText("취소 안내가 필요한 찜이 2건 있어요.").performClick()
        compose.onNodeWithText("찜 수령 여부를 확인해주세요.").performClick()
        compose.runOnIdle {
            assertEquals(listOf(OwnerHomeAction.ViewNewPickups, OwnerHomeAction.ViewCancellations, OwnerHomeAction.ConfirmPickups), actions)
        }
    }

    @Test
    fun smallScreenAndLargeFontKeepProductDetailsAccessible() {
        var action: OwnerHomeAction? = null
        val longName = "상품명은25자내외까지허용입니다그것을넘어가면안돼"
        val product = OwnerHomeSamples.operating().products.first()
        val packageName = InstrumentationRegistry.getInstrumentation().targetContext.packageName
        val state = OwnerHomeSamples.empty.copy(
            products = persistentListOf(
                product.copy(
                    name = longName,
                    imageUrl = product.imageUrl.replace("com.swyp.mangro.feature.owner.home", packageName),
                ),
            ),
        )
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, 1.3f)) {
                Box(Modifier.size(width = 320.dp, height = 640.dp)) {
                    MangroTheme(typography = OwnerMangroTypography) {
                        OwnerHomeScreen(state, { action = it }, bottomBar = { /*OwnerBottomAppBar(OwnerMenu.HOME, {})*/ })
                    }
                }
            }
        }
        compose.onNodeWithTag("owner-home-list").performScrollToNode(hasText(longName))
        compose.onNodeWithText(longName).assertIsDisplayed()
        capture("small-large-font")
        compose.onNodeWithText(longName).performClick()
        compose.runOnIdle { assertEquals(OwnerHomeAction.ViewProduct("product-1"), action) }
    }
}
