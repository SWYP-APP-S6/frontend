package com.swyp.mangro.feature.owner.product.component

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.swyp.mangro.core.designsystem.component.MangroButton
import com.swyp.mangro.core.designsystem.component.MangroButtonStyle
import com.swyp.mangro.feature.owner.product.R

@Composable
internal fun ManagementLoadStatus(isLoading: Boolean, hasError: Boolean, onRetry: () -> Unit) {
    Column {
        if (isLoading) CircularProgressIndicator()
        if (hasError && !isLoading) {
            Text(stringResource(R.string.pickup_error))
            MangroButton(text = stringResource(R.string.owner_management_retry), onClick = onRetry, style = MangroButtonStyle.OUTLINED)
        }
    }
}
