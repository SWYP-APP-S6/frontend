package com.swyp.mangro.feature.owner.product.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.swyp.mangro.core.designsystem.component.MangroButton
import com.swyp.mangro.core.designsystem.component.MangroButtonStyle
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.feature.owner.product.R

@Composable
internal fun OwnerDetailLoadStatus(
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    onRetry: () -> Unit,
) {
    if (isLoading) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MangroTheme.colors.primaryNormal)
        }
    } else {
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(painter = painterResource(R.drawable.pickup_empty), contentDescription = null)
            Text(
                text = stringResource(R.string.pickup_error),
                color = MangroTheme.colors.textTitle,
                style = MangroTheme.typography.title.titleM,
            )
            MangroButton(
                text = stringResource(R.string.owner_management_retry),
                onClick = onRetry,
                style = MangroButtonStyle.OUTLINED,
            )
        }
    }
}
