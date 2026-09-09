package com.swyp.mangro.core.designsystem.component.card.wishlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.swyp.mangro.core.designsystem.R
import com.swyp.mangro.core.designsystem.component.badge.MangroBadge
import com.swyp.mangro.core.designsystem.component.card.DiscountThumbnail
import com.swyp.mangro.core.designsystem.component.card.timer.formatRemaining
import com.swyp.mangro.core.designsystem.component.card.timer.rememberTimerCardState
import com.swyp.mangro.core.designsystem.component.progress.MangroProgressBar
import com.swyp.mangro.core.designsystem.theme.ConsumerMangroCaption
import com.swyp.mangro.core.designsystem.theme.Gray900
import com.swyp.mangro.core.designsystem.theme.MangroTheme

data class WishHistoryItem(
    val id: String,
    val imageUrl: String,
    val discountRate: Int?,
    val name: String,
    val quantity: Int,
    val storeName: String,
    val price: Int,
    val status: WishStatus,
    val dateLabel: String? = null,
    val requestTimeMillis: Long? = null,
    val endTimeMillis: Long? = null,
)

enum class WishStatus {
    IN_PROGRESS,
    PICKED_UP,
    EXPIRED,
}

data class WishStatusStyle(
    val labelTextRes: Int,
    val containerColor: Color,
    val contentColor: Color,
)

@Composable
fun WishStatus.toStyle(): WishStatusStyle = when (this) {
    WishStatus.IN_PROGRESS -> WishStatusStyle(
        labelTextRes = R.string.wish_status_in_progress,
        containerColor = MangroTheme.colors.warningBg,
        contentColor = MangroTheme.colors.primaryNormal,
    )
    WishStatus.PICKED_UP -> WishStatusStyle(
        labelTextRes = R.string.wish_status_picked_up,
        containerColor = MangroTheme.colors.dangerBg,
        contentColor = MangroTheme.colors.dangerNormal,
    )
    WishStatus.EXPIRED -> WishStatusStyle(
        labelTextRes = R.string.wish_status_expired,
        containerColor = MangroTheme.colors.surfaceAlter,
        contentColor = MangroTheme.colors.textCanceled,
    )
}

@Composable
fun WishHistoryCard(
    item: WishHistoryItem,
    modifier: Modifier = Modifier,
) {
    when (item.status) {
        WishStatus.IN_PROGRESS -> InProgressWishCard(item, modifier)
        WishStatus.PICKED_UP -> PickedUpWishCard(item, modifier)
        WishStatus.EXPIRED -> ExpiredWishCard(item, modifier)
    }
}

@Composable
fun InProgressWishCard(
    item: WishHistoryItem,
    modifier: Modifier = Modifier,
) {
    val timerState = if (item.requestTimeMillis != null && item.endTimeMillis != null) {
        rememberTimerCardState(item.requestTimeMillis, item.endTimeMillis)
    } else {
        null
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                vertical = 16.dp,
                horizontal = 20.dp,
            ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            DiscountThumbnail(
                imageUrl = item.imageUrl,
                discountRate = item.discountRate,
                modifier = Modifier.width(72.dp),
            )

            Column(
                modifier = Modifier
                    .weight(1f),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = item.name,
                        color = MangroTheme.colors.textTitle,
                        style = MangroTheme.typography.label.labelL,
                    )
                    Text(
                        text = "·",
                        color = Gray900,
                        style = MangroTheme.typography.label.labelL,
                    )
                    Text(
                        text = stringResource(R.string.product_quantity, item.quantity),
                        color = MangroTheme.colors.textTitle,
                        style = MangroTheme.typography.label.labelL,
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = item.storeName,
                    color = MangroTheme.colors.textSubtitle,
                    style = MangroTheme.typography.caption.captionS,
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = stringResource(R.string.product_price, item.price),
                    color = MangroTheme.colors.textTitle,
                    style = MangroTheme.typography.body.bodyM,
                )
            }

            timerState?.let {
                Text(
                    text = formatRemaining(it.remainingMillis),
                    color = MangroTheme.colors.primaryNormal,
                    style = MangroTheme.typography.number.numberL,
                    modifier = Modifier
                        .padding(top = 3.dp)
                        .align(Alignment.Top),
                )
            }
        }

        val style = item.status.toStyle()
        MangroBadge(
            text = stringResource(style.labelTextRes),
            containerColor = style.containerColor,
            contentColor = style.contentColor,
            textStyle = ConsumerMangroCaption.captionS,
            modifier = Modifier.align(Alignment.End),
        )

        timerState?.let {
            Spacer(modifier = Modifier.height(10.dp))

            MangroProgressBar(
                progress = 1f - it.progress,
                thickness = 8.dp,
            )
        }
    }
}

@Composable
private fun PickedUpWishCard(
    item: WishHistoryItem,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                vertical = 16.dp,
                horizontal = 20.dp,
            ),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        DiscountThumbnail(
            imageUrl = item.imageUrl,
            discountRate = item.discountRate,
            modifier = Modifier.width(60.dp),
        )

        Column(
            modifier = Modifier
                .weight(1f),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = item.name,
                    color = MangroTheme.colors.textTitle,
                    style = MangroTheme.typography.label.labelL,
                )
                Text(
                    text = "·",
                    color = Gray900,
                    style = MangroTheme.typography.label.labelL,
                )
                Text(
                    text = stringResource(R.string.product_quantity, item.quantity),
                    color = MangroTheme.colors.textTitle,
                    style = MangroTheme.typography.label.labelL,
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item.dateLabel?.let {
                    Text(
                        text = it,
                        color = MangroTheme.colors.textSubtitle,
                        style = MangroTheme.typography.caption.captionS,
                    )

                    Text(
                        text = "·",
                        color = MangroTheme.colors.textSubtitle,
                        style = MangroTheme.typography.caption.captionS,
                    )
                }
                Text(
                    text = item.storeName,
                    color = MangroTheme.colors.textSubtitle,
                    style = MangroTheme.typography.caption.captionS,
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(R.string.product_price, item.price),
                    color = MangroTheme.colors.textSubtitle,
                    style = MangroTheme.typography.caption.captionS,
                )

                val style = item.status.toStyle()
                MangroBadge(
                    text = stringResource(style.labelTextRes),
                    containerColor = style.containerColor,
                    contentColor = style.contentColor,
                    textStyle = ConsumerMangroCaption.captionS,
                )
            }
        }
    }
}

@Composable
private fun ExpiredWishCard(
    item: WishHistoryItem,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                vertical = 16.dp,
                horizontal = 20.dp,
            ),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        DiscountThumbnail(
            imageUrl = item.imageUrl,
            discountRate = item.discountRate,
            modifier = Modifier.width(60.dp),
        )

        Column(
            modifier = Modifier
                .weight(1f),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = item.name,
                    color = MangroTheme.colors.textTitle,
                    style = MangroTheme.typography.label.labelL,
                )
                Text(
                    text = "·",
                    color = Gray900,
                    style = MangroTheme.typography.label.labelL,
                )
                Text(
                    text = stringResource(R.string.product_quantity, item.quantity),
                    color = MangroTheme.colors.textTitle,
                    style = MangroTheme.typography.label.labelL,
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item.dateLabel?.let {
                    Text(
                        text = it,
                        color = MangroTheme.colors.textSubtitle,
                        style = MangroTheme.typography.caption.captionS,
                    )

                    Text(
                        text = "·",
                        color = MangroTheme.colors.textSubtitle,
                        style = MangroTheme.typography.caption.captionS,
                    )
                }
                Text(
                    text = item.storeName,
                    color = MangroTheme.colors.textSubtitle,
                    style = MangroTheme.typography.caption.captionS,
                )
            }

            val style = item.status.toStyle()
            MangroBadge(
                text = stringResource(style.labelTextRes),
                containerColor = style.containerColor,
                contentColor = style.contentColor,
                textStyle = ConsumerMangroCaption.captionS,
                modifier = Modifier.align(Alignment.End),
            )
        }
    }
}

private class WishHistoryCardPreviewParamProvider : PreviewParameterProvider<WishHistoryItem> {
    private val now = System.currentTimeMillis()

    override val values = sequenceOf(
        WishHistoryItem(
            id = "1",
            imageUrl = "",
            discountRate = 60,
            name = "복숭아 4입",
            quantity = 1,
            storeName = "청과마을",
            price = 4000,
            status = WishStatus.IN_PROGRESS,
            requestTimeMillis = now - 5 * 60_000,
            endTimeMillis = now + 9 * 60_000 + 24_000,
        ),
        WishHistoryItem(
            id = "2",
            imageUrl = "",
            discountRate = 60,
            name = "대파 1단",
            quantity = 2,
            storeName = "청과마을",
            price = 7000,
            status = WishStatus.PICKED_UP,
            dateLabel = "어제",
        ),
        WishHistoryItem(
            id = "3",
            imageUrl = "",
            discountRate = 50,
            name = "알배추",
            quantity = 1,
            storeName = "청과마을",
            price = 3500,
            status = WishStatus.EXPIRED,
            dateLabel = "3일 전",
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun WishHistoryCardPreview(
    @PreviewParameter(WishHistoryCardPreviewParamProvider::class) item: WishHistoryItem,
) {
    MangroTheme {
        WishHistoryCard(
            item = item,
            modifier = Modifier
                .padding(20.dp),
        )
    }
}
