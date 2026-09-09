package com.swyp.mangro.core.designsystem.component.card.wishlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.swyp.mangro.core.designsystem.R
import com.swyp.mangro.core.designsystem.theme.MangroTheme

data class WishedProduct(
    val id: String,
    val imageUrl: String,
    val name: String,
    val quantity: Int,
    val price: Int,
    val originalPrice: Int?,
)

@Composable
fun WishProductCard(
    product: WishedProduct,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(20.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        AsyncImage(
            model = product.imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .width(80.dp)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(MangroTheme.colors.surfaceDisabled),
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = product.name,
                color = MangroTheme.colors.textTitle,
                style = MangroTheme.typography.title.titleL,
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(R.string.product_quantity, product.quantity),
                    color = MangroTheme.colors.textTitle,
                    style = MangroTheme.typography.label.labelM,
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    product.originalPrice?.let {
                        Text(
                            text = stringResource(R.string.product_price, it),
                            color = MangroTheme.colors.grayScale500,
                            style = MangroTheme.typography.caption.captionS,
                        )
                    }

                    Text(
                        text = stringResource(R.string.product_price, product.price),
                        color = MangroTheme.colors.textTitle,
                        style = MangroTheme.typography.label.labelL,
                    )
                }
            }
        }
    }
}

private class WishedProductCardPreviewParamProvider : PreviewParameterProvider<WishedProduct> {
    override val values = sequenceOf(
        WishedProduct(
            id = "1",
            imageUrl = "",
            name = "복숭아 4입",
            quantity = 1,
            price = 4_000,
            originalPrice = 10_000,
        ),
        WishedProduct(
            id = "2",
            imageUrl = "",
            name = "알배기 배추 2통",
            quantity = 2,
            price = 3_000,
            originalPrice = null,
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun WishProductCardPreview(
    @PreviewParameter(WishedProductCardPreviewParamProvider::class) product: WishedProduct,
) {
    MangroTheme {
        WishProductCard(
            product = product,
        )
    }
}
