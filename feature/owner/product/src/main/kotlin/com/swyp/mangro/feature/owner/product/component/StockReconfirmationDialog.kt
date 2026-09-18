package com.swyp.mangro.feature.owner.product.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swyp.mangro.core.designsystem.component.MangroButton
import com.swyp.mangro.core.designsystem.component.MangroButtonStyle
import com.swyp.mangro.core.designsystem.component.dialog.MangroDialogContainer
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.feature.owner.product.R

@Composable
fun StockReconfirmationDialog(
    productName: String,
    quantity: Int,
    isSaving: Boolean,
    onConfirm: () -> Unit,
    onReject: () -> Unit,
    onLater: () -> Unit,
) {
    MangroDialogContainer(
        show = true,
        onDismissRequest = { if (!isSaving) onLater() },
        contentSpacing = 0.dp,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(stringResource(R.string.owner_stock_reconfirm_title))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        quantity.toString(),
                        color = MangroTheme.colors.primaryNormal,
                        style = MangroTheme.typography.title.titleL.copy(fontSize = 24.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold),
                    )
                    Text(stringResource(R.string.owner_stock_reconfirm_quantity))
                }
                Text(productName, style = MangroTheme.typography.label.labelM, color = MangroTheme.colors.textSubtitle)
            }
        },
        actions = {
            MangroButton(
                text = stringResource(R.string.owner_stock_reconfirm_yes),
                onClick = onConfirm,
                style = MangroButtonStyle.ACTIVE,
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                textStyle = MangroTheme.typography.title.titleL,
            )
            MangroButton(
                text = stringResource(R.string.owner_stock_reconfirm_no),
                onClick = onReject,
                style = MangroButtonStyle.DEFAULT,
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                textStyle = MangroTheme.typography.title.titleL,
            )
            MangroButton(
                onClick = onLater,
                style = MangroButtonStyle.GHOST,
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth().height(56.dp),
            ) {
                Text(stringResource(R.string.owner_stock_reconfirm_later), style = MangroTheme.typography.body.bodyM.copy(fontSize = 14.sp, lineHeight = 19.6.sp), textDecoration = TextDecoration.Underline)
            }
        },
    )
}

@Preview
@Composable
private fun StockReconfirmationDialogPreview() {
    MangroTheme { StockReconfirmationDialog("시금치 한 단", 4, false, {}, {}, {}) }
}
