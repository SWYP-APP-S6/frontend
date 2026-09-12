package com.swyp.mangro.feature.owner.pickup

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.swyp.mangro.core.designsystem.R as DesignR
import com.swyp.mangro.core.designsystem.component.card.owner.OwnerPickupRequestCard
import com.swyp.mangro.core.designsystem.component.card.owner.OwnerPickupRequestItem
import com.swyp.mangro.core.designsystem.component.card.owner.OwnerPickupRequestStatus
import com.swyp.mangro.core.designsystem.component.card.timer.formatRemaining
import com.swyp.mangro.core.designsystem.component.chip.MangroChip
import com.swyp.mangro.core.designsystem.component.progress.MangroCircularProgress
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
internal fun PickupListScreen(
    pickups: List<Pickup>,
    totalCount: Int,
    filter: PickupFilter,
    now: Long,
    busy: Boolean,
    canComplete: (Pickup) -> Boolean,
    onFilterClick: (PickupFilter) -> Unit,
    onDetailClick: (Pickup) -> Unit,
    onCompleteClick: (Pickup) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Text(
            stringResource(R.string.pickup_title, totalCount),
            Modifier.fillMaxWidth().background(MangroTheme.colors.surfaceNormal).padding(20.dp),
            color = MangroTheme.colors.primaryNormal,
            style = MangroTheme.typography.title.titleM,
            textAlign = TextAlign.Center,
        )
        HorizontalDivider(color = MangroTheme.colors.primaryNormal)
        LazyRow(contentPadding = PaddingValues(20.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(PickupFilter.entries) { item ->
                MangroChip(
                    isOwner = true,
                    content = stringResource(
                        when (item) {
                            PickupFilter.ALL -> R.string.pickup_all
                            PickupFilter.COMPLETED -> R.string.pickup_completed
                            PickupFilter.UNAVAILABLE -> R.string.pickup_unavailable
                            PickupFilter.CANCELED -> R.string.pickup_canceled
                            PickupFilter.EXPIRED -> R.string.pickup_expired
                        },
                    ),
                    isSelected = item == filter,
                    onClick = { onFilterClick(item) },
                )
            }
        }
        Text(
            stringResource(R.string.pickup_count, pickups.size),
            Modifier.padding(horizontal = 20.dp).padding(bottom = 20.dp),
            color = MangroTheme.colors.textSubtitle,
            style = MangroTheme.typography.caption.captionS,
        )
        if (pickups.isEmpty()) {
            PickupEmpty(false, Modifier.weight(1f))
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(pickups, key = { it.id }) { pickup ->
                    val status = pickup.statusAt(now)
                    Column {
                        OwnerPickupRequestCard(
                            item = OwnerPickupRequestItem(
                                id = pickup.id,
                                requestedAt = pickupDate(pickup.requestedAt),
                                consumerName = pickup.customerName,
                                pickupDeadlineText = stringResource(R.string.pickup_deadline, pickupDate(pickup.deadline, "H시 mm분")),
                                productName = pickup.productName,
                                quantity = pickup.quantity,
                                status = when (status) {
                                    PickupStatus.WAITING -> OwnerPickupRequestStatus.IN_PROGRESS
                                    PickupStatus.COMPLETED -> OwnerPickupRequestStatus.COMPLETED
                                    PickupStatus.UNAVAILABLE -> OwnerPickupRequestStatus.UNAVAILABLE
                                    PickupStatus.CANCELED -> OwnerPickupRequestStatus.CANCELED
                                    PickupStatus.EXPIRED -> OwnerPickupRequestStatus.EXPIRED
                                },
                                requestTimeMillis = pickup.requestedAt.takeIf { status == PickupStatus.WAITING },
                                endTimeMillis = pickup.deadline.takeIf { status == PickupStatus.WAITING },
                            ),
                            onConsumerClick = { onDetailClick(pickup) },
                            onButtonClick = { onCompleteClick(pickup) },
                            enabled = !busy,
                            completeEnabled = canComplete(pickup),
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun PickupDetailScreen(pickup: Pickup, storeName: String, now: Long, modifier: Modifier = Modifier) {
    val status = pickup.statusAt(now)
    val remaining = if (status == PickupStatus.WAITING) (pickup.deadline - now).coerceAtLeast(0) else 0L
    Column(modifier.verticalScroll(rememberScrollState()).padding(20.dp)) {
        Text(
            when (status) {
                PickupStatus.WAITING -> stringResource(R.string.pickup_deadline, pickupDate(pickup.deadline, "M월 d일 H시 mm분"))
                PickupStatus.COMPLETED -> pickup.completedAt?.let { stringResource(R.string.pickup_complete_heading, pickupDate(it, "M월 d일 H시 mm분")) } ?: stringResource(R.string.pickup_complete_done)
                PickupStatus.EXPIRED -> stringResource(R.string.pickup_expired_heading)
                else -> pickupActionText(status)
            },
            color = MangroTheme.colors.primaryNormal,
            style = MangroTheme.typography.label.labelM,
        )
        Spacer(Modifier.height(36.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(painterResource(DesignR.drawable.ic_store_front), null, Modifier.size(16.dp), tint = MangroTheme.colors.textSubtitle)
                    Text(storeName, color = MangroTheme.colors.textSubtitle, style = MangroTheme.typography.caption.captionS)
                }
                Text(stringResource(R.string.pickup_customer, pickup.customerName), color = MangroTheme.colors.textTitle, style = MangroTheme.typography.title.titleL)
                Text(stringResource(R.string.pickup_requested, pickupDate(pickup.requestedAt)), color = MangroTheme.colors.textSubtitle, style = MangroTheme.typography.caption.captionS)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.size(92.dp).padding(5.dp), contentAlignment = Alignment.Center) {
                    MangroCircularProgress(
                        if (status == PickupStatus.WAITING) 1f - remaining.toFloat() / (pickup.deadline - pickup.requestedAt) else 0f,
                        status == PickupStatus.WAITING,
                        Modifier.fillMaxSize(),
                    )
                    Text(formatRemaining(remaining), style = MangroTheme.typography.title.titleL, color = MangroTheme.colors.textTitle)
                }
                Text(stringResource(R.string.pickup_remaining), style = MangroTheme.typography.caption.captionS, color = MangroTheme.colors.textCanceled)
            }
        }
        Spacer(Modifier.height(32.dp))
        HorizontalDivider(color = MangroTheme.colors.borderDefault)
        Spacer(Modifier.height(32.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            Text(stringResource(R.string.pickup_product), style = MangroTheme.typography.label.labelM, color = MangroTheme.colors.textTitle)
            Text(stringResource(R.string.pickup_quantity, pickup.productName, pickup.quantity), Modifier.weight(1f), style = MangroTheme.typography.body.bodyM, color = MangroTheme.colors.textBody)
        }
        Spacer(Modifier.height(20.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.pickup_total), Modifier.weight(1f), style = MangroTheme.typography.label.labelM, color = MangroTheme.colors.textTitle)
            Text(stringResource(R.string.pickup_price, NumberFormat.getIntegerInstance(Locale.KOREA).format(pickup.totalPrice)), style = MangroTheme.typography.heading.headingM, color = MangroTheme.colors.primaryNormal)
        }
    }
}

@Composable
internal fun PickupEmpty(cancellation: Boolean, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        if (cancellation) Image(painterResource(R.drawable.pickup_empty), null, Modifier.size(48.dp)) else Icon(painterResource(DesignR.drawable.ic_error), null, Modifier.size(48.dp), tint = MangroTheme.colors.textCanceled)
        Spacer(Modifier.height(12.dp))
        Text(
            stringResource(if (cancellation) R.string.pickup_cancel_empty else R.string.pickup_empty),
            Modifier.padding(horizontal = 20.dp),
            color = MangroTheme.colors.textSubtitle,
            style = MangroTheme.typography.body.bodyM,
            textAlign = TextAlign.Center,
        )
    }
}

internal fun pickupDate(millis: Long, pattern: String = "MM.dd(E) H시 mm분"): String = SimpleDateFormat(pattern, Locale.KOREA).format(Date(millis))
