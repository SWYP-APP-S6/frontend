package com.swyp.mangro.feature.owner.product.screen.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.swyp.mangro.core.designsystem.component.MangroButton
import com.swyp.mangro.core.designsystem.component.MangroButtonStyle
import com.swyp.mangro.core.designsystem.component.stepper.MangroStepper
import com.swyp.mangro.core.designsystem.component.stepper.MangroStepperSize
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.feature.owner.product.R
import com.swyp.mangro.feature.owner.product.component.OwnerProductConfirmationBottomSheet
import com.swyp.mangro.feature.owner.product.component.OwnerProductLabel
import com.swyp.mangro.feature.owner.product.component.OwnerProductScaffold
import com.swyp.mangro.feature.owner.product.component.OwnerProductSheetBottomSheet
import com.swyp.mangro.feature.owner.product.util.formatAmount

@Composable
internal fun ProductDetailScreen(
    uiState: ProductDetailState,
    onAction: (ProductDetailAction) -> Unit,
) {
    val product = uiState.product ?: return
    OwnerProductScaffold(stringResource(R.string.owner_product_detail_title), { onAction(ProductDetailAction.NavigateBack) }, bottomBarContent = {
        MangroButton(
            text = stringResource(R.string.owner_product_save),
            onClick = { onAction(ProductDetailAction.Save) },
            style = MangroButtonStyle.ACTIVE,
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState.canSave,
        )
    }) {
        Text(product.name, style = MangroTheme.typography.heading.headingM, color = MangroTheme.colors.textTitle)
        Row(
            Modifier.fillMaxWidth().background(MangroTheme.colors.surfaceDisabled, RoundedCornerShape(16.dp)).padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            listOf(stringResource(R.string.owner_product_initial_quantity) to product.initialQuantity, stringResource(R.string.owner_product_reserved_quantity) to product.reservedQuantity, stringResource(R.string.owner_product_picked_up_quantity) to product.pickedUpQuantity).forEach { (title, value) ->
                Column {
                    Text(title, style = MangroTheme.typography.caption.captionS, color = MangroTheme.colors.textSubtitle)
                    Text(value.toString(), style = MangroTheme.typography.heading.headingM, color = MangroTheme.colors.textTitle)
                }
            }
        }
        ProductInfoRow(stringResource(R.string.owner_product_original_price), stringResource(R.string.owner_product_price, product.originalPrice.formatAmount()))
        ProductInfoRow(stringResource(R.string.owner_product_sale_price), stringResource(R.string.owner_product_price, product.salePrice.formatAmount()))
        ProductInfoRow(stringResource(R.string.owner_product_pickup_end), stringResource(R.string.owner_product_pickup_today, product.pickupEndTime))
        ProductInfoRow(stringResource(R.string.owner_product_tags_label), product.tags.mapIndexed { index, tag -> if (index == 0) stringResource(R.string.owner_product_primary_tag, tag) else tag }.joinToString(" · ").ifEmpty { stringResource(R.string.owner_product_none) })
        MangroButton(stringResource(R.string.owner_product_edit_title), { onAction(ProductDetailAction.EditProduct) }, MangroButtonStyle.OUTLINED, Modifier.fillMaxWidth())
        HorizontalDivider()
        OwnerProductLabel(text = stringResource(R.string.owner_product_remaining_quantity), hint = stringResource(R.string.owner_product_remaining_quantity_hint))
        MangroStepper(uiState.quantity, { onAction(ProductDetailAction.QuantityChanged(it)) }, size = MangroStepperSize.LARGE, minValue = 0)
        Text(stringResource(R.string.owner_product_zero_quantity_hint), style = MangroTheme.typography.caption.captionS, color = MangroTheme.colors.textSubtitle)
        if (product.shortageQuantity > 0) {
            Text(stringResource(R.string.owner_product_shortage_quantity, product.shortageQuantity), style = MangroTheme.typography.caption.captionS, color = MangroTheme.colors.textSubtitle)
            MangroButton(stringResource(R.string.owner_product_cancel_reservations), { onAction(ProductDetailAction.CancelReservations) }, MangroButtonStyle.TEXT)
        }
    }
    if (uiState.showSaveConfirmation) {
        OwnerProductConfirmationBottomSheet(
            stringResource(R.string.owner_product_quantity_confirm_title, uiState.quantity),
            if (uiState.quantity == 0) stringResource(R.string.owner_product_zero_quantity_confirm) else stringResource(R.string.owner_product_shortage_confirm),
            { onAction(ProductDetailAction.DismissConfirmation) },
            { onAction(ProductDetailAction.ConfirmSave) },
        )
    }
    if (uiState.showSaved) {
        OwnerProductSheetBottomSheet(
            onDismiss = { onAction(ProductDetailAction.DismissSaved) },
            bottomBar = {
                if (uiState.savedShortage > 0) {
                    MangroButton(
                        text = stringResource(R.string.owner_product_cancel_reservations),
                        onClick = {
                            saved = false
                            onCancelReservations()
                        },
                        style = MangroButtonStyle.ACTIVE,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    MangroButton(stringResource(R.string.owner_product_later), { onAction(ProductDetailAction.DismissSaved) }, MangroButtonStyle.TEXT)
                } else {
                    MangroButton(
                        text = stringResource(R.string.owner_product_confirm),
                        onClick = {
                            saved = false
                            onBack()
                        },
                        style = MangroButtonStyle.ACTIVE,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
        ) {
            Text(if (uiState.savedShortage > 0) stringResource(R.string.owner_product_shortage_quantity, uiState.savedShortage) else stringResource(R.string.owner_product_saved), style = MangroTheme.typography.heading.headingM, color = MangroTheme.colors.textTitle)
            if (uiState.savedShortage > 0) {
                Text(stringResource(R.string.owner_product_shortage_allocation_hint), style = MangroTheme.typography.caption.captionS, color = MangroTheme.colors.textSubtitle)
            }
        }
    }
}

@Composable
private fun ProductInfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(label, Modifier.weight(1f), style = MangroTheme.typography.body.bodyM)
        Text(value, Modifier.weight(1f), style = MangroTheme.typography.body.bodyM, color = MangroTheme.colors.textBody)
    }
}
