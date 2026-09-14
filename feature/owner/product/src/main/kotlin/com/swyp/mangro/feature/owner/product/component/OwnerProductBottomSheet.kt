package com.swyp.mangro.feature.owner.product.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.swyp.mangro.core.designsystem.component.MangroButton
import com.swyp.mangro.core.designsystem.component.MangroButtonStyle
import com.swyp.mangro.core.designsystem.component.bottomsheet.MangroBottomSheet
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.feature.owner.product.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun OwnerProductSheetBottomSheet(
    onDismiss: () -> Unit,
    bottomBar: @Composable ColumnScope.() -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    MangroBottomSheet(
        onDismissRequest = onDismiss,
        bottomBar = {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                content = bottomBar,
            )
        },
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            content = content,
        )
    }
}

@Composable
internal fun OwnerProductConfirmationBottomSheet(
    title: String,
    description: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    OwnerProductSheetBottomSheet(
        onDismiss = onDismiss,
        bottomBar = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MangroButton(
                    text = stringResource(R.string.owner_product_no),
                    onClick = onDismiss,
                    style = MangroButtonStyle.OUTLINED,
                )
                MangroButton(
                    text = stringResource(R.string.owner_product_confirm_yes),
                    onClick = onConfirm,
                    style = MangroButtonStyle.ACTIVE,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                )
            }
        },
    ) {
        Text(
            text = title,
            style = MangroTheme.typography.heading.headingM,
            color = MangroTheme.colors.textTitle,
        )
        Text(
            text = description,
            style = MangroTheme.typography.caption.captionS,
            color = MangroTheme.colors.textSubtitle,
        )
    }
}
