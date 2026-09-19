package com.swyp.mangro.feature.owner.product.screen.editor.pickup

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.swyp.mangro.core.designsystem.component.MangroButton
import com.swyp.mangro.core.designsystem.component.MangroButtonStyle
import com.swyp.mangro.core.designsystem.component.card.product.ProductListCard
import com.swyp.mangro.core.designsystem.component.dropdown.MangroDropdownField
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.core.designsystem.theme.PretendardFont
import com.swyp.mangro.core.model.product.Product
import com.swyp.mangro.core.model.product.ProductCategory
import com.swyp.mangro.feature.owner.product.R
import com.swyp.mangro.feature.owner.product.component.OwnerProductLabel
import com.swyp.mangro.feature.owner.product.component.OwnerProductScaffold
import com.swyp.mangro.feature.owner.product.component.OwnerProductSheetBottomSheet
import com.swyp.mangro.feature.owner.product.model.OwnerProductModel
import com.swyp.mangro.feature.owner.product.model.ProductDraftModel
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
    storeCategory: String? = null,
    viewModel: ProductPickupInfoViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val owner = LocalLifecycleOwner.current
    LaunchedEffect(viewModel, draft, storeClosingTime, storeOpeningTime, storeCategory) {
        draft?.let {
            viewModel.initialize(
                input = it,
                storeCategory = storeCategory,
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
    BackHandler(
        enabled = !uiState.showPreview,
        onBack = { onAction(ProductPickupInfoAction.NavigationBackClicked) },
    )

    OwnerProductScaffold(
        title = stringResource(R.string.owner_product_register_title),
        onBack = { onAction(ProductPickupInfoAction.NavigationBackClicked) },
        contentSpacing = 0.dp,
        bottomBarContent = {
            MangroButton(
                text = stringResource(R.string.owner_product_register),
                onClick = {
                    onAction(ProductPickupInfoAction.RegisterClicked)
                },
                style = MangroButtonStyle.ACTIVE,
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.canPreview,
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
                selectedOption = uiState.pickupTime ?: uiState.storeClosingTime.takeIf { it in uiState.pickupTimeOptions },
                isError = uiState.pickupTime != null && uiState.pickupTime !in uiState.pickupTimeOptions,
                onOptionSelected = { onAction(ProductPickupInfoAction.PickupTimeChanged(it)) },
                placeholder = stringResource(R.string.owner_product_pickup_time_unavailable),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 16.dp),
                borderWidth = 1.2.dp,
                placeholderColor = MangroTheme.colors.textSubtitle,
            )
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
                    category = ProductCategory.fromStoreCategory(uiState.storeCategory),
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
