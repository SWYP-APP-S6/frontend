package com.swyp.mangro.feature.owner.product.screen.pickup.detail

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.swyp.mangro.core.designsystem.R as DesignR
import com.swyp.mangro.core.designsystem.component.MangroButton
import com.swyp.mangro.core.designsystem.component.MangroButtonStyle
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.feature.owner.product.R
import com.swyp.mangro.feature.owner.product.component.OwnerProductScaffold
import com.swyp.mangro.feature.owner.product.component.pickup.PickupTimer
import com.swyp.mangro.feature.owner.product.model.PickupStatus
import com.swyp.mangro.feature.owner.product.model.pickupDate
import java.text.NumberFormat
import java.util.Locale
import kotlinx.serialization.Serializable

@Serializable
data class OwnerPickupDetailDestination(val pickupId: String)

@Composable
internal fun PickupDetailRoute(
    navigateBack: () -> Unit,
    navigateToHome: () -> Unit,
    viewModel: PickupDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    BackHandler { viewModel.handleAction(PickupDetailAction.NavigationBackClicked) }
    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                PickupDetailEvent.NavigateBack -> navigateBack()
                PickupDetailEvent.NavigateToHome -> navigateToHome()
            }
        }
    }

    PickupDetailScreen(
        uiState = state,
        onAction = viewModel::handleAction,
    )
}

@Composable
internal fun PickupDetailScreen(
    uiState: PickupDetailState,
    onAction: (PickupDetailAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val pickup = uiState.pickup
    val isExpired = uiState.status == PickupStatus.EXPIRED

    OwnerProductScaffold(
        title = stringResource(R.string.pickup_detail),
        onBack = { onAction(PickupDetailAction.NavigationBackClicked) },
        modifier = modifier,
        contentSpacing = 0.dp,
        bottomBarContent = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (uiState.hasError) {
                    Text(
                        text = stringResource(R.string.pickup_error),
                        color = MangroTheme.colors.dangerNormal,
                    )
                }
                if (pickup != null) {
                    MangroButton(
                        text = stringResource(
                            when (uiState.status) {
                                PickupStatus.WAITING -> R.string.pickup_complete_action
                                PickupStatus.COMPLETED -> R.string.pickup_complete_done
                                PickupStatus.CANCELED -> R.string.pickup_canceled
                                PickupStatus.UNAVAILABLE -> R.string.pickup_unavailable
                                else -> R.string.pickup_expired_done
                            },
                        ),
                        onClick = { onAction(PickupDetailAction.CompleteClicked) },
                        style = MangroButtonStyle.OUTLINED,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uiState.canComplete,
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                        disabledContainerColor = MangroTheme.colors.surfaceAlter,
                    )
                }
                MangroButton(
                    onClick = { onAction(PickupDetailAction.HomeClicked) },
                    style = MangroButtonStyle.TEXT,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = stringResource(R.string.pickup_home),
                        color = if (isExpired) MangroTheme.colors.textCanceled else MangroTheme.colors.primaryNormal,
                        style = if (isExpired) MangroTheme.typography.body.bodyM else MangroTheme.typography.title.titleM,
                    )
                }
            }
        },
    ) {
        if (pickup == null) {
            Text(
                text = stringResource(R.string.pickup_empty),
                modifier = Modifier.padding(vertical = 32.dp),
                color = MangroTheme.colors.textSubtitle,
            )
        } else {
            Text(
                text = when (uiState.status) {
                    PickupStatus.WAITING -> stringResource(R.string.pickup_detail_deadline, pickupDate(pickup.deadline, "M월 d일 H시 mm분"))
                    PickupStatus.COMPLETED -> stringResource(R.string.pickup_complete_heading, pickupDate(pickup.completedAt ?: pickup.deadline, "M월 d일 H시 mm분"))
                    PickupStatus.CANCELED -> stringResource(R.string.pickup_canceled)
                    PickupStatus.UNAVAILABLE -> stringResource(R.string.pickup_unavailable)
                    else -> stringResource(R.string.pickup_expired_heading)
                },
                modifier = Modifier.padding(top = 24.dp),
                color = MangroTheme.colors.primaryNormal,
                style = MangroTheme.typography.title.titleS ?: MangroTheme.typography.title.titleM,
            )
            Spacer(Modifier.height(28.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            painter = painterResource(DesignR.drawable.ic_store_front),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MangroTheme.colors.textSubtitle,
                        )
                        Text(
                            uiState.storeName,
                            style = MangroTheme.typography.caption.captionS,
                            color = MangroTheme.colors.textSubtitle,
                        )
                    }
                    Text(
                        text = stringResource(R.string.pickup_customer, pickup.customerName),
                        style = MangroTheme.typography.heading.headingXXS,
                        color = MangroTheme.colors.textTitle,
                    )
                    Text(
                        text = stringResource(R.string.pickup_requested, pickupDate(pickup.requestedAt)),
                        style = MangroTheme.typography.body.bodyM,
                        color = MangroTheme.colors.textSubtitle,
                    )
                }
                PickupTimer(
                    uiState.remaining,
                    pickup.deadline - pickup.requestedAt,
                    uiState.status == PickupStatus.WAITING,
                    inactiveColor = if (isExpired) Color(0xFFEAEAEA) else Color(0xFFFFE8A3),
                )
            }
            Spacer(Modifier.height(32.dp))
            HorizontalDivider(color = MangroTheme.colors.borderDefault)
            Spacer(Modifier.height(32.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Text(
                    text = stringResource(R.string.pickup_product),
                    style = MangroTheme.typography.title.titleS ?: MangroTheme.typography.title.titleM,
                    color = MangroTheme.colors.textTitle,
                )
                Text(
                    text = stringResource(R.string.pickup_quantity, pickup.productName, pickup.quantity),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End,
                    style = MangroTheme.typography.body.bodyM,
                    color = MangroTheme.colors.textBody,
                )
            }
            Spacer(Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.pickup_total),
                    modifier = Modifier.weight(1f),
                    style = MangroTheme.typography.title.titleS ?: MangroTheme.typography.title.titleM,
                    color = MangroTheme.colors.textTitle,
                )
                Text(
                    text = stringResource(R.string.pickup_price, NumberFormat.getIntegerInstance(Locale.KOREA).format(pickup.totalPrice)),
                    style = MangroTheme.typography.heading.headingM,
                    color = MangroTheme.colors.primaryNormal,
                )
            }
        }
    }
}
