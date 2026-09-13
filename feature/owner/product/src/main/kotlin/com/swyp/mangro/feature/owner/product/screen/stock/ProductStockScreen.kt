package com.swyp.mangro.feature.owner.product.screen.stock

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.swyp.mangro.core.designsystem.component.MangroButton
import com.swyp.mangro.core.designsystem.component.MangroButtonStyle
import com.swyp.mangro.core.designsystem.component.MangroInputBox
import com.swyp.mangro.core.designsystem.component.stepper.MangroStepper
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.feature.owner.product.R
import com.swyp.mangro.feature.owner.product.component.OwnerProductConfirmationBottomSheet
import com.swyp.mangro.feature.owner.product.component.OwnerProductLabel
import com.swyp.mangro.feature.owner.product.component.OwnerProductScaffold
import com.swyp.mangro.feature.owner.product.component.OwnerProductSheetBottomSheet
import com.swyp.mangro.feature.owner.product.model.OwnerProductModel
import com.swyp.mangro.feature.owner.product.util.parseQuantity

@Composable
internal fun ProductStockScreen(
    products: List<OwnerProductModel>,
    onBack: () -> Unit,
    onSave: (List<OwnerProductModel>) -> Unit,
    onCancelReservations: (List<String>) -> Unit,
) {
    // A draft of all quantities is committed together; dismissing leaves caller state untouched.
    var quantities by rememberSaveable { mutableStateOf(products.associate { it.id to it.remainingQuantity }) }
    var confirm by rememberSaveable { mutableStateOf(false) }
    var saved by rememberSaveable { mutableStateOf(false) }
    var shortageIds by rememberSaveable { mutableStateOf(emptyList<String>()) }
    val updated = products.map { it.copy(remainingQuantity = quantities[it.id] ?: it.remainingQuantity) }
    val save = {
        onSave(updated)
        shortageIds = updated.filter { it.shortageQuantity > 0 }.map { it.id }
        confirm = false
        saved = true
    }
    OwnerProductScaffold(stringResource(R.string.owner_product_stock_title), onBack, bottomBarContent = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            MangroButton(
                text = stringResource(R.string.owner_product_save),
                onClick = {
                    if (updated.any { it.remainingQuantity == 0 || it.shortageQuantity > 0 }) confirm = true else save()
                },
                style = MangroButtonStyle.ACTIVE,
                modifier = Modifier.fillMaxWidth(),
                enabled = products.isNotEmpty(),
            )
            MangroButton(stringResource(R.string.owner_product_later), onBack, MangroButtonStyle.TEXT, Modifier.fillMaxWidth())
        }
    }) {
        Text(stringResource(R.string.owner_product_stock_heading), style = MangroTheme.typography.heading.headingM, color = MangroTheme.colors.textTitle)
        Text(stringResource(R.string.owner_product_stock_hint), style = MangroTheme.typography.caption.captionS, color = MangroTheme.colors.textSubtitle)
        if (products.isEmpty()) Text(stringResource(R.string.owner_product_stock_empty), style = MangroTheme.typography.caption.captionS, color = MangroTheme.colors.textSubtitle)
        products.forEach { product ->
            key(product.id) {
                StockQuantityRow(product.name, quantities[product.id] ?: product.remainingQuantity) {
                    quantities = quantities + (product.id to it)
                }
            }
            HorizontalDivider()
        }
    }
    if (confirm) {
        OwnerProductConfirmationBottomSheet(
            stringResource(R.string.owner_product_stock_confirm_title),
            stringResource(R.string.owner_product_stock_confirm_description),
            { confirm = false },
            save,
        )
    }
    if (saved) {
        OwnerProductSheetBottomSheet(
            onDismiss = onBack,
            bottomBar = {
                if (shortageIds.isNotEmpty()) {
                    MangroButton(
                        text = stringResource(R.string.owner_product_cancel_reservations),
                        onClick = {
                            saved = false
                            onCancelReservations(shortageIds)
                        },
                        style = MangroButtonStyle.ACTIVE,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    MangroButton(stringResource(R.string.owner_product_later), onBack, MangroButtonStyle.TEXT)
                } else {
                    MangroButton(
                        text = stringResource(R.string.owner_product_confirm),
                        onClick = onBack,
                        style = MangroButtonStyle.ACTIVE,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
        ) {
            Text(stringResource(R.string.owner_product_stock_saved), style = MangroTheme.typography.heading.headingM, color = MangroTheme.colors.textTitle)
            if (shortageIds.isNotEmpty()) {
                Text(stringResource(R.string.owner_product_stock_shortage_products, shortageIds.size), style = MangroTheme.typography.caption.captionS, color = MangroTheme.colors.textSubtitle)
            }
        }
    }
}

@Composable
private fun StockQuantityRow(name: String, quantity: Int, onChange: (Int) -> Unit) {
    var inputOpen by rememberSaveable { mutableStateOf(false) }
    OwnerProductLabel(name)
    MangroStepper(quantity, onChange, minValue = 0)
    MangroButton(stringResource(R.string.owner_product_direct_input), { inputOpen = true }, MangroButtonStyle.OUTLINED)
    if (inputOpen) {
        val input = rememberSaveable(saver = TextFieldState.Saver) { TextFieldState(quantity.toString()) }
        OwnerProductSheetBottomSheet(
            onDismiss = { inputOpen = false },
            bottomBar = {
                MangroButton(
                    text = stringResource(R.string.owner_product_apply),
                    onClick = {
                        parseQuantity(input.text.toString())?.let(onChange)
                        inputOpen = false
                    },
                    style = MangroButtonStyle.ACTIVE,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = parseQuantity(input.text.toString()) != null,
                )
            },
        ) {
            MangroInputBox(stringResource(R.string.owner_product_product_quantity, name), stringResource(R.string.owner_product_quantity_input_hint), input, stringResource(R.string.owner_product_quantity_placeholder), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
        }
    }
}
