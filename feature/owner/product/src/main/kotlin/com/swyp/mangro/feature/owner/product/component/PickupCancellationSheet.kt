package com.swyp.mangro.feature.owner.product.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.swyp.mangro.core.designsystem.R as DesignR
import com.swyp.mangro.core.designsystem.component.MangroButton
import com.swyp.mangro.core.designsystem.component.MangroButtonStyle
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.feature.owner.product.R
import com.swyp.mangro.feature.owner.product.screen.pickup.cancellation.PickupCancellationAction
import com.swyp.mangro.feature.owner.product.screen.pickup.cancellation.PickupCancellationState

@Composable
internal fun PickupCancellationSheet(
    uiState: PickupCancellationState,
    onAction: (PickupCancellationAction) -> Unit,
) {
    OwnerProductSheetBottomSheet(
        onDismiss = { onAction(PickupCancellationAction.ConfirmationDismissed) },
        bottomBar = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MangroButton(
                    text = stringResource(R.string.pickup_return),
                    onClick = { onAction(PickupCancellationAction.ConfirmationDismissed) },
                    style = MangroButtonStyle.OUTLINED,
                )
                MangroButton(
                    text = stringResource(R.string.pickup_cancel_send),
                    onClick = { onAction(PickupCancellationAction.ConfirmationClicked) },
                    style = MangroButtonStyle.ACTIVE,
                    modifier = Modifier.weight(1f),
                )
            }
        },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = buildAnnotatedString {
                    append(stringResource(R.string.pickup_confirm_prefix))
                    withStyle(SpanStyle(color = MangroTheme.colors.primaryNormal)) {
                        append(stringResource(R.string.pickup_cases, uiState.selectedIds.size))
                    }
                    append(stringResource(R.string.pickup_confirm_suffix))
                },
                style = MangroTheme.typography.heading.headingXXS,
                color = MangroTheme.colors.textTitle,
            )
            uiState.selectedGroups.forEach { (name, count) ->
                Text(
                    text = stringResource(R.string.pickup_cancel_product_count, name, count),
                    style = MangroTheme.typography.body.bodyM,
                    color = MangroTheme.colors.textBody,
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MangroTheme.colors.dangerBg,
                    shape = RoundedCornerShape(4.dp),
                ).padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                painter = painterResource(DesignR.drawable.ic_error),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MangroTheme.colors.dangerNormal,
            )
            Text(
                text = stringResource(R.string.pickup_cancel_warning),
                style = MangroTheme.typography.caption.captionS,
                color = MangroTheme.colors.dangerNormal,
            )
        }
        Spacer(Modifier.height(4.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = stringResource(R.string.pickup_message_preview),
                style = MangroTheme.typography.title.titleS ?: MangroTheme.typography.title.titleM,
                color = MangroTheme.colors.textTitle,
            )
            Text(
                text = stringResource(R.string.pickup_cancel_message, uiState.storeName, uiState.storePhone),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MangroTheme.colors.surfaceAlter,
                        shape = RoundedCornerShape(12.dp),
                    ).padding(16.dp),
                style = MangroTheme.typography.body.bodyM,
                color = MangroTheme.colors.textBody,
            )
            if (uiState.hasError) {
                Text(
                    text = stringResource(R.string.pickup_error),
                    color = MangroTheme.colors.dangerNormal,
                )
            }
        }
    }
}
