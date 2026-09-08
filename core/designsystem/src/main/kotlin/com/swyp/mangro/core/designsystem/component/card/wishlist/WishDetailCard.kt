package com.swyp.mangro.core.designsystem.component.card.wishlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.swyp.mangro.core.designsystem.R
import com.swyp.mangro.core.designsystem.component.badge.MangroBadge
import com.swyp.mangro.core.designsystem.theme.ConsumerMangroCaption
import com.swyp.mangro.core.designsystem.theme.Gray900
import com.swyp.mangro.core.designsystem.theme.MangroTheme

data class WishDetailItem(
    val id: String,
    val name: String,
    val quantity: Int,
    val price: Int,
    val originalPrice: Int?,
    val discountRate: Int?,
    val status: WishStatus,
)

@Composable
fun WishDetailCard(
    item: WishDetailItem,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
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

        Spacer(modifier = Modifier.height(2.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = stringResource(R.string.product_price, item.price),
                color = MangroTheme.colors.textTitle,
                style = MangroTheme.typography.label.labelL,
            )

            item.originalPrice?.let {
                Text(
                    text = stringResource(R.string.product_price, item.originalPrice),
                    color = MangroTheme.colors.grayScale500,
                    style = MangroTheme.typography.caption.captionS.copy(
                        textDecoration = TextDecoration.LineThrough,
                    ),
                )
            }

            item.discountRate?.let {
                Text(
                    text = "${item.discountRate}%",
                    color = MangroTheme.colors.primaryNormal,
                    style = MangroTheme.typography.caption.captionS,
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
    }
}

private class WishDetailCardPreviewParamProvider : PreviewParameterProvider<WishDetailItem> {
    override val values = sequenceOf(
        WishDetailItem("1", "복숭아 4입", 1, 4_000, 10_000, 50, WishStatus.IN_PROGRESS),
        WishDetailItem("2", "복숭아 4입", 1, 4_000, null, null, WishStatus.PICKED_UP),
        WishDetailItem("3", "복숭아 4입", 1, 4_000, null, null, WishStatus.EXPIRED),
    )
}

@Preview(showBackground = true)
@Composable
private fun WishDetailCardPreview(
    @PreviewParameter(WishDetailCardPreviewParamProvider::class) item: WishDetailItem,
) {
    MangroTheme {
        WishDetailCard(
            item = item,
            modifier = Modifier
                .padding(16.dp),
        )
    }
}
