package com.swyp.mangro.core.designsystem.component.card.product

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.swyp.mangro.core.designsystem.R
import com.swyp.mangro.core.designsystem.component.badge.MangroBadge
import com.swyp.mangro.core.designsystem.component.banner.NoticeBanner
import com.swyp.mangro.core.designsystem.component.label.MangroLabel
import com.swyp.mangro.core.designsystem.theme.ConsumerMangroCaption
import com.swyp.mangro.core.designsystem.theme.Gray600
import com.swyp.mangro.core.designsystem.theme.MangroTheme

data class ProductDetail(
    val hashtags: List<String>,
    val name: String,
    val category: ProductCategory,
    val remainingCount: Int,
    val originalPrice: Int?,
    val discountRate: Int?,
    val price: Int,
    val storeName: String,
    val address: String,
    val distanceMeters: Int,
    val travelTime: String,
    val operatingHoursText: String,
)

@Composable
fun ProductDetailCard(
    product: ProductDetail,
    onStoreClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MangroTheme.colors.surfaceNormal)
            .padding(20.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            product.hashtags.forEach { tag ->
                Text(
                    text = "#$tag",
                    color = MangroTheme.colors.primaryNormal,
                    style = MangroTheme.typography.caption.captionS,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .padding(bottom = 20.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = product.name,
                    color = MangroTheme.colors.textTitle,
                    style = MangroTheme.typography.title.titleL,
                )

                Spacer(modifier = Modifier.width(6.dp))

                val (labelBg, labelContent) = product.category.toLabelColors()
                MangroLabel(
                    content = stringResource(product.category.toLabelTextRes()),
                    contentColor = labelContent,
                    containerColor = labelBg,
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

            Spacer(modifier = Modifier.height(8.dp))

            if (product.originalPrice != null) {
                Text(
                    text = stringResource(R.string.product_price, product.originalPrice),
                    color = MangroTheme.colors.textCanceled,
                    style = (MangroTheme.typography.label.labelS ?: MangroTheme.typography.label.labelM).copy(
                        textDecoration = TextDecoration.LineThrough,
                    ),
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (product.discountRate != null) {
                    Text(
                        text = "${product.discountRate}%",
                        color = MangroTheme.colors.primaryStrong,
                        style = MangroTheme.typography.heading.headingM,
                    )
                }

                Text(
                    text = stringResource(R.string.product_price, product.price),
                    color = MangroTheme.colors.textTitle,
                    style = MangroTheme.typography.heading.headingM,
                )
            }
        }

        Column(
            modifier = Modifier
                .padding(top = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ProductInfoRow(
                iconRes = R.drawable.ic_store_front,
                label = stringResource(R.string.product_detail_seller_label),
            ) {
                Row(
                    modifier = Modifier.clickable(onClick = onStoreClick),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = product.storeName,
                        color = MangroTheme.colors.textTitle,
                        style = MangroTheme.typography.title.titleS ?: MangroTheme.typography.title.titleM,
                    )

                    Icon(
                        painter = painterResource(R.drawable.ic_chevron_right),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = Gray600,
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = product.address,
                    color = MangroTheme.colors.textBody,
                    style = MangroTheme.typography.body.bodyM,
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = stringResource(R.string.product_detail_distance_prefix),
                        color = MangroTheme.colors.textBody,
                        style = MangroTheme.typography.body.bodyM,
                    )

                    Text(
                        text = stringResource(R.string.product_detail_distance_value, product.distanceMeters),
                        color = MangroTheme.colors.primaryStrong,
                        style = MangroTheme.typography.body.bodyM,
                    )

                    Text(
                        text = "·",
                        color = MangroTheme.colors.textBody,
                        style = MangroTheme.typography.body.bodyM,
                    )

                    Text(
                        text = product.travelTime,
                        color = MangroTheme.colors.textBody,
                        style = MangroTheme.typography.body.bodyM,
                    )
                }
            }

            ProductInfoRow(
                iconRes = R.drawable.ic_alarm_on_16px,
                label = stringResource(R.string.product_detail_operating_hours_label),
            ) {
                Text(
                    text = product.operatingHoursText,
                    color = MangroTheme.colors.textBody,
                    style = MangroTheme.typography.body.bodyM,
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        NoticeBanner(
            stringRes = R.string.banner_payment_notice,
        )
    }
}

@Composable
private fun ProductInfoRow(
    iconRes: Int,
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            modifier = Modifier
                .width(74.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = Gray600,
            )

            Text(
                text = label,
                color = MangroTheme.colors.textSubtitle,
                style = MangroTheme.typography.body.bodyM,
            )
        }

        Spacer(modifier = Modifier.width(24.dp))

        Column(
            modifier = Modifier
                .weight(1f),
        ) { content() }
    }
}

private val previewProductDetail = ProductDetail(
    hashtags = listOf("복숭아", "청과", "딱복", "물복", "식자재"),
    name = "복숭아 4입",
    category = ProductCategory.VEGETABLES,
    remainingCount = 3,
    originalPrice = 10_000,
    discountRate = 50,
    price = 4_000,
    storeName = "청과마을",
    address = "서울 마포구 망원로 12",
    distanceMeters = 450,
    travelTime = "도보 7분",
    operatingHoursText = "오늘 20:00까지",
)

@Preview(showBackground = true)
@Composable
private fun ProductDetailCardPreview() {
    MangroTheme {
        ProductDetailCard(
            product = previewProductDetail,
            onStoreClick = {},
        )
    }
}
