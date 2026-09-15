package com.swyp.mangro.feature.consumer.hold.hold

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.swyp.mangro.core.designsystem.R as dsR
import com.swyp.mangro.core.designsystem.component.MangroButton
import com.swyp.mangro.core.designsystem.component.MangroButtonStyle
import com.swyp.mangro.core.designsystem.component.appbar.MangroDefaultStartAlignedTopAppBar
import com.swyp.mangro.core.designsystem.component.card.store.StoreLocationCard
import com.swyp.mangro.core.designsystem.component.card.timer.TimerCard
import com.swyp.mangro.core.designsystem.component.card.timer.TimerCardPhase
import com.swyp.mangro.core.designsystem.component.card.wishlist.WishProductCard
import com.swyp.mangro.core.designsystem.theme.Gray900
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.feature.consumer.hold.R
import com.swyp.mangro.feature.consumer.hold.component.CancelledNoticeCard
import com.swyp.mangro.feature.consumer.hold.component.StoreInfoCard

@Composable
fun HoldScreen(
    uiState: HoldUiState,
    onAction: (HoldUiAction) -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val wishedProducts = uiState.wishedProducts
    if (wishedProducts.isEmpty()) return
    val storeInfo = uiState.storeInfo ?: return

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentColor = MangroTheme.colors.surfaceNormal,
        topBar = {
            MangroDefaultStartAlignedTopAppBar(
                modifier = Modifier.fillMaxWidth(),
                title = {
                    Text(
                        text = stringResource(R.string.hold_title),
                        style = MangroTheme.typography.title.titleM,
                        color = MangroTheme.colors.grayScale900,
                    )
                },
                actions = {
                    Icon(
                        imageVector = ImageVector.vectorResource(dsR.drawable.ic_x_24px),
                        contentDescription = stringResource(R.string.hold_title),
                        tint = Gray900,
                        modifier = Modifier.size(24.dp).clickable(onClick = onCloseClick),
                    )
                },
            )
        },
        bottomBar = {
            val isExpired = uiState.timerPhase == TimerCardPhase.EXPIRED

            val buttonTextRes = when {
                uiState.isCancelled -> R.string.hold_view_other_products_button
                isExpired -> R.string.hold_retry_button
                else -> R.string.hold_cancel_button
            }

            val buttonAction = when {
                uiState.isCancelled -> HoldUiAction.OnViewOtherProductsClick
                isExpired -> HoldUiAction.OnRetryClick
                else -> HoldUiAction.OnCancelClick
            }

            MangroButton(
                text = stringResource(buttonTextRes),
                style = MangroButtonStyle.ACTIVE,
                onClick = { onAction(buttonAction) },
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(top = 20.dp, bottom = 32.dp, start = 20.dp, end = 20.dp),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                if (uiState.isCancelled) {
                    CancelledNoticeCard(storeName = storeInfo.name)
                } else {
                    Text(
                        text = stringResource(R.string.hold_description),
                        style = MangroTheme.typography.title.titleM,
                        color = MangroTheme.colors.textTitle,
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = stringResource(R.string.hold_notice),
                        style = MangroTheme.typography.caption.captionS,
                        color = MangroTheme.colors.textBody,
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    TimerCard(
                        requestTimeMillis = uiState.requestTimeMillis,
                        endTimeMillis = uiState.endTimeMillis,
                        onPhaseChange = { onAction(HoldUiAction.OnTimerPhaseChange(it)) },
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(R.string.hold_store_info_title),
                    style = MangroTheme.typography.title.titleM,
                    color = MangroTheme.colors.textTitle,
                    modifier = Modifier.padding(horizontal = 6.dp),
                )

                Spacer(modifier = Modifier.height(12.dp))

                StoreLocationCard(
                    imageUrl = "",
                    onDirectionsClick = { onAction(HoldUiAction.OnDirectionsClick) },
                )

                Spacer(modifier = Modifier.height(12.dp))

                StoreInfoCard(
                    storeName = storeInfo.name,
                    address = storeInfo.address,
                    phoneNumber = storeInfo.phoneNumber,
                    operatingStatusText = storeInfo.closingTime,
                    onCopyAddressClick = { onAction(HoldUiAction.OnCopyAddressClick) },
                    onCallClick = { onAction(HoldUiAction.OnCallClick) },
                )

                Spacer(modifier = Modifier.height(10.dp))
            }

            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(MangroTheme.colors.surfaceAlter),
            )

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(
                        if (uiState.isCancelled) {
                            R.string.hold_wished_product_past_title
                        } else {
                            R.string.hold_wished_product_title
                        },
                    ),
                    style = MangroTheme.typography.label.labelL,
                    color = MangroTheme.colors.textTitle,
                )

                wishedProducts.forEach { product ->
                    WishProductCard(product = product)
                }

                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HoldScreenDefaultPreview() {
    MangroTheme {
        HoldScreen(uiState = dummyHoldUiState, onAction = {}, onCloseClick = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun HoldScreenCautionPreview() {
    MangroTheme {
        HoldScreen(uiState = dummyHoldUiStateCaution, onAction = {}, onCloseClick = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun HoldScreenExpiredPreview() {
    MangroTheme {
        HoldScreen(uiState = dummyHoldUiStateExpired, onAction = {}, onCloseClick = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun HoldScreenCancelledPreview() {
    MangroTheme {
        HoldScreen(uiState = dummyHoldUiStateCancelled, onAction = {}, onCloseClick = {})
    }
}
