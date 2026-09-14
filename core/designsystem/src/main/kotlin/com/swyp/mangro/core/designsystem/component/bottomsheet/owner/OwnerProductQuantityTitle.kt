package com.swyp.mangro.core.designsystem.component.bottomsheet.owner

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swyp.mangro.core.designsystem.R
import com.swyp.mangro.core.designsystem.theme.MangroTheme

@Composable
internal fun OwnerProductQuantityTitle(text: String, quantity: Int) {
    val highlighted = stringResource(R.string.product_quantity, quantity)
    val start = text.indexOf(highlighted)
    Text(
        text = buildAnnotatedString {
            append(text)
            if (start >= 0) {
                addStyle(SpanStyle(color = MangroTheme.colors.primaryNormal), start, start + highlighted.length)
            }
        },
        modifier = Modifier.widthIn(max = 280.dp).fillMaxWidth(),
        textAlign = TextAlign.Center,
        style = MangroTheme.typography.heading.headingM.copy(lineHeight = 33.6.sp),
        color = MangroTheme.colors.textTitle,
    )
}
