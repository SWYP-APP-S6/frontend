package com.swyp.mangro.core.designsystem.component.card.owner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import com.swyp.mangro.core.designsystem.component.banner.NoticeBanner
import com.swyp.mangro.core.designsystem.theme.MangroTheme

data class OwnerProduct(
    val id: String,
    val imageUrl: String,
    val name: String,
    val price: Int,
    val remainingCount: Int,
    val expectedVisitCount: Int,
    val unableToPurchaseCount: Int,
)

@Composable
fun OwnerProductCard(
    product: OwnerProduct,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
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

            Column {
                Text(
                    text = product.name,
                    color = MangroTheme.colors.textTitle,
                    style = MangroTheme.typography.title.titleL,
                )

                Text(
                    text = stringResource(R.string.product_price, product.price),
                    color = MangroTheme.colors.textTitle,
                    style = MangroTheme.typography.label.labelL,
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.owner_product_card_remaining_prefix),
                        color = MangroTheme.colors.textBody,
                        style = MangroTheme.typography.body.bodyM,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = product.remainingCount.toString(),
                        color = MangroTheme.colors.primaryNormal,
                        style = MangroTheme.typography.title.titleS ?: MangroTheme.typography.title.titleM,
                    )
                    Text(
                        text = "개",
                        color = MangroTheme.colors.textBody,
                        style = MangroTheme.typography.body.bodyM,
                    )
                    Text(
                        text = "·",
                        color = MangroTheme.colors.textSubtitle,
                        style = MangroTheme.typography.body.bodyM,
                        modifier = Modifier.padding(horizontal = 8.dp),
                    )
                    Text(
                        text = stringResource(R.string.owner_product_card_expected_visit_prefix),
                        color = MangroTheme.colors.textBody,
                        style = MangroTheme.typography.body.bodyM,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.product_quantity, product.expectedVisitCount),
                        color = MangroTheme.colors.textBody,
                        style = MangroTheme.typography.body.bodyM,
                    )
                }
            }
        }

        if (product.unableToPurchaseCount > 0) {
            NoticeBanner(
                text = stringResource(
                    R.string.owner_product_card_unable_to_purchase,
                    product.unableToPurchaseCount,
                ),
            )
        }
    }
}

private class OwnerProductCardPreviewParamProvider : PreviewParameterProvider<OwnerProduct> {
    override val values = sequenceOf(
        OwnerProduct(
            id = "1",
            imageUrl = "",
            name = "시금치 한 단",
            price = 4_000,
            remainingCount = 6,
            expectedVisitCount = 4,
            unableToPurchaseCount = 2,
        ),
        OwnerProduct(
            id = "2",
            imageUrl = "",
            name = "복숭아 4입",
            price = 4_000,
            remainingCount = 3,
            expectedVisitCount = 3,
            unableToPurchaseCount = 0,
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun OwnerProductCardPreview(
    @PreviewParameter(OwnerProductCardPreviewParamProvider::class) product: OwnerProduct,
) {
    MangroTheme {
        OwnerProductCard(
            product = product,
            modifier = Modifier.padding(16.dp),
        )
    }
}
