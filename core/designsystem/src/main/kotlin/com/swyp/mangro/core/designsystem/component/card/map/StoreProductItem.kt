package com.swyp.mangro.core.designsystem.component.card.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.swyp.mangro.core.designsystem.component.badge.MangroBadge
import com.swyp.mangro.core.designsystem.theme.ConsumerMangroCaption
import com.swyp.mangro.core.designsystem.theme.MangroTheme

@Composable
fun StoreProductItem(
    imageUrl: String,
    discountRate: Int?,
    productName: String,
    price: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .width(88.dp),
    ) {
        Box {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MangroTheme.colors.surfaceDisabled),
            )

            if (discountRate != null) {
                MangroBadge(
                    text = "$discountRate%",
                    containerColor = MangroTheme.colors.primaryNormal,
                    contentColor = MangroTheme.colors.textOnBrandWhite,
                    textStyle = ConsumerMangroCaption.captionS,
                    contentPadding = PaddingValues(
                        horizontal = 4.dp,
                        vertical = 2.dp,
                    ),
                    modifier = Modifier
                        .padding(4.dp)
                        .align(Alignment.TopStart),
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = productName,
            color = MangroTheme.colors.textSubtitle,
            style = MangroTheme.typography.caption.captionS,
        )

        Text(
            text = formatPrice(price),
            color = MangroTheme.colors.textTitle,
            style = MangroTheme.typography.label.labelM,
        )
    }
}

private fun formatPrice(price: Int): String {
    val formatted = "%,d".format(price)
    return "${formatted}원"
}

private data class StoreProductItemPreviewParam(
    val productName: String,
    val price: Int,
    val discountRate: Int?,
)

private class StoreProductItemPreviewParamProvider : PreviewParameterProvider<StoreProductItemPreviewParam> {
    override val values = sequenceOf(
        StoreProductItemPreviewParam("복숭아 4입", 4_000, 60),
        StoreProductItemPreviewParam("알배기 배추 2통", 3_000, 50),
        StoreProductItemPreviewParam("대파 1단", 3_500, null),
    )
}

@Preview(showBackground = true)
@Composable
private fun StoreProductItemPreview(
    @PreviewParameter(StoreProductItemPreviewParamProvider::class) param: StoreProductItemPreviewParam,
) {
    MangroTheme {
        StoreProductItem(
            imageUrl = "",
            discountRate = param.discountRate,
            productName = param.productName,
            price = param.price,
            modifier = Modifier.padding(12.dp),
        )
    }
}
