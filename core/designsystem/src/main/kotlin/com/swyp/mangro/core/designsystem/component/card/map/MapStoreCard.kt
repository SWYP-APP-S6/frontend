package com.swyp.mangro.core.designsystem.component.card.map

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.swyp.mangro.core.designsystem.R
import com.swyp.mangro.core.designsystem.theme.Gray400
import com.swyp.mangro.core.designsystem.theme.Gray900
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.core.designsystem.theme.White
import com.swyp.mangro.core.designsystem.theme.utils.dropShadow

data class StoreProduct(
    val id: String,
    val imageUrl: String,
    val discountRate: Int?,
    val productName: String,
    val price: Int,
)

@Composable
fun MapStoreCard(
    storeName: String,
    closingTime: String,
    products: List<StoreProduct>,
    onProductClick: (StoreProduct) -> Unit,
    modifier: Modifier = Modifier,
    travelInfo: (@Composable () -> Unit)? = null,
) {
    val shape = RoundedCornerShape(16.dp)

    Column(
        modifier = modifier
            .width(320.dp)
            .dropShadow(
                shape = shape,
                color = Color(0xFFA6A6A6).copy(alpha = 0.10f),
                blur = 8.dp,
                offsetX = 2.dp,
                offsetY = 4.dp,
            )
            .clip(shape)
            .background(White)
            .border(
                width = 0.6.dp,
                color = Gray400,
                shape = shape,
            )
            .padding(
                vertical = 20.dp,
                horizontal = 16.dp,
            ),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = storeName,
                color = MangroTheme.colors.textTitle,
                style = MangroTheme.typography.label.labelM,
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (travelInfo != null) {
                    travelInfo()

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(6.dp)
                            .background(Color(0xFFD9D9D9)),
                    )
                }

                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_acute),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Gray900,
                )

                Text(
                    text = stringResource(R.string.map_store_closing_time, closingTime),
                    color = MangroTheme.colors.textBody,
                    style = MangroTheme.typography.caption.captionS,
                )
            }
        }

        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            products.forEach { product ->
                StoreProductItem(
                    imageUrl = product.imageUrl,
                    discountRate = product.discountRate,
                    productName = product.productName,
                    price = product.price,
                    modifier = Modifier.clickable { onProductClick(product) },
                )
            }
        }
    }
}

private val storeCardPreviewProducts = listOf(
    StoreProduct("1", "", 60, "복숭아 4입", 4_000),
    StoreProduct("2", "", 50, "알배기 배추 2통", 3_000),
    StoreProduct("3", "", null, "대파 1단", 3_500),
)

@Preview(showBackground = true, backgroundColor = 0xFFF0F0F0)
@Composable
private fun MapStoreCardPreview() {
    MangroTheme {
        MapStoreCard(
            storeName = "청과마을",
            closingTime = "19:30",
            products = storeCardPreviewProducts,
            onProductClick = {},
            modifier = Modifier.padding(16.dp),
            travelInfo = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_directions_walk),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MangroTheme.colors.textSubtitle,
                    )

                    Text(
                        text = "도보 7분",
                        color = MangroTheme.colors.textBody,
                        style = MangroTheme.typography.caption.captionS,
                    )
                }
            },
        )
    }
}
