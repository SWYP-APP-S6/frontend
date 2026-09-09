package com.swyp.mangro.core.designsystem.component.card.map

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.swyp.mangro.core.designsystem.R
import com.swyp.mangro.core.designsystem.component.card.DiscountThumbnail
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
        DiscountThumbnail(
            imageUrl = imageUrl,
            discountRate = discountRate,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = productName,
            color = MangroTheme.colors.textSubtitle,
            style = MangroTheme.typography.caption.captionS,
        )

        Text(
            text = stringResource(R.string.product_price, price),
            color = MangroTheme.colors.textTitle,
            style = MangroTheme.typography.label.labelM,
        )
    }
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
