package com.swyp.mangro.feature.owner.product.screen.editor.price

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.swyp.mangro.core.designsystem.R as DesignR
import com.swyp.mangro.core.designsystem.component.MangroButton
import com.swyp.mangro.core.designsystem.component.MangroButtonStyle
import com.swyp.mangro.core.designsystem.component.MangroInputBox
import com.swyp.mangro.core.designsystem.component.stepper.MangroStepper
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.feature.owner.product.R
import com.swyp.mangro.feature.owner.product.component.OwnerProductLabel
import com.swyp.mangro.feature.owner.product.component.OwnerProductScaffold
import com.swyp.mangro.feature.owner.product.model.ProductDraftModel
import com.swyp.mangro.feature.owner.product.util.formatAmount
import com.swyp.mangro.feature.owner.product.util.priceInputTransformation
import com.swyp.mangro.feature.owner.product.util.priceOutputTransformation
import com.swyp.mangro.feature.owner.product.util.rememberProductTextFieldState
import kotlinx.serialization.Serializable

@Serializable
internal data object ProductPriceDestination

@Composable
internal fun ProductPriceRoute(
    draft: ProductDraftModel?,
    onNext: (ProductDraftModel) -> Unit,
    onBack: () -> Unit,
    viewModel: ProductPriceViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel, draft) {
        draft?.let(viewModel::initialize)
    }

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is ProductPriceEvent.Next -> onNext(event.draft)
                ProductPriceEvent.Back -> onBack()
            }
        }
    }

    BackHandler {
        viewModel.handleAction(ProductPriceAction.NavigationBackClicked)
    }

    ProductPriceScreen(
        uiState = state,
        onAction = viewModel::handleAction,
    )
}

@Composable
internal fun ProductPriceScreen(
    uiState: ProductPriceState,
    onAction: (ProductPriceAction) -> Unit,
) {
    val originalPrice = rememberProductTextFieldState(uiState.originalPrice) {
        onAction(ProductPriceAction.OriginalPriceChanged(it))
    }

    val salePrice = rememberProductTextFieldState(uiState.salePrice) {
        onAction(ProductPriceAction.SalePriceChanged(it))
    }

    OwnerProductScaffold(
        title = stringResource(R.string.owner_product_register_title),
        onBack = { onAction(ProductPriceAction.NavigationBackClicked) },
        contentSpacing = 0.dp,
        bottomBarContent = {
            MangroButton(
                text = stringResource(R.string.owner_product_next),
                onClick = { onAction(ProductPriceAction.NextClicked) },
                style = MangroButtonStyle.ACTIVE,
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.canContinue,
                textStyle = MangroTheme.typography.title.titleL.copy(fontWeight = FontWeight.Bold),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            )
        },
    ) {
        Column(
            modifier = Modifier.padding(top = 40.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            stringResource(R.string.owner_product_editor_price_heading).lines().forEach { line ->
                Text(
                    text = line,
                    style = MangroTheme.typography.heading.headingM.copy(lineHeight = 33.6.sp),
                    color = MangroTheme.colors.textTitle,
                )
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(32.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OwnerProductLabel(
                    text = stringResource(R.string.owner_product_quantity_required),
                    isRequired = true,
                    textStyle = MangroTheme.typography.heading.headingXXS,
                    hint = stringResource(R.string.owner_product_quantity_hint),
                    modifier = Modifier.weight(1f),
                )
                MangroStepper(
                    value = uiState.quantity,
                    onValueChange = { onAction(ProductPriceAction.QuantityChanged(it)) },
                )
            }
            MangroInputBox(
                label = stringResource(R.string.owner_product_original_price),
                hint = stringResource(R.string.owner_product_original_price_hint),
                state = originalPrice,
                placeholder = stringResource(R.string.owner_product_original_price_placeholder),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                inputTransformation = priceInputTransformation,
                outputTransformation = priceOutputTransformation,
                leadingIcon = {
                    Icon(
                        painter = painterResource(DesignR.drawable.ic_won),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MangroTheme.colors.textCanceled,
                    )
                },
            )

            MangroInputBox(
                label = stringResource(R.string.owner_product_sale_price),
                hint = stringResource(R.string.owner_product_sale_price_hint),
                state = salePrice,
                placeholder = stringResource(R.string.owner_product_sale_price_placeholder),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                inputTransformation = priceInputTransformation,
                outputTransformation = priceOutputTransformation,
                leadingIcon = {
                    Icon(
                        painter = painterResource(DesignR.drawable.ic_won),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MangroTheme.colors.textCanceled,
                    )
                },
            )
        }

        Column(
            modifier = Modifier.padding(top = 10.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MangroTheme.colors.primaryLight,
                        shape = RoundedCornerShape(8.dp),
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(R.string.owner_product_final_discount),
                    style = (MangroTheme.typography.title.titleS ?: MangroTheme.typography.title.titleM),
                    color = MangroTheme.colors.primaryNormal,
                )
                Text(
                    text = stringResource(R.string.owner_product_discount_percent, uiState.discount),
                    fontWeight = FontWeight.Bold,
                    color = MangroTheme.colors.textTitle,
                    style = MangroTheme.typography.title.titleL,
                )
            }

            if (uiState.validPrices || salePrice.text.isEmpty()) {
                Text(
                    text = stringResource(R.string.owner_product_savings, stringResource(R.string.owner_product_price, uiState.savings.formatAmount())),
                    modifier = Modifier.fillMaxWidth(),
                    color = MangroTheme.colors.primaryNormal,
                    style = (MangroTheme.typography.title.titleS ?: MangroTheme.typography.title.titleM),
                    textAlign = TextAlign.End,
                )
            } else if (salePrice.text.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.owner_product_price_error),
                    style = MangroTheme.typography.caption.captionS,
                    color = MangroTheme.colors.textSubtitle,
                )
            }
        }
    }
}
