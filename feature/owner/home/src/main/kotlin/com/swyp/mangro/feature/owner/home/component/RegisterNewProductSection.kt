package com.swyp.mangro.feature.owner.home.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.swyp.mangro.core.designsystem.component.MangroButton
import com.swyp.mangro.core.designsystem.component.MangroButtonStyle
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.feature.owner.home.R

@Composable
internal fun RegisterNewProductSection(onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.owner_home_welcome),
            modifier = Modifier.padding(top = 48.dp, bottom = 12.dp),
            style = MangroTheme.typography.heading.headingL,
            color = MangroTheme.colors.primaryNormal,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.owner_home_welcome_description),
            style = MangroTheme.typography.body.bodyM,
            color = MangroTheme.colors.textTitle,
            textAlign = TextAlign.Center,
        )
        Image(
            painter = painterResource(R.drawable.welcome_store),
            contentDescription = null,
            modifier = Modifier.padding(vertical = 48.dp),
        )
        Text(
            text = stringResource(R.string.owner_home_welcome_hint),
            style = MangroTheme.typography.title.titleS ?: MangroTheme.typography.title.titleM,
            color = MangroTheme.colors.textBody,
            textAlign = TextAlign.Center,
        )
        MangroButton(
            text = stringResource(R.string.owner_home_first_product),
            onClick = onClick,
            style = MangroButtonStyle.ACTIVE,
            modifier = Modifier
                .padding(top = 40.dp)
                .fillMaxWidth(),
        )
    }
}
