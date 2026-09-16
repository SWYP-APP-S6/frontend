package com.swyp.mangro.feature.owner.home.component.section

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.feature.owner.home.R

@Composable
internal fun PendingStoreSection() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.welcome_store),
            contentDescription = null,
            modifier = Modifier.padding(vertical = 48.dp),
        )

        Text(
            text = stringResource(R.string.owner_home_pending),
            textAlign = TextAlign.Center,
            style = MangroTheme.typography.title.titleS?.copy(lineBreak = LineBreak.Heading)
                ?: MangroTheme.typography.title.titleM.copy(lineBreak = LineBreak.Heading),
            color = MangroTheme.colors.textBody,
        )
    }
}
