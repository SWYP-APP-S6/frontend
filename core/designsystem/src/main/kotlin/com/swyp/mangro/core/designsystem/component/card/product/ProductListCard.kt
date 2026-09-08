package com.swyp.mangro.core.designsystem.component.card.product

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.swyp.mangro.core.designsystem.R
import com.swyp.mangro.core.designsystem.component.badge.MangroBadge
import com.swyp.mangro.core.designsystem.component.card.DiscountThumbnail
import com.swyp.mangro.core.designsystem.component.label.MangroLabel
import com.swyp.mangro.core.designsystem.theme.ConsumerMangroCaption
import com.swyp.mangro.core.designsystem.theme.MangroTheme

enum class ProductCategory {
    GRAINS,
    VEGETABLES,
    MEAT,
    SEAFOOD,
    NUTS,
    ETC,
}

@Composable
fun ProductCategory.toLabelColors(): Pair<Color, Color> = when (this) {
    ProductCategory.GRAINS -> MangroTheme.colors.grainsBg to MangroTheme.colors.grainsNormal
    ProductCategory.VEGETABLES -> MangroTheme.colors.vegetablesBg to MangroTheme.colors.vegetablesNormal
    ProductCategory.MEAT -> MangroTheme.colors.meatBg to MangroTheme.colors.meatNormal
    ProductCategory.SEAFOOD -> MangroTheme.colors.seafoodBg to MangroTheme.colors.seafoodShadow
    ProductCategory.NUTS -> MangroTheme.colors.nutsBg to MangroTheme.colors.nutsNormal
    ProductCategory.ETC -> MangroTheme.colors.surfaceDisabled to MangroTheme.colors.textSubtitle
}

fun ProductCategory.toLabelTextRes(): Int = when (this) {
    ProductCategory.GRAINS -> R.string.product_category_grains
    ProductCategory.VEGETABLES -> R.string.product_category_vegetables
    ProductCategory.MEAT -> R.string.product_category_meat
    ProductCategory.SEAFOOD -> R.string.product_category_seafood
    ProductCategory.NUTS -> R.string.product_category_nuts
    ProductCategory.ETC -> R.string.product_category_etc
}

data class Product(
    val id: String,
    val imageUrl: String,
    val discountRate: Int?,
    val name: String,
    val price: Int,
    val originalPrice: Int?,
    val category: ProductCategory,
    val remainingCount: Int,
)

@Composable
fun ProductListCard(
    product: Product,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        DiscountThumbnail(
            imageUrl = product.imageUrl,
            discountRate = product.discountRate,
            modifier = Modifier.size(80.dp),
        )

        Column(
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            Text(
                text = product.name,
                color = MangroTheme.colors.textTitle,
                style = MangroTheme.typography.label.labelL,
            )

            Spacer(modifier = Modifier.height(2.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = stringResource(R.string.product_list_card_price, product.price),
                    color = MangroTheme.colors.textTitle,
                    style = MangroTheme.typography.label.labelL,
                )

                if (product.originalPrice != null) {
                    Text(
                        text = stringResource(R.string.product_list_card_price, product.originalPrice),
                        style = MangroTheme.typography.caption.captionS.copy(
                            textDecoration = TextDecoration.LineThrough,
                        ),
                        color = MangroTheme.colors.grayScale500,
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val (bg, content) = product.category.toLabelColors()
                MangroLabel(
                    content = stringResource(product.category.toLabelTextRes()),
                    contentColor = content,
                    containerColor = bg,
                )
                MangroBadge(
                    text = stringResource(R.string.product_list_card_remaining_count, product.remainingCount),
                    containerColor = MangroTheme.colors.warningBg,
                    contentColor = MangroTheme.colors.primaryNormal,
                    textStyle = ConsumerMangroCaption.captionS,
                    iconResId = R.drawable.ic_badge_dot_4px,
                    iconSize = 4.dp,
                    iconSpacing = 4.dp,
                )
            }
        }
    }
}

private class ProductListItemPreviewParamProvider : PreviewParameterProvider<Product> {
    override val values = sequenceOf(
        Product(
            id = "1",
            imageUrl = "",
            discountRate = 60,
            name = "복숭아 4입",
            price = 4_000,
            originalPrice = 10_000,
            category = ProductCategory.VEGETABLES,
            remainingCount = 3,
        ),
        Product(
            id = "2",
            imageUrl = "",
            discountRate = 50,
            name = "알배기 배추 2통",
            price = 3_000,
            originalPrice = 6_000,
            category = ProductCategory.VEGETABLES,
            remainingCount = 4,
        ),
        Product(
            id = "3",
            imageUrl = "",
            discountRate = null,
            name = "대파 1단",
            price = 3_500,
            originalPrice = null,
            category = ProductCategory.VEGETABLES,
            remainingCount = 2,
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun ProductListCardPreview(
    @PreviewParameter(ProductListItemPreviewParamProvider::class) product: Product,
) {
    MangroTheme {
        ProductListCard(
            product = product,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProductListCardBorderedPreview(
    @PreviewParameter(ProductListItemPreviewParamProvider::class) product: Product,
) {
    MangroTheme {
        val shape = RoundedCornerShape(10.dp)

        Box(
            modifier = Modifier
                .padding(10.dp),
        ) {
            ProductListCard(
                product = product,
                modifier = Modifier
                    .clip(shape)
                    .background(MangroTheme.colors.surfaceNormal)
                    .border(
                        width = 1.dp,
                        color = MangroTheme.colors.borderDefault,
                        shape = shape,
                    )
                    .padding(12.dp),
            )
        }
    }
}
