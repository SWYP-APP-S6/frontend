package com.swyp.mangro.core.designsystem.component.bottomsheet.owner

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.swyp.mangro.core.designsystem.R
import com.swyp.mangro.core.designsystem.component.MangroButton
import com.swyp.mangro.core.designsystem.component.MangroButtonStyle
import com.swyp.mangro.core.designsystem.component.bottomsheet.MangroBottomSheet
import com.swyp.mangro.core.designsystem.theme.MangroTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerProductShortageBottomSheet(
    shortage: Int,
    onDismiss: () -> Unit,
    onCancelReservations: () -> Unit,
) {
    MangroBottomSheet(
        onDismissRequest = onDismiss,
        bottomBar = {
            Column(
                modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                MangroButton(
                    text = stringResource(R.string.owner_product_sheet_cancel_reservations),
                    onClick = onCancelReservations,
                    style = MangroButtonStyle.ACTIVE,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MangroTheme.typography.heading.headingXXS,
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.5.dp),
                )
                Text(
                    text = stringResource(R.string.owner_product_sheet_later),
                    modifier = Modifier.fillMaxWidth().clickable(role = Role.Button, onClick = onDismiss),
                    textAlign = TextAlign.Center,
                    textDecoration = TextDecoration.Underline,
                    style = requireNotNull(MangroTheme.typography.caption.captionM),
                    color = MangroTheme.colors.textCanceled,
                )
            }
        },
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_warning),
                contentDescription = null,
                modifier = Modifier.padding(bottom = 12.dp).size(48.dp),
                tint = Color(0xFFFF8B00),
            )
            OwnerProductQuantityTitle(stringResource(R.string.owner_product_sheet_shortage_title, shortage), shortage)
            Text(
                text = stringResource(R.string.owner_product_sheet_shortage_hint),
                modifier = Modifier.padding(top = 4.dp).widthIn(max = 280.dp).fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MangroTheme.typography.body.bodyM,
                color = MangroTheme.colors.textBody,
            )
        }
    }
}
