package com.swyp.mangro.feature.owner.product.screen.editor

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
import androidx.compose.ui.res.stringResource
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
import com.swyp.mangro.feature.owner.product.R
import com.swyp.mangro.feature.owner.product.component.OwnerProductConfirmationBottomSheet
import com.swyp.mangro.feature.owner.product.component.OwnerProductLabel
import com.swyp.mangro.feature.owner.product.component.OwnerProductScaffold
import com.swyp.mangro.feature.owner.product.component.OwnerProductSheetBottomSheet
import com.swyp.mangro.feature.owner.product.model.OwnerProductModel
import com.swyp.mangro.feature.owner.product.util.OwnerProductLimits
import com.swyp.mangro.feature.owner.product.util.addProductTag
import com.swyp.mangro.feature.owner.product.util.discountPercent
import com.swyp.mangro.feature.owner.product.util.formatAmount
import com.swyp.mangro.feature.owner.product.util.isValidPrice
import com.swyp.mangro.feature.owner.product.util.isValidProductName
import com.swyp.mangro.feature.owner.product.util.mergedProductPhotos
import java.util.Locale
import java.util.UUID

@Composable
internal fun ProductEditorScreen(
    product: OwnerProductModel?,
    storeClosingTime: String,
    onBack: () -> Unit,
    onSave: (OwnerProductModel) -> Unit,
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
        OwnerProductModel(
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
    OwnerProductScaffold(
        title = if (product == null) stringResource(R.string.owner_product_register_title) else stringResource(R.string.owner_product_edit_title),
        onBack = goBack,
        bottomBarContent = {
            MangroButton(
                text = if (step == 3) stringResource(R.string.owner_product_register) else stringResource(R.string.owner_product_next),
                onClick = {
                    if (step < 3) {
                        step++
                    } else {
                        tags = addProductTag(tags, tag.text.toString())
                        tag.edit { replace(0, length, "") }
                        preview = true
                    }
                },
                style = MangroButtonStyle.ACTIVE,
                modifier = Modifier.fillMaxWidth(),
                enabled = valid,
            )
        },
    ) {
        when (step) {
            1 -> {
                Text(stringResource(R.string.owner_product_editor_name_heading), style = MangroTheme.typography.heading.headingM, color = MangroTheme.colors.textTitle)
                OwnerProductLabel(
                    text = stringResource(R.string.owner_product_photos_label),
                    hint = stringResource(R.string.owner_product_photos_hint, OwnerProductLimits.PHOTO_COUNT),
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        Column(
                            Modifier.size(66.dp).border(1.dp, MangroTheme.colors.borderDefault, RoundedCornerShape(4.dp)),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            IconButton(onClick = openPhotos, enabled = photos.size < OwnerProductLimits.PHOTO_COUNT) {
                                Icon(painterResource(DesignR.drawable.ic_camera_add), stringResource(R.string.owner_product_photo_add), Modifier.size(24.dp))
                            }
                            Text(stringResource(R.string.owner_product_photo_count, photos.size, OwnerProductLimits.PHOTO_COUNT), style = MangroTheme.typography.caption.captionS, color = MangroTheme.colors.textSubtitle)
                        }
                    }
                    items(photos, key = { it }) { photo ->
                        Box(Modifier.size(66.dp)) {
                            AsyncImage(photo, stringResource(R.string.owner_product_photo_selected), Modifier.size(66.dp).clip(RoundedCornerShape(4.dp)), contentScale = ContentScale.Crop)
                            IconButton(onClick = { photos = photos - photo }, modifier = Modifier.align(Alignment.TopEnd).size(28.dp)) {
                                Icon(painterResource(DesignR.drawable.ic_x_circle), stringResource(R.string.owner_product_photo_delete), Modifier.size(20.dp))
                            }
                        }
                    }
                }
                MangroButton(stringResource(R.string.owner_product_photo_upload), openPhotos, MangroButtonStyle.DEFAULT, Modifier.fillMaxWidth(), photos.size < OwnerProductLimits.PHOTO_COUNT)
                if (photoError) Text(stringResource(R.string.owner_product_photo_error), style = MangroTheme.typography.caption.captionS, color = MangroTheme.colors.textSubtitle)
                MangroInputBox(stringResource(R.string.owner_product_name_label), stringResource(R.string.owner_product_name_hint), name, stringResource(R.string.owner_product_name_placeholder))
                if (name.text.isNotEmpty() && !validName) Text(stringResource(R.string.owner_product_name_error), style = MangroTheme.typography.caption.captionS, color = MangroTheme.colors.textSubtitle)
            }

            2 -> {
                Text(stringResource(R.string.owner_product_editor_price_heading), style = MangroTheme.typography.heading.headingM, color = MangroTheme.colors.textTitle)
                // Existing products edit stock in the detail screen so shortage confirmation cannot be bypassed.
                if (product == null) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OwnerProductLabel(
                            text = stringResource(R.string.owner_product_quantity_required),
                            hint = stringResource(R.string.owner_product_quantity_hint),
                            modifier = Modifier.weight(1f),
                        )
                        MangroStepper(quantity, { quantity = it })
                    }
                } else {
                    OwnerProductLabel(
                        text = stringResource(R.string.owner_product_remaining_quantity_count, quantity),
                        hint = stringResource(R.string.owner_product_quantity_edit_hint),
                    )
                }
                MangroInputBox(stringResource(R.string.owner_product_original_price), stringResource(R.string.owner_product_original_price_hint), originalPrice, stringResource(R.string.owner_product_original_price_placeholder), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                MangroInputBox(stringResource(R.string.owner_product_sale_price), stringResource(R.string.owner_product_sale_price_hint), salePrice, stringResource(R.string.owner_product_sale_price_placeholder), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                val original = originalPrice.text.toString().toIntOrNull() ?: 0
                val sale = salePrice.text.toString().toIntOrNull() ?: 0
                Row(
                    Modifier.fillMaxWidth().background(MangroTheme.colors.primaryLight, RoundedCornerShape(8.dp)).padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(stringResource(R.string.owner_product_final_discount), color = MangroTheme.colors.primaryNormal)
                    Text(stringResource(R.string.owner_product_discount_percent, discountPercent(original, sale)), style = MangroTheme.typography.title.titleL)
                }
                if (validPrices) {
                    Text(stringResource(R.string.owner_product_savings, stringResource(R.string.owner_product_price, (original - sale).formatAmount())), Modifier.fillMaxWidth(), color = MangroTheme.colors.primaryNormal, textAlign = TextAlign.End)
                } else if (salePrice.text.isNotEmpty()) {
                    Text(stringResource(R.string.owner_product_price_error), style = MangroTheme.typography.caption.captionS, color = MangroTheme.colors.textSubtitle)
                }
            }

            3 -> {
                Text(stringResource(R.string.owner_product_editor_info_heading), style = MangroTheme.typography.heading.headingM, color = MangroTheme.colors.textTitle)
                OwnerProductLabel(
                    text = stringResource(R.string.owner_product_pickup_end),
                    hint = stringResource(R.string.owner_product_pickup_end_hint),
                )
                MangroDropdownField(
                    text = stringResource(R.string.owner_product_pickup_today, pickupTime ?: storeClosingTime),
                    onClick = {
                        val parts = (pickupTime ?: storeClosingTime).split(":")
                        TimePickerDialog(context, { _, hour, minute -> pickupTime = String.format(Locale.ROOT, "%02d:%02d", hour, minute) }, parts[0].toInt(), parts[1].toInt(), true).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    isPlaceholder = pickupTime == null,
                    textAlign = TextAlign.Center,
                )
                if (pickupTime != null) MangroButton(stringResource(R.string.owner_product_apply_store_hours), { pickupTime = null }, MangroButtonStyle.TEXT)
                MangroInputBox(
                    stringResource(R.string.owner_product_tags_label),
                    stringResource(R.string.owner_product_tags_hint, OwnerProductLimits.TAG_COUNT),
                    tag,
                    stringResource(R.string.owner_product_tag_placeholder),
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
                    stringResource(R.string.owner_product_tag_add),
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
                            Text(item, style = MangroTheme.typography.caption.captionS, color = MangroTheme.colors.textSubtitle)
                            IconButton(onClick = { tags = tags - item }) {
                                Icon(painterResource(DesignR.drawable.ic_x_20px), stringResource(R.string.owner_product_tag_delete, item), Modifier.size(20.dp))
                            }
                        }
                    }
                }
                if (!valid) Text(stringResource(R.string.owner_product_tags_error), style = MangroTheme.typography.caption.captionS, color = MangroTheme.colors.textSubtitle)
            }
        }
    }
    if (preview && validName && validPrices) {
        val draft = currentDraft()
        OwnerProductSheetBottomSheet(
            onDismiss = { preview = false },
            bottomBar = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MangroButton(stringResource(R.string.owner_product_edit), {
                        preview = false
                        step = 1
                    }, MangroButtonStyle.OUTLINED)
                    MangroButton(
                        text = if (product == null) stringResource(R.string.owner_product_register) else stringResource(R.string.owner_product_save),
                        onClick = { onSave(draft) },
                        style = MangroButtonStyle.ACTIVE,
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                    )
                }
            },
        ) {
            OwnerProductLabel(
                text = stringResource(R.string.owner_product_preview_title),
                hint = stringResource(R.string.owner_product_preview_hint),
            )
            ProductListCard(
                Product(draft.id, draft.photos.first(), draft.discountPercent, draft.name, draft.salePrice, draft.originalPrice, ProductCategory.ETC, draft.availableQuantity),
                Modifier.border(1.dp, MangroTheme.colors.borderDefault, RoundedCornerShape(12.dp)).padding(12.dp),
            )
            Text(stringResource(R.string.owner_product_preview_summary, draft.pickupEndTime, draft.tags.joinToString(" · ")), style = MangroTheme.typography.caption.captionS, color = MangroTheme.colors.textSubtitle)
        }
    }
    if (discard) {
        OwnerProductConfirmationBottomSheet(stringResource(R.string.owner_product_discard_title), stringResource(R.string.owner_product_discard_description), { discard = false }, onBack)
    }
}
