package com.swyp.mangro.core.designsystem.component.card.purchase

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.swyp.mangro.core.designsystem.R
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.core.designsystem.theme.utils.dropShadow

data class PurchaseInfo(
    val date: String,
    val storeName: String,
    val productName: String,
    val quantity: Int,
    val price: Int,
)

@Composable
fun PurchaseInfoCard(
    receipt: PurchaseInfo,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .dropShadow(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFADADAD).copy(alpha = 0.25f),
                blur = 8.dp,
                offsetX = 0.dp,
                offsetY = 0.dp,
            )
            .clip(RoundedCornerShape(16.dp))
            .background(MangroTheme.colors.surfaceNormal),
    ) {
        ReceiptRow(
            label = stringResource(R.string.pickup_receipt_date),
            value = receipt.date,
        )
        ReceiptRow(
            label = stringResource(R.string.pickup_receipt_store),
            value = receipt.storeName,
        )
        ReceiptRow(
            label = stringResource(R.string.pickup_receipt_product),
            value = receipt.productName,
        )
        ReceiptRow(
            label = stringResource(R.string.pickup_receipt_quantity),
            value = receipt.quantity.toString(),
        )

        HorizontalDivider(
            thickness = 0.5.dp,
            color = MangroTheme.colors.borderDefault,
        )

        ReceiptRow(
            label = stringResource(R.string.pickup_receipt_price),
            value = stringResource(R.string.product_price, receipt.price),
            valueColor = MangroTheme.colors.primaryNormal,
            labelStyle = MangroTheme.typography.title.titleM,
            valueStyle = MangroTheme.typography.body.bodyL,
        )
    }
}

@Composable
private fun ReceiptRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MangroTheme.colors.textTitle,
    labelStyle: TextStyle = MangroTheme.typography.label.labelM,
    valueStyle: TextStyle = MangroTheme.typography.label.labelM,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                vertical = 12.dp,
                horizontal = 20.dp,
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            color = MangroTheme.colors.textBody,
            style = labelStyle,
        )

        Text(
            text = value,
            color = valueColor,
            style = valueStyle,
        )
    }
}

private val previewReceipt = PurchaseInfo(
    date = "2026.09.04",
    storeName = "청과마을",
    productName = "복숭아 4입",
    quantity = 1,
    price = 4000,
)

@Preview(showBackground = true)
@Composable
private fun PurchaseInfoCardPreview() {
    MangroTheme {
        Box(
            modifier = Modifier
                .padding(20.dp),
        ) {
            PurchaseInfoCard(receipt = previewReceipt)
        }
    }
}
