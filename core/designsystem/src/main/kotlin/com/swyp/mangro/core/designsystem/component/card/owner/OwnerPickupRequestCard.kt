package com.swyp.mangro.core.designsystem.component.card.owner

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.swyp.mangro.core.designsystem.R
import com.swyp.mangro.core.designsystem.component.MangroButton
import com.swyp.mangro.core.designsystem.component.MangroButtonStyle
import com.swyp.mangro.core.designsystem.component.card.timer.formatRemaining
import com.swyp.mangro.core.designsystem.component.card.timer.rememberTimerCardState
import com.swyp.mangro.core.designsystem.component.progress.MangroCircularProgress
import com.swyp.mangro.core.designsystem.theme.Gray500
import com.swyp.mangro.core.designsystem.theme.MangroTheme

enum class OwnerPickupRequestStatus { EXPIRED, IN_PROGRESS, COMPLETED, UNAVAILABLE, CANCELED }

data class OwnerPickupRequestItem(
    val id: String,
    val requestedAt: String,
    val consumerName: String,
    val pickupDeadlineText: String,
    val productName: String,
    val quantity: Int,
    val status: OwnerPickupRequestStatus,
    val requestTimeMillis: Long? = null,
    val endTimeMillis: Long? = null,
)

@Composable
fun OwnerPickupRequestCard(
    item: OwnerPickupRequestItem,
    onConsumerClick: () -> Unit,
    onButtonClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    completeEnabled: Boolean = true,
) {
    val timerState = if (item.requestTimeMillis != null && item.endTimeMillis != null) {
        rememberTimerCardState(item.requestTimeMillis, item.endTimeMillis)
    } else {
        null
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MangroTheme.colors.surfaceNormal)
            .padding(
                vertical = 20.dp,
                horizontal = 24.dp,
            ),
    ) {
        Text(
            text = item.requestedAt,
            color = MangroTheme.colors.textSubtitle,
            style = MangroTheme.typography.caption.captionS,
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(
                modifier = Modifier
                    .weight(1f),
                verticalArrangement = Arrangement.Center,
            ) {
                Row(
                    modifier = Modifier
                        .clickable(
                            enabled = enabled,
                            onClick = onConsumerClick,
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = stringResource(R.string.pickup_consumer_name, item.consumerName),
                        color = MangroTheme.colors.textTitle,
                        style = MangroTheme.typography.heading.headingXXS,
                    )

                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_chevron_right),
                        contentDescription = null,
                        tint = Gray500,
                        modifier = Modifier.size(24.dp),
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = item.pickupDeadlineText,
                    color = MangroTheme.colors.primaryNormal,
                    style = MangroTheme.typography.caption.captionM ?: MangroTheme.typography.caption.captionS,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = item.productName,
                        color = MangroTheme.colors.textBody,
                        style = MangroTheme.typography.body.bodyM,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )

                    Text(
                        text = "*",
                        color = MangroTheme.colors.textBody,
                        style = MangroTheme.typography.body.bodyM,
                    )

                    Text(
                        text = item.quantity.toString(),
                        color = MangroTheme.colors.textBody,
                        style = MangroTheme.typography.body.bodyM,
                    )
                }
            }

            if (timerState != null) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        MangroCircularProgress(
                            progress = 1f - timerState.progress,
                            isActive = item.status == OwnerPickupRequestStatus.IN_PROGRESS,
                            modifier = Modifier.fillMaxSize(),
                        )

                        Text(
                            text = formatRemaining(timerState.remainingMillis),
                            color = MangroTheme.colors.textTitle,
                            style = MangroTheme.typography.title.titleS ?: MangroTheme.typography.title.titleM,
                        )
                    }

                    Text(
                        text = stringResource(R.string.pickup_request_remaining_time),
                        color = MangroTheme.colors.textCanceled,
                        style = MangroTheme.typography.caption.captionS,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        when (item.status) {
            OwnerPickupRequestStatus.UNAVAILABLE, OwnerPickupRequestStatus.CANCELED ->
                MangroButton(
                    text = stringResource(if (item.status == OwnerPickupRequestStatus.UNAVAILABLE) R.string.pickup_request_unavailable else R.string.pickup_request_canceled),
                    onClick = onButtonClick,
                    style = MangroButtonStyle.DEFAULT,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false,
                )

            OwnerPickupRequestStatus.EXPIRED ->
                MangroButton(
                    text = stringResource(R.string.pickup_request_expired),
                    onClick = onButtonClick,
                    style = MangroButtonStyle.DEFAULT,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false,
                )

            OwnerPickupRequestStatus.IN_PROGRESS ->
                MangroButton(
                    text = stringResource(R.string.pickup_request_complete),
                    onClick = onButtonClick,
                    style = MangroButtonStyle.OUTLINED,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = enabled && completeEnabled,
                )

            OwnerPickupRequestStatus.COMPLETED ->
                MangroButton(
                    text = stringResource(R.string.pickup_request_completed),
                    onClick = onButtonClick,
                    style = MangroButtonStyle.DEFAULT,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false,
                )
        }
    }
}

private class OwnerPickupRequestPreviewParamProvider : PreviewParameterProvider<OwnerPickupRequestItem> {
    private val now = System.currentTimeMillis()

    override val values = sequenceOf(
        OwnerPickupRequestItem(
            id = "1",
            requestedAt = "09.02(수) 18시 15분",
            consumerName = "양모펠트",
            pickupDeadlineText = "18시 30분까지 픽업 예정",
            productName = "[딱 2시간만] 사과 6입",
            quantity = 1,
            status = OwnerPickupRequestStatus.EXPIRED,
        ),
        OwnerPickupRequestItem(
            id = "2",
            requestedAt = "09.02(수) 18시 20분",
            consumerName = "윤지현",
            pickupDeadlineText = "18시 35분까지 픽업 예정",
            productName = "복숭아 4입",
            quantity = 1,
            status = OwnerPickupRequestStatus.IN_PROGRESS,
            requestTimeMillis = now - 11 * 60_000,
            endTimeMillis = now + 3 * 60_000 + 35_000,
        ),
        OwnerPickupRequestItem(
            id = "3",
            requestedAt = "09.02(수) 18시 25분",
            consumerName = "닉네임최대몇글자까지",
            pickupDeadlineText = "18시 40분까지 픽업 예정",
            productName = "상품명은25자내외까지허용입니다상품명은25자내외까지허용입니다",
            quantity = 1,
            status = OwnerPickupRequestStatus.COMPLETED,
            requestTimeMillis = now - 12 * 60_000,
            endTimeMillis = now + 8 * 60_000 + 24_000,
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun OwnerPickupRequestCardPreview(
    @PreviewParameter(OwnerPickupRequestPreviewParamProvider::class) item: OwnerPickupRequestItem,
) {
    MangroTheme {
        OwnerPickupRequestCard(
            item = item,
            onConsumerClick = {},
            onButtonClick = {},
            modifier = Modifier.padding(top = 150.dp),
        )
    }
}
