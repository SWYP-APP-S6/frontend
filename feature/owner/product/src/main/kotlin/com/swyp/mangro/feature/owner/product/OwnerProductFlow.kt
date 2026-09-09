package com.swyp.mangro.feature.owner.product

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.swyp.mangro.core.designsystem.component.MangroButton
import com.swyp.mangro.core.designsystem.component.MangroButtonStyle
import com.swyp.mangro.core.designsystem.component.card.owner.OwnerProduct as CardProduct
import com.swyp.mangro.core.designsystem.component.card.owner.OwnerProductCard
import com.swyp.mangro.core.designsystem.component.chip.MangroChip

/**
 * Caller owns the catalog and commits changes in [onSaveProducts].
 * Cancellation belongs to owner:pickup; [onCancelReservations] receives the product IDs with shortages.
 * [storeClosingTime] is the store's configured closing time in HH:mm format.
 */
@Composable
fun OwnerProductFlow(
    products: List<OwnerProduct>,
    storeClosingTime: String,
    onSaveProducts: (List<OwnerProduct>) -> Unit,
    onCancelReservations: (List<String>) -> Unit,
    modifier: Modifier = Modifier,
) {
    require(Regex("([01][0-9]|2[0-3]):[0-5][0-9]").matches(storeClosingTime))
    var screen by rememberSaveable { mutableStateOf("list") }
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    val selected = products.find { it.id == selectedId }
    val back = { screen = "list" }
    BackHandler(enabled = screen != "list", onBack = back)
    when {
        screen == "create" || screen == "edit" && selected != null -> key(selectedId, screen) {
            ProductEditor(
                product = if (screen == "edit") selected else null,
                storeClosingTime = storeClosingTime,
                onBack = back,
                onSave = { updated ->
                    onSaveProducts(listOf(updated))
                    screen = "list"
                },
            )
        }
        screen == "detail" && selected != null -> key(selected.id) {
            ProductDetailScreen(
                product = selected,
                onBack = back,
                onEdit = { screen = "edit" },
                onSave = { onSaveProducts(listOf(it)) },
                onCancelReservations = { onCancelReservations(listOf(selected.id)) },
            )
        }
        screen == "stock" -> ProductStockScreen(products, back, onSaveProducts, onCancelReservations)
        else -> ProductListScreen(
            products = products,
            onAdd = {
                selectedId = null
                screen = "create"
            },
            onSelect = {
                selectedId = it
                screen = "detail"
            },
            onStock = { screen = "stock" },
            onCancelReservations = onCancelReservations,
            modifier = modifier,
        )
    }
}

@Composable
private fun ProductListScreen(
    products: List<OwnerProduct>,
    onAdd: () -> Unit,
    onSelect: (String) -> Unit,
    onStock: () -> Unit,
    onCancelReservations: (List<String>) -> Unit,
    modifier: Modifier = Modifier,
) {
    var filter by rememberSaveable { mutableStateOf("전체") }
    val filtered = products.filter {
        when (filter) {
            "판매 가능" -> it.isVisibleToCustomers
            "재고 없음" -> it.remainingQuantity == 0
            else -> true
        }
    }
    ProductPage("점포 관리", null, modifier, bottom = { ProductCta("상품 등록", onAdd) }) {
        ProductLabel("등록된 상품 ${products.size}")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("전체", "판매 가능", "재고 없음").forEach {
                MangroChip(true, it, isSelected = filter == it, onClick = { filter = it })
            }
        }
        ProductHint("해당 상품 ${filtered.size}개")
        if (products.isNotEmpty()) MangroButton("재고 재확인", onStock, MangroButtonStyle.OUTLINED, Modifier.fillMaxWidth())
        if (filtered.isEmpty()) {
            ProductHeading(if (products.isEmpty()) "등록된 상품이 없어요" else "해당 상품이 없어요")
            ProductHint(if (products.isEmpty()) "오늘 판매할 상품을 등록해주세요." else "다른 조건을 선택해보세요.")
        }
        filtered.forEach { product ->
            OwnerProductCard(
                CardProduct(product.id, product.photos.firstOrNull().orEmpty(), product.name, product.salePrice, product.remainingQuantity, product.reservedQuantity, 0),
                Modifier.clickable { onSelect(product.id) },
            )
            if (product.shortageQuantity > 0) {
                ProductHint("찜된 수량보다 재고가 ${product.shortageQuantity}개 부족해요.")
                MangroButton("찜 취소하기", { onCancelReservations(listOf(product.id)) }, MangroButtonStyle.TEXT)
            }
            HorizontalDivider()
        }
    }
}
