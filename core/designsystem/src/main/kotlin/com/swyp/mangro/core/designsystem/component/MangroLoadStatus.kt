package com.swyp.mangro.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.swyp.mangro.core.designsystem.R
import com.swyp.mangro.core.designsystem.theme.MangroTheme

@Composable
fun MangroLoadStatus(
    isLoading: Boolean,
    hasError: Boolean,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = MangroTheme.colors.primaryNormal,
            )
        }

        if (hasError && !isLoading) {
            Text(
                text = stringResource(R.string.load_status_error),
                style = MangroTheme.typography.title.titleL,
                color = MangroTheme.colors.textTitle,
                textAlign = TextAlign.Center,
            )

            MangroButton(
                text = stringResource(R.string.load_status_retry),
                style = MangroButtonStyle.OUTLINED,
                onClick = onRetry,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MangroLoadStatusPreview() {
    MangroTheme {
        Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
            MangroLoadStatus(
                isLoading = true,
                hasError = false,
                onRetry = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
            )

            MangroLoadStatus(
                isLoading = false,
                hasError = true,
                onRetry = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
            )
        }
    }
}
