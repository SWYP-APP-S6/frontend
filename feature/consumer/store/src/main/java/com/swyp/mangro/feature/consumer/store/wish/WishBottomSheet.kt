package com.swyp.mangro.feature.consumer.store.wish

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.swyp.mangro.core.designsystem.component.MangroButton
import com.swyp.mangro.core.designsystem.component.MangroButtonStyle
import com.swyp.mangro.core.designsystem.component.banner.NoticeBanner
import com.swyp.mangro.core.designsystem.component.bottomsheet.MangroBottomSheet
import com.swyp.mangro.core.designsystem.component.card.product.ProductListCard
import com.swyp.mangro.core.designsystem.component.stepper.MangroStepper
import com.swyp.mangro.core.designsystem.component.stepper.MangroStepperSize
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.core.model.product.Product
import com.swyp.mangro.feature.consumer.store.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishBottomSheet(
    product: Product,
    wishState: WishUiState,
    onQuantityChange: (Int) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val totalPrice = wishState.quantity * product.price
    val shape = RoundedCornerShape(10.dp)

    MangroBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = 20.dp,
                        bottom = 32.dp,
                        start = 20.dp,
                        end = 20.dp,
                    ),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                MangroButton(
                    text = stringResource(R.string.wish_sheet_edit_button),
                    onClick = onDismiss,
                    style = MangroButtonStyle.DEFAULT,
                )

                MangroButton(
                    text = stringResource(R.string.wish_sheet_confirm_button, 15),
                    onClick = onConfirm,
                    style = MangroButtonStyle.ACTIVE,
                    modifier = Modifier.weight(1f),
                )
            }
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    vertical = 4.dp,
                    horizontal = 20.dp,
                ),
        ) {
            Text(
                text = stringResource(R.string.wish_sheet_title),
                color = MangroTheme.colors.textTitle,
                style = MangroTheme.typography.heading.headingXXS,
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = stringResource(R.string.wish_sheet_description),
                color = MangroTheme.colors.textSubtitle,
                style = MangroTheme.typography.caption.captionS,
            )

            Spacer(modifier = Modifier.height(24.dp))

            ProductListCard(
                product = product,
                modifier = Modifier
                    .clip(shape)
                    .background(MangroTheme.colors.surfaceNormal)
                    .border(
                        width = 1.dp,
                        color = MangroTheme.colors.borderDefault,
                        shape = shape,
                    )
                    .padding(12.dp),
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.wish_sheet_quantity_label),
                    color = MangroTheme.colors.textTitle,
                    style = MangroTheme.typography.title.titleM,
                )

                MangroStepper(
                    value = wishState.quantity,
                    onValueChange = onQuantityChange,
                    size = MangroStepperSize.SMALL,
                    minValue = 1,
                    maxValue = product.remainingCount,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(R.string.wish_sheet_total_price_label),
                    color = MangroTheme.colors.textTitle,
                    style = MangroTheme.typography.heading.headingXXS,
                )

                Text(
                    text = stringResource(R.string.product_price, totalPrice),
                    color = MangroTheme.colors.textTitle,
                    style = MangroTheme.typography.heading.headingM,
                )
            }

            Text(
                text = stringResource(
                    R.string.wish_sheet_unit_price_quantity,
                    product.price.toString(),
                    wishState.quantity,
                ),
                color = MangroTheme.colors.textBody,
                style = MangroTheme.typography.body.body03,
                modifier = Modifier.align(Alignment.End),
            )

            Spacer(modifier = Modifier.height(16.dp))

            NoticeBanner(
                text = stringResource(R.string.wish_sheet_cancel_notice),
            )
        }
    }
}
