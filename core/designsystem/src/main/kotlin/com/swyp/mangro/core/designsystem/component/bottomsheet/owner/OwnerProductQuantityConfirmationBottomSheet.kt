package com.swyp.mangro.core.designsystem.component.bottomsheet.owner

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.swyp.mangro.core.designsystem.R
import com.swyp.mangro.core.designsystem.component.MangroButton
import com.swyp.mangro.core.designsystem.component.MangroButtonStyle
import com.swyp.mangro.core.designsystem.component.bottomsheet.MangroBottomSheet
import com.swyp.mangro.core.designsystem.theme.MangroTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerProductQuantityConfirmationBottomSheet(
    show: Boolean,
    quantity: Int,
    description: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    if (show) {
        MangroBottomSheet(
            onDismissRequest = onDismiss,
            bottomBar = {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 32.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    MangroButton(
                        onClick = onDismiss,
                        style = MangroButtonStyle.GHOST,
                        modifier = Modifier.border(1.dp, MangroTheme.colors.borderDefault, RoundedCornerShape(12.dp)),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.5.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.owner_product_sheet_no),
                            style = MangroTheme.typography.heading.headingXXS,
                            color = MangroTheme.colors.textTitle,
                        )
                    }
                    MangroButton(
                        text = stringResource(R.string.owner_product_sheet_confirm_yes),
                        onClick = onConfirm,
                        style = MangroButtonStyle.ACTIVE,
                        modifier = Modifier.weight(1f),
                        textStyle = MangroTheme.typography.heading.headingXXS,
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.5.dp),
                    )
                }
            },
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                OwnerProductQuantityTitle(stringResource(R.string.owner_product_sheet_quantity_title, quantity), quantity)
                Text(
                    text = description,
                    modifier = Modifier.widthIn(max = 280.dp).fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MangroTheme.typography.body.bodyM,
                    color = MangroTheme.colors.textSubtitle,
                )
            }
        }
    }
}
