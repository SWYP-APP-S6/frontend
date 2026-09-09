package com.swyp.mangro.feature.owner.product

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.swyp.mangro.core.designsystem.component.MangroButton
import com.swyp.mangro.core.designsystem.component.MangroButtonStyle
import com.swyp.mangro.core.designsystem.component.MangroInputBox
import com.swyp.mangro.core.designsystem.component.stepper.MangroStepper
import com.swyp.mangro.core.designsystem.component.stepper.MangroStepperSize
import com.swyp.mangro.core.designsystem.theme.MangroTheme

@Composable
internal fun ProductDetailScreen(
    product: OwnerProduct,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onSave: (OwnerProduct) -> Unit,
    onCancelReservations: () -> Unit,
) {
    var quantity by rememberSaveable(product.remainingQuantity) { mutableIntStateOf(product.remainingQuantity) }
    var confirm by rememberSaveable { mutableStateOf(false) }
    var saved by rememberSaveable { mutableStateOf(false) }
    var shortage by rememberSaveable { mutableIntStateOf(0) }
    val save = {
        shortage = (product.reservedQuantity - quantity).coerceAtLeast(0)
        onSave(product.copy(remainingQuantity = quantity))
        confirm = false
        saved = true
    }
    ProductPage("상품 관리 상세", onBack, bottom = {
        ProductCta("저장하기", {
            if (quantity == 0 || quantity < product.reservedQuantity) confirm = true else save()
        }, enabled = quantity != product.remainingQuantity)
    }) {
        ProductHeading(product.name)
        Row(
            Modifier.fillMaxWidth().background(MangroTheme.colors.surfaceDisabled, RoundedCornerShape(16.dp)).padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            listOf("최초 등록" to product.initialQuantity, "방문 예정" to product.reservedQuantity, "픽업 완료" to product.pickedUpQuantity).forEach { (title, value) ->
                Column {
                    ProductHint(title)
                    ProductHeading(value.toString())
                }
            }
        }
        ProductInfoRow("정가", product.originalPrice.won())
        ProductInfoRow("할인가", product.salePrice.won())
        ProductInfoRow("픽업 종료시간", "오늘 ${product.pickupEndTime}")
        ProductInfoRow("식자재 태그", product.tags.mapIndexed { index, tag -> if (index == 0) "$tag (대표)" else tag }.joinToString(" · ").ifEmpty { "없음" })
        MangroButton("상품 정보 수정", onEdit, MangroButtonStyle.OUTLINED, Modifier.fillMaxWidth())
        HorizontalDivider()
        ProductLabel("매장에 남은 수량", "현재 시점에서 판매 가능한 개수를 입력해주세요.")
        MangroStepper(quantity, { quantity = it }, size = MangroStepperSize.LARGE, minValue = 0)
        ProductHint("수량을 0으로 변경하면 손님께는 숨겨져요.")
        if (product.shortageQuantity > 0) {
            ProductHint("재고가 ${product.shortageQuantity}개 부족해요.")
            MangroButton("찜 취소하기", onCancelReservations, MangroButtonStyle.TEXT)
        }
    }
    if (confirm) {
        ProductConfirmation(
            "지금 판매 가능한 수량이\n${quantity}개가 맞나요?",
            if (quantity == 0) "재고가 없으면 손님께 보이지 않아요." else "재고가 찜된 수보다 부족해져요.",
            { confirm = false },
            save,
        )
    }
    if (saved) {
        ProductSheet({ saved = false }) {
            ProductHeading(if (shortage > 0) "재고가 ${shortage}개 부족해요." else "저장했어요")
            if (shortage > 0) {
                ProductHint("먼저 찜한 순서대로 재고를 배정하고, 부족한 찜을 확인해주세요.")
                ProductCta("찜 취소하기", {
                    saved = false
                    onCancelReservations()
                })
                MangroButton("나중에 하기", { saved = false }, MangroButtonStyle.TEXT)
            } else {
                ProductCta("확인", {
                    saved = false
                    onBack()
                })
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

@Composable
internal fun ProductStockScreen(
    products: List<OwnerProduct>,
    onBack: () -> Unit,
    onSave: (List<OwnerProduct>) -> Unit,
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
    ProductPage("재고 재확인", onBack, bottom = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ProductCta("저장하기", {
                if (updated.any { it.remainingQuantity == 0 || it.shortageQuantity > 0 }) confirm = true else save()
            }, enabled = products.isNotEmpty())
            MangroButton("나중에 하기", onBack, MangroButtonStyle.TEXT, Modifier.fillMaxWidth())
        }
    }) {
        ProductHeading("매장에 남은 수량을\n다시 확인해주세요.")
        ProductHint("찜된 수량을 포함해 매장에 실제로 남은 수량을 입력해주세요.")
        if (products.isEmpty()) ProductHint("재고를 확인할 상품이 없어요.")
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
        ProductConfirmation(
            "확인한 재고로 저장할까요?",
            "수량이 0인 상품은 손님께 숨겨져요. 찜된 수량보다 부족한 상품은 찜 취소가 필요해요.",
            { confirm = false },
            save,
        )
    }
    if (saved) {
        ProductSheet(onBack) {
            ProductHeading("재고를 저장했어요")
            if (shortageIds.isNotEmpty()) {
                ProductHint("재고가 부족한 상품 ${shortageIds.size}개의 찜을 확인해주세요.")
                ProductCta("찜 취소하기", {
                    saved = false
                    onCancelReservations(shortageIds)
                })
                MangroButton("나중에 하기", onBack, MangroButtonStyle.TEXT)
            } else {
                ProductCta("확인", onBack)
            }
        }
    }
}

@Composable
private fun StockQuantityRow(name: String, quantity: Int, onChange: (Int) -> Unit) {
    var inputOpen by rememberSaveable { mutableStateOf(false) }
    ProductLabel(name)
    MangroStepper(quantity, onChange, minValue = 0)
    MangroButton("직접 입력", { inputOpen = true }, MangroButtonStyle.OUTLINED)
    if (inputOpen) {
        val input = rememberSaveable(saver = TextFieldState.Saver) { TextFieldState(quantity.toString()) }
        ProductSheet({ inputOpen = false }) {
            MangroInputBox("$name 수량", "0 이상의 정수를 입력해주세요.", input, "수량 입력", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            ProductCta("적용", {
                parseQuantity(input.text.toString())?.let(onChange)
                inputOpen = false
            }, enabled = parseQuantity(input.text.toString()) != null)
        }
    }
}
