package com.swyp.mangro.feature.consumer.home.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.swyp.mangro.core.designsystem.R
import com.swyp.mangro.core.designsystem.component.MangroButton
import com.swyp.mangro.core.designsystem.component.MangroButtonStyle
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.feature.consumer.home.R as homeR

@Composable
internal fun LocationPermissionRequiredContent(
    onExpandRadiusClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(
                horizontal = 20.dp,
                vertical = 16.dp,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.ic_location_on),
            contentDescription = null,
            tint = MangroTheme.colors.primaryNormal,
            modifier = Modifier.size(48.dp),
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(homeR.string.home_permission_required_title),
            style = MangroTheme.typography.title.titleM,
            color = MangroTheme.colors.textTitle,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = stringResource(homeR.string.home_permission_required_description),
            style = MangroTheme.typography.body.body03,
            color = MangroTheme.colors.textBody,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(23.dp))

        MangroButton(
            text = stringResource(homeR.string.home_expand_radius_button),
            onClick = onExpandRadiusClick,
            style = MangroButtonStyle.ACTIVE,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LocationPermissionRequiredContentPreview() {
    MangroTheme {
        LocationPermissionRequiredContent(
            onExpandRadiusClick = {},
        )
    }
}
