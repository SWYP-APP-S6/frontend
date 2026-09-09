package com.swyp.mangro.feature.owner.product

import android.app.TimePickerDialog
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.swyp.mangro.core.designsystem.R as DesignR
import com.swyp.mangro.core.designsystem.component.MangroButton
import com.swyp.mangro.core.designsystem.component.MangroButtonStyle
import com.swyp.mangro.core.designsystem.component.MangroInputBox
import com.swyp.mangro.core.designsystem.component.card.product.Product
import com.swyp.mangro.core.designsystem.component.card.product.ProductCategory
import com.swyp.mangro.core.designsystem.component.card.product.ProductListCard
import com.swyp.mangro.core.designsystem.component.dropdown.MangroDropdownField
import com.swyp.mangro.core.designsystem.component.stepper.MangroStepper
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import java.util.Locale
import java.util.UUID

@Composable
internal fun ProductEditor(
    product: OwnerProduct?,
    storeClosingTime: String,
    onBack: () -> Unit,
    onSave: (OwnerProduct) -> Unit,
) {
    var step by rememberSaveable { mutableIntStateOf(1) }
    var photos by rememberSaveable { mutableStateOf(product?.photos ?: emptyList<String>()) }
    val name = rememberSaveable(saver = TextFieldState.Saver) { TextFieldState(product?.name.orEmpty()) }
    val originalPrice = rememberSaveable(saver = TextFieldState.Saver) { TextFieldState(product?.originalPrice?.toString().orEmpty()) }
    val salePrice = rememberSaveable(saver = TextFieldState.Saver) { TextFieldState(product?.salePrice?.toString().orEmpty()) }
    val tag = rememberSaveable(saver = TextFieldState.Saver) { TextFieldState() }
    var quantity by rememberSaveable { mutableIntStateOf(product?.remainingQuantity ?: 1) }
    var pickupTime by rememberSaveable { mutableStateOf(product?.pickupEndTime) }
    var tags by rememberSaveable { mutableStateOf(product?.tags ?: emptyList<String>()) }
    var preview by rememberSaveable { mutableStateOf(false) }
    var discard by rememberSaveable { mutableStateOf(false) }
    var photoError by rememberSaveable { mutableStateOf(false) }
    val productId = rememberSaveable { product?.id ?: UUID.randomUUID().toString() }
    val context = LocalContext.current
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(OwnerProductLimits.PHOTO_COUNT)) { uris ->
        val selected = uris.mapNotNull { uri ->
            try {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                uri.toString()
            } catch (_: SecurityException) {
                photoError = true
                null
            }
        }
        photos = mergedProductPhotos(photos, selected)
    }
    val openPhotos = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
    val goBack: () -> Unit = { if (step > 1) step-- else discard = true }
    BackHandler(onBack = goBack)
    val validName = isValidProductName(name.text.toString())
    val validPrices = isValidPrice(originalPrice.text.toString(), salePrice.text.toString())
    val valid = when (step) {
        1 -> photos.isNotEmpty() && validName
        2 -> validPrices && (product != null || quantity > 0)
        else -> tag.text.isBlank() || (tags.size < OwnerProductLimits.TAG_COUNT && tag.text.toString().trim() !in tags)
    }
    val currentDraft = {
        OwnerProduct(
            id = productId,
            name = name.text.toString().trim(),
            photos = photos,
            originalPrice = originalPrice.text.toString().toInt(),
            salePrice = salePrice.text.toString().toInt(),
            initialQuantity = product?.initialQuantity ?: quantity,
            remainingQuantity = quantity,
            reservedQuantity = product?.reservedQuantity ?: 0,
            pickedUpQuantity = product?.pickedUpQuantity ?: 0,
            pickupEndTime = pickupTime ?: storeClosingTime,
            tags = tags,
        )
    }
    ProductPage(
        title = if (product == null) "상품 등록" else "상품 정보 수정",
        onBack = goBack,
        bottom = {
            ProductCta(
                text = if (step == 3) "등록하기" else "다음",
                onClick = {
                    if (step < 3) {
                        step++
                    } else {
                        tags = addProductTag(tags, tag.text.toString())
                        tag.edit { replace(0, length, "") }
                        preview = true
                    }
                },
                enabled = valid,
            )
        },
    ) {
        when (step) {
            1 -> {
                ProductHeading("오늘은 어떤 상품을\n판매하실 건가요?")
                ProductLabel("사진 *", "최대 ${OwnerProductLimits.PHOTO_COUNT}장까지 올릴 수 있어요.")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        Column(
                            Modifier.size(66.dp).border(1.dp, MangroTheme.colors.borderDefault, RoundedCornerShape(4.dp)),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            IconButton(onClick = openPhotos, enabled = photos.size < OwnerProductLimits.PHOTO_COUNT) {
                                Icon(painterResource(DesignR.drawable.ic_camera_add), "사진 추가", Modifier.size(24.dp))
                            }
                            ProductHint("${photos.size} / ${OwnerProductLimits.PHOTO_COUNT}")
                        }
                    }
                    items(photos, key = { it }) { photo ->
                        Box(Modifier.size(66.dp)) {
                            AsyncImage(photo, "선택한 상품 사진", Modifier.size(66.dp).clip(RoundedCornerShape(4.dp)), contentScale = ContentScale.Crop)
                            IconButton(onClick = { photos = photos - photo }, modifier = Modifier.align(Alignment.TopEnd).size(28.dp)) {
                                Icon(painterResource(DesignR.drawable.ic_x_circle), "사진 삭제", Modifier.size(20.dp))
                            }
                        }
                    }
                }
                MangroButton("사진 올리기", openPhotos, MangroButtonStyle.DEFAULT, Modifier.fillMaxWidth(), photos.size < OwnerProductLimits.PHOTO_COUNT)
                if (photoError) ProductHint("사진을 불러오지 못했어요. 다른 사진을 선택해주세요.")
                MangroInputBox("품목명", "판매하실 상품명·중량 등을 입력해주세요. (최대 25자)", name, "품목명 입력")
                if (name.text.isNotEmpty() && !validName) ProductHint("품목명은 공백을 제외한 내용이 있어야 하며 최대 25자예요.")
            }
            2 -> {
                ProductHeading("어떤 가격에,\n얼마나 판매할까요?")
                // Existing products edit stock in the detail screen so shortage confirmation cannot be bypassed.
                if (product == null) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ProductLabel("수량 *", "판매 가능한 개수를 입력해주세요.", Modifier.weight(1f))
                        MangroStepper(quantity, { quantity = it })
                    }
                } else {
                    ProductLabel("매장에 남은 수량 ${quantity}개", "수량은 상품 관리 상세에서 변경할 수 있어요.")
                }
                MangroInputBox("정가", "기존 가격을 입력해주세요.", originalPrice, "정가 입력", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                MangroInputBox("할인가", "판매하실 가격을 입력해주세요.", salePrice, "할인가 입력", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                val original = originalPrice.text.toString().toIntOrNull() ?: 0
                val sale = salePrice.text.toString().toIntOrNull() ?: 0
                Row(
                    Modifier.fillMaxWidth().background(MangroTheme.colors.primaryLight, RoundedCornerShape(8.dp)).padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("최종 할인율", color = MangroTheme.colors.primaryNormal)
                    Text("${discountPercent(original, sale)}%", style = MangroTheme.typography.title.titleL)
                }
                if (validPrices) {
                    Text("${(original - sale).won()}이 저렴해져요", Modifier.fillMaxWidth(), color = MangroTheme.colors.primaryNormal, textAlign = TextAlign.End)
                } else if (salePrice.text.isNotEmpty()) {
                    ProductHint("가격은 1원 이상이며, 할인가는 정가 이하여야 해요.")
                }
            }
            3 -> {
                ProductHeading("판매에 필요한 정보를\n더 알려주세요.")
                ProductLabel("픽업 종료시간", "미입력 시 기존 운영 시간으로 설정됩니다.")
                MangroDropdownField(
                    text = "오늘 ${pickupTime ?: storeClosingTime}",
                    onClick = {
                        val parts = (pickupTime ?: storeClosingTime).split(":")
                        TimePickerDialog(context, { _, hour, minute -> pickupTime = String.format(Locale.ROOT, "%02d:%02d", hour, minute) }, parts[0].toInt(), parts[1].toInt(), true).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    isPlaceholder = pickupTime == null,
                    textAlign = TextAlign.Center,
                )
                if (pickupTime != null) MangroButton("운영시간 적용", { pickupTime = null }, MangroButtonStyle.TEXT)
                MangroInputBox(
                    "식자재 태그",
                    "최대 ${OwnerProductLimits.TAG_COUNT}개까지 입력할 수 있어요. 첫 태그가 대표 태그예요.",
                    tag,
                    "태그 입력",
                    isRequired = false,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    onKeyboardAction = {
                        val added = addProductTag(tags, tag.text.toString())
                        if (added != tags) {
                            tags = added
                            tag.edit { replace(0, length, "") }
                        }
                    },
                )
                MangroButton(
                    "태그 추가",
                    {
                        tags = addProductTag(tags, tag.text.toString())
                        tag.edit { replace(0, length, "") }
                    },
                    MangroButtonStyle.DEFAULT,
                    enabled = tag.text.isNotBlank() && tags.size < OwnerProductLimits.TAG_COUNT && tag.text.toString().trim() !in tags,
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    tags.forEach { item ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            ProductHint(item)
                            IconButton(onClick = { tags = tags - item }) {
                                Icon(painterResource(DesignR.drawable.ic_x_20px), "$item 태그 삭제", Modifier.size(20.dp))
                            }
                        }
                    }
                }
                if (!valid) ProductHint("중복 태그를 지우거나, 태그를 최대 5개로 맞춰주세요.")
            }
        }
    }
    if (preview && validName && validPrices) {
        val draft = currentDraft()
        ProductSheet({ preview = false }) {
            ProductLabel("상품 미리보기", "손님께는 이렇게 보여요.")
            ProductListCard(
                Product(draft.id, draft.photos.first(), draft.discountPercent, draft.name, draft.salePrice, draft.originalPrice, ProductCategory.ETC, draft.availableQuantity),
                Modifier.border(1.dp, MangroTheme.colors.borderDefault, RoundedCornerShape(12.dp)).padding(12.dp),
            )
            ProductHint("픽업 종료 오늘 ${draft.pickupEndTime} · ${draft.tags.joinToString(" · ")}")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MangroButton("수정하기", {
                    preview = false
                    step = 1
                }, MangroButtonStyle.OUTLINED)
                ProductCta(if (product == null) "등록하기" else "저장하기", { onSave(draft) }, Modifier.weight(1f))
            }
        }
    }
    if (discard) {
        ProductConfirmation("작성을 그만둘까요?", "입력한 내용은 저장되지 않아요.", { discard = false }, onBack)
    }
}
