package com.swyp.mangro.feature.owner.product.screen.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.swyp.mangro.core.designsystem.component.MangroButton
import com.swyp.mangro.core.designsystem.component.MangroButtonStyle
import com.swyp.mangro.core.designsystem.component.banner.NoticeBanner
import com.swyp.mangro.core.designsystem.component.bottomsheet.owner.OwnerProductQuantityConfirmationBottomSheet
import com.swyp.mangro.core.designsystem.component.bottomsheet.owner.OwnerProductShortageBottomSheet
import com.swyp.mangro.core.designsystem.component.dialog.MangroDialogContainer
import com.swyp.mangro.core.designsystem.component.stepper.MangroStepper
import com.swyp.mangro.core.designsystem.component.stepper.MangroStepperSize
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.core.designsystem.theme.PretendardFont
import com.swyp.mangro.feature.owner.product.R
import com.swyp.mangro.feature.owner.product.component.OwnerProductScaffold
import com.swyp.mangro.feature.owner.product.model.OwnerProductModel
import com.swyp.mangro.feature.owner.product.util.formatAmount
import kotlinx.serialization.Serializable

@Serializable
data class OwnerProductDetailDestination(val productId: String)

@Composable
internal fun ProductDetailRoute(
    products: List<OwnerProductModel>,
    onBack: () -> Unit,
    onSave: (OwnerProductModel) -> Unit,
    onCancelReservations: (String) -> Unit,
    viewModel: ProductDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(products) {
        viewModel.updateProducts(products)
    }
    LaunchedEffect(viewModel, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.event.collect { event ->
                when (event) {
                    is ProductDetailEvent.SaveProduct -> onSave(event.product)
                    ProductDetailEvent.NavigateBack -> onBack()
                    is ProductDetailEvent.NavigateToCancellations -> onCancelReservations(event.productId)
                }
            }
        }
    }

    ProductDetailScreen(
        uiState = uiState,
        onAction = viewModel::handleAction,
    )
}

@Composable
internal fun ProductDetailScreen(
    uiState: ProductDetailState,
    onAction: (ProductDetailAction) -> Unit,
) {
    val product = uiState.product
    if (product == null) {
        OwnerProductScaffold(
            title = stringResource(R.string.owner_product_management_title),
            onBack = { onAction(ProductDetailAction.NavigationBackClicked) },
        ) {
            Text(
                text = stringResource(R.string.owner_product_product_missing),
                style = MangroTheme.typography.caption.captionS,
                color = MangroTheme.colors.textSubtitle,
            )
        }
        return
    }

    OwnerProductScaffold(
        title = stringResource(R.string.owner_product_detail_title),
        onBack = { onAction(ProductDetailAction.NavigationBackClicked) },
        bottomBarContent = {
            MangroButton(
                text = stringResource(R.string.owner_product_save),
                onClick = { onAction(ProductDetailAction.SaveClicked) },
                style = MangroButtonStyle.ACTIVE,
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.canSave,
                textStyle = MangroTheme.typography.heading.headingXXS,
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.5.dp),
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 20.dp),
        ) {
            Column(
                modifier = Modifier.padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Text(
                    text = product.name,
                    style = MangroTheme.typography.heading.headingS.copy(fontFamily = PretendardFont.Bold),
                    color = MangroTheme.colors.textTitle,
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MangroTheme.colors.surfaceDisabled,
                            shape = RoundedCornerShape(16.dp),
                        )
                        .padding(horizontal = 10.dp, vertical = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = stringResource(R.string.owner_product_initial_quantity),
                            style = MangroTheme.typography.caption.captionS,
                            color = MangroTheme.colors.textSubtitle,
                        )
                        Text(
                            text = product.initialQuantity.toString(),
                            style = MangroTheme.typography.heading.headingL,
                            color = MangroTheme.colors.textTitle,
                        )
                    }

                    VerticalDivider(
                        modifier = Modifier.height(24.dp),
                        thickness = 1.dp,
                        color = MangroTheme.colors.borderDefault,
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = stringResource(R.string.owner_product_reserved_quantity),
                            style = MangroTheme.typography.caption.captionS,
                            color = MangroTheme.colors.textSubtitle,
                        )
                        Text(
                            text = product.reservedQuantity.toString(),
                            style = MangroTheme.typography.heading.headingL,
                            color = MangroTheme.colors.primaryNormal,
                        )
                    }

                    VerticalDivider(
                        modifier = Modifier.height(24.dp),
                        thickness = 1.dp,
                        color = MangroTheme.colors.borderDefault,
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = stringResource(R.string.owner_product_picked_up_quantity),
                            style = MangroTheme.typography.caption.captionS,
                            color = MangroTheme.colors.textSubtitle,
                        )
                        Text(
                            text = product.pickedUpQuantity.toString(),
                            style = MangroTheme.typography.heading.headingL,
                            color = MangroTheme.colors.textTitle,
                        )
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ProductInfoRow(
                    label = stringResource(R.string.owner_product_original_price),
                    value = stringResource(R.string.owner_product_price, product.originalPrice.formatAmount()),
                )
                ProductInfoRow(
                    label = stringResource(R.string.owner_product_sale_price),
                    value = stringResource(R.string.owner_product_price, product.salePrice.formatAmount()),
                )
                ProductInfoRow(
                    label = stringResource(R.string.owner_product_pickup_end),
                    value = stringResource(R.string.owner_product_pickup_today, product.pickupEndTime),
                )
                ProductInfoRow(
                    label = stringResource(R.string.owner_product_tags_label),
                    value = product.tags.mapIndexed { index, tag -> if (index == 0) stringResource(R.string.owner_product_primary_tag, tag) else tag }.joinToString(" · ").ifEmpty { stringResource(R.string.owner_product_none) },
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 32.dp),
                color = MangroTheme.colors.borderDefault,
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = stringResource(R.string.owner_product_remaining_quantity),
                        style = MangroTheme.typography.heading.headingXXS,
                        color = MangroTheme.colors.textTitle,
                    )
                    Text(
                        text = stringResource(R.string.owner_product_remaining_quantity_hint),
                        style = MangroTheme.typography.caption.captionS,
                        color = MangroTheme.colors.textSubtitle,
                    )
                }
                MangroStepper(
                    value = uiState.quantity,
                    onValueChange = { onAction(ProductDetailAction.QuantityChanged(it)) },
                    size = MangroStepperSize.LARGE,
                    minValue = 0,
                )
                NoticeBanner(
                    text = stringResource(R.string.owner_product_zero_quantity_hint),
                    containerColor = MangroTheme.colors.primaryLight,
                    contentColor = MangroTheme.colors.primaryNormal,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                )
            }
        }
    }

    if (uiState.showSaveConfirmation) {
        OwnerProductQuantityConfirmationBottomSheet(
            quantity = uiState.quantity,
            description = if (uiState.quantity == 0) {
                stringResource(R.string.owner_product_zero_quantity_confirm)
            } else {
                stringResource(R.string.owner_product_shortage_confirm)
            },
            onDismiss = { onAction(ProductDetailAction.SaveConfirmationDismissed) },
            onConfirm = { onAction(ProductDetailAction.SaveConfirmClicked) },
        )
    }

    if (uiState.showSaved) {
        if (uiState.savedShortage > 0) {
            OwnerProductShortageBottomSheet(
                shortage = uiState.savedShortage,
                onDismiss = { onAction(ProductDetailAction.SaveResultDismissed) },
                onCancelReservations = { onAction(ProductDetailAction.ReservationsCancelClicked) },
            )
        } else {
            MangroDialogContainer(
                show = true,
                onDismissRequest = { onAction(ProductDetailAction.SaveResultDismissed) },
                title = { Text(text = stringResource(R.string.owner_product_saved)) },
                actions = {
                    MangroButton(
                        text = stringResource(R.string.owner_product_confirm),
                        onClick = { onAction(ProductDetailAction.SaveResultConfirmClicked) },
                        style = MangroButtonStyle.ACTIVE,
                        modifier = Modifier.fillMaxWidth(),
                    )
                },
            )
        }
    }
}

@Composable
private fun ProductInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            textAlign = TextAlign.Start,
            style = requireNotNull(MangroTheme.typography.title.titleS),
            color = MangroTheme.colors.textTitle,
        )
        Text(
            text = value,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f),
            style = MangroTheme.typography.body.bodyM,
            color = MangroTheme.colors.textBody,
        )
    }
}
