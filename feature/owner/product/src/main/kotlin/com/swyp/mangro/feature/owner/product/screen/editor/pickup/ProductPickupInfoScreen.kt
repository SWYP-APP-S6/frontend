package com.swyp.mangro.feature.owner.product.screen.editor.pickup

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.swyp.mangro.core.designsystem.R as DesignR
import com.swyp.mangro.core.designsystem.component.MangroButton
import com.swyp.mangro.core.designsystem.component.MangroButtonStyle
import com.swyp.mangro.core.designsystem.component.MangroInputBox
import com.swyp.mangro.core.designsystem.component.card.product.Product
import com.swyp.mangro.core.designsystem.component.card.product.ProductCategory
import com.swyp.mangro.core.designsystem.component.card.product.ProductListCard
import com.swyp.mangro.core.designsystem.component.dropdown.MangroDropdownField
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.core.designsystem.theme.PretendardFont
import com.swyp.mangro.feature.owner.product.R
import com.swyp.mangro.feature.owner.product.component.OwnerProductLabel
import com.swyp.mangro.feature.owner.product.component.OwnerProductScaffold
import com.swyp.mangro.feature.owner.product.component.OwnerProductSheetBottomSheet
import com.swyp.mangro.feature.owner.product.component.rememberProductTextFieldState
import com.swyp.mangro.feature.owner.product.model.OwnerProductModel
import com.swyp.mangro.feature.owner.product.model.ProductDraftModel
import com.swyp.mangro.feature.owner.product.util.OwnerProductLimits
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.serialization.Serializable

@Serializable
internal data object ProductPickupInfoDestination

@Composable
internal fun ProductPickupInfoRoute(
    draft: ProductDraftModel?,
    storeClosingTime: String,
    storeOpeningTime: String,
    onBack: () -> Unit,
    onEditBasicInfo: () -> Unit,
    onSave: (OwnerProductModel) -> Unit,
    viewModel: ProductPickupInfoViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val owner = LocalLifecycleOwner.current
    LaunchedEffect(viewModel, draft, storeClosingTime, storeOpeningTime) {
        draft?.let {
            viewModel.initialize(
                input = it,
                storeClosingTime = storeClosingTime,
                storeOpeningTime = storeOpeningTime,
            )
        }
    }
    LaunchedEffect(viewModel, owner) {
        owner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            while (isActive) {
                viewModel.refreshTimeOptions()
                delay(1_000L.milliseconds)
            }
        }
    }
    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                ProductPickupInfoEvent.Back -> onBack()
                ProductPickupInfoEvent.EditBasicInfo -> onEditBasicInfo()
                is ProductPickupInfoEvent.Save -> onSave(event.product)
            }
        }
    }

    if (!state.isLoading) {
        ProductPickupInfoScreen(
            uiState = state,
            onAction = viewModel::handleAction,
        )
    }
}

@Composable
internal fun ProductPickupInfoScreen(
    uiState: ProductPickupInfoState,
    onAction: (ProductPickupInfoAction) -> Unit,
) {
    val tag = rememberProductTextFieldState(value = uiState.tagInput) {
        onAction(ProductPickupInfoAction.TagChanged(it))
    }
    val pickupTime = uiState.pickupTime
    val storeClosingTime = uiState.storeClosingTime
    val tags = uiState.tags
    val valid = uiState.canPreview
    val onBack = { onAction(ProductPickupInfoAction.NavigationBackClicked) }
    BackHandler(
        enabled = !uiState.showPreview,
        onBack = onBack,
    )
    OwnerProductScaffold(
        title = stringResource(R.string.owner_product_register_title),
        onBack = onBack,
        contentSpacing = 0.dp,
        bottomBarContent = {
            MangroButton(
                text = stringResource(R.string.owner_product_register),
                onClick = {
                    onAction(ProductPickupInfoAction.RegisterClicked)
                },
                style = MangroButtonStyle.ACTIVE,
                modifier = Modifier.fillMaxWidth(),
                enabled = valid,
                textStyle = MangroTheme.typography.title.titleL.copy(fontFamily = PretendardFont.Bold),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            )
        },
    ) {
        Column(
            modifier = Modifier.padding(vertical = 48.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            stringResource(R.string.owner_product_editor_info_heading).lines().forEach { line ->
                Text(
                    text = line,
                    style = MangroTheme.typography.heading.headingM.copy(lineHeight = 33.6.sp),
                    color = MangroTheme.colors.textTitle,
                )
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            OwnerProductLabel(
                textStyle = MangroTheme.typography.heading.headingXXS,
                text = stringResource(R.string.owner_product_pickup_end),
                hint = stringResource(R.string.owner_product_pickup_end_hint),
            )
            MangroDropdownField(
                options = uiState.pickupTimeOptions,
                selectedOption = pickupTime,
                isError = pickupTime != null && pickupTime !in uiState.pickupTimeOptions,
                onOptionSelected = { onAction(ProductPickupInfoAction.PickupTimeChanged(it)) },
                placeholder = if (storeClosingTime in uiState.pickupTimeOptions) {
                    stringResource(
                        R.string.owner_product_editor_pickup_today,
                        stringResource(
                            id = if (storeClosingTime.substringBefore(":").toInt() < 12) {
                                R.string.owner_product_am
                            } else {
                                R.string.owner_product_pm
                            },
                        ),
                        storeClosingTime,
                    )
                } else {
                    stringResource(R.string.owner_product_pickup_time_unavailable)
                },
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 16.dp),
                borderWidth = 1.2.dp,
                placeholderColor = MangroTheme.colors.textSubtitle,
            )
        }
        Column(
            modifier = Modifier.padding(top = 48.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            MangroInputBox(
                label = stringResource(R.string.owner_product_tags_label),
                hint = stringResource(R.string.owner_product_tags_hint, OwnerProductLimits.TAG_COUNT),
                state = tag,
                placeholder = stringResource(R.string.owner_product_tag_placeholder),
                isRequired = false,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                onKeyboardAction = { onAction(ProductPickupInfoAction.TagSubmitted) },
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                tags.forEach { item ->
                    Row(
                        modifier = Modifier
                            .background(
                                color = MangroTheme.colors.surfaceAlter,
                                shape = RoundedCornerShape(80.dp),
                            )
                            .border(
                                width = 1.dp,
                                color = MangroTheme.colors.borderDefault,
                                shape = RoundedCornerShape(80.dp),
                            )
                            .clickable(
                                role = Role.Button,
                                onClick = { onAction(ProductPickupInfoAction.TagRemoveClicked(item)) },
                            )
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = item,
                            style = MangroTheme.typography.label.labelM,
                            color = MangroTheme.colors.textSubtitle,
                        )
                        Icon(
                            painter = painterResource(DesignR.drawable.ic_x_20px),
                            contentDescription = stringResource(R.string.owner_product_tag_delete, item),
                            modifier = Modifier.size(20.dp),
                            tint = MangroTheme.colors.borderDefault,
                        )
                    }
                }
            }
            if (uiState.tagInput.isNotBlank() && !uiState.canAddTag) {
                Text(
                    text = stringResource(R.string.owner_product_tags_error),
                    style = MangroTheme.typography.caption.captionS,
                    color = MangroTheme.colors.textSubtitle,
                )
            }
        }
    }
    val draft = uiState.draft
    if (uiState.showPreview && draft != null) {
        OwnerProductSheetBottomSheet(
            onDismiss = { onAction(ProductPickupInfoAction.PreviewDismissed) },
            bottomBar = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MangroButton(
                        text = stringResource(R.string.owner_product_edit),
                        onClick = { onAction(ProductPickupInfoAction.EditBasicInfoClicked) },
                        style = MangroButtonStyle.OUTLINED,
                    )
                    MangroButton(
                        text = stringResource(R.string.owner_product_register),
                        onClick = { onAction(ProductPickupInfoAction.SaveClicked) },
                        style = MangroButtonStyle.ACTIVE,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                    )
                }
            },
        ) {
            OwnerProductLabel(
                text = stringResource(R.string.owner_product_preview_title),
                hint = stringResource(R.string.owner_product_preview_hint),
            )
            ProductListCard(
                product = Product(
                    id = draft.id,
                    imageUrl = draft.photos.first(),
                    discountRate = draft.discountPercent,
                    name = draft.name,
                    price = draft.salePrice,
                    originalPrice = draft.originalPrice,
                    category = ProductCategory.ETC,
                    remainingCount = draft.availableQuantity,
                ),
                modifier = Modifier
                    .border(
                        width = 1.dp,
                        color = MangroTheme.colors.borderDefault,
                        shape = RoundedCornerShape(12.dp),
                    )
                    .padding(12.dp),
            )
        }
    }
}
