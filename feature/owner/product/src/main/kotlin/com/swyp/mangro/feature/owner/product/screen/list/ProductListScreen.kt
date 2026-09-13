package com.swyp.mangro.feature.owner.product.screen.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.swyp.mangro.core.designsystem.component.MangroButton
import com.swyp.mangro.core.designsystem.component.MangroButtonStyle
import com.swyp.mangro.core.designsystem.component.card.owner.OwnerProduct as CardProduct
import com.swyp.mangro.core.designsystem.component.card.owner.OwnerProductCard
import com.swyp.mangro.core.designsystem.component.chip.MangroChip
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.feature.owner.product.R
import com.swyp.mangro.feature.owner.product.component.OwnerProductLabel
import com.swyp.mangro.feature.owner.product.component.OwnerProductScaffold
import com.swyp.mangro.feature.owner.product.model.OwnerProductModel

@Composable
internal fun ProductListScreen(
    products: List<OwnerProductModel>,
    onBack: () -> Unit,
    onAdd: () -> Unit,
    onSelect: (String) -> Unit,
    onStock: () -> Unit,
    onCancelReservations: (List<String>) -> Unit,
    modifier: Modifier = Modifier,
) {
    var filter by rememberSaveable { mutableStateOf(ProductFilter.ALL) }
    val filtered = products.filter {
        when (filter) {
            ProductFilter.AVAILABLE -> it.isVisibleToCustomers
            ProductFilter.OUT_OF_STOCK -> it.remainingQuantity == 0
            else -> true
        }
    }
    OwnerProductScaffold(stringResource(R.string.owner_product_list_title), onBack, modifier, bottomBarContent = {
        MangroButton(
            text = stringResource(R.string.owner_product_register_title),
            onClick = onAdd,
            style = MangroButtonStyle.ACTIVE,
            modifier = Modifier.fillMaxWidth(),
        )
    }) {
        OwnerProductLabel(stringResource(R.string.owner_product_registered_count, products.size))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ProductFilter.entries.forEach {
                MangroChip(true, stringResource(it.labelRes), isSelected = filter == it, onClick = { filter = it })
            }
        }
        Text(stringResource(R.string.owner_product_filtered_count, filtered.size), style = MangroTheme.typography.caption.captionS, color = MangroTheme.colors.textSubtitle)
        if (products.isNotEmpty()) MangroButton(stringResource(R.string.owner_product_stock_title), onStock, MangroButtonStyle.OUTLINED, Modifier.fillMaxWidth())
        if (filtered.isEmpty()) {
            Text(if (products.isEmpty()) stringResource(R.string.owner_product_empty_catalog_title) else stringResource(R.string.owner_product_empty_filter_title), style = MangroTheme.typography.heading.headingM, color = MangroTheme.colors.textTitle)
            Text(if (products.isEmpty()) stringResource(R.string.owner_product_empty_catalog_hint) else stringResource(R.string.owner_product_empty_filter_hint), style = MangroTheme.typography.caption.captionS, color = MangroTheme.colors.textSubtitle)
        }
        filtered.forEach { product ->
            OwnerProductCard(
                CardProduct(product.id, product.photos.firstOrNull().orEmpty(), product.name, product.salePrice, product.remainingQuantity, product.reservedQuantity, 0),
                Modifier.clickable { onSelect(product.id) },
            )
            if (product.shortageQuantity > 0) {
                Text(stringResource(R.string.owner_product_reservation_shortage, product.shortageQuantity), style = MangroTheme.typography.caption.captionS, color = MangroTheme.colors.textSubtitle)
                MangroButton(stringResource(R.string.owner_product_cancel_reservations), { onCancelReservations(listOf(product.id)) }, MangroButtonStyle.TEXT)
            }
            HorizontalDivider()
        }
    }
}

private enum class ProductFilter(val labelRes: Int) {
    ALL(R.string.owner_product_filter_all),
    AVAILABLE(R.string.owner_product_filter_available),
    OUT_OF_STOCK(R.string.owner_product_filter_out_of_stock),
}
