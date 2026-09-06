package com.swyp.mangro.core.designsystem.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.swyp.mangro.core.designsystem.R
import com.swyp.mangro.core.designsystem.component.MangroButton
import com.swyp.mangro.core.designsystem.component.MangroButtonStyle
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.core.designsystem.theme.PretendardFont
import com.swyp.mangro.core.designsystem.theme.utils.dropShadow

@Composable
fun RequestLocationPermissionScreen(
    onAllowClick: () -> Unit,
    onLaterClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { Spacer(modifier = Modifier.size(85.dp)) },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .dropShadow(
                        shape = RectangleShape,
                        color = Color(0xFFA2A2A2).copy(alpha = 0.25f),
                        blur = 4.dp,
                        offsetY = 0.dp,
                    )
                    .background(MangroTheme.colors.surfaceNormal.copy(alpha = 0.8f))
                    .padding(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 32.dp),
            ) {
                MangroButton(
                    onClick = onAllowClick,
                    style = MangroButtonStyle.ACTIVE,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp),
                ) {
                    Text(
                        text = stringResource(R.string.request_permission_allow),
                        color = MangroTheme.colors.grayScale50,
                        style = MangroTheme.typography.title.titleM,
                    )
                }
                MangroButton(
                    onClick = onLaterClick,
                    style = MangroButtonStyle.TEXT,
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .fillMaxWidth()
                        .heightIn(min = 48.dp),
                ) {
                    Text(
                        text = stringResource(R.string.request_permission_later),
                        color = MangroTheme.colors.textSubtitle,
                        style = MangroTheme.typography.title.titleM,
                    )
                }
            }
        },
        containerColor = MangroTheme.colors.surfaceNormal,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.ic_permission_location),
                contentDescription = null,
                modifier = Modifier.size(56.dp),
            )
            Text(
                text = stringResource(R.string.request_permission_title),
                modifier = Modifier.padding(top = 8.dp).fillMaxWidth(),
                color = MangroTheme.colors.textTitle,
                style = MangroTheme.typography.heading.headingM.copy(lineHeight = 1.4.em),
            )
            Text(
                text = stringResource(R.string.request_permission_description),
                modifier = Modifier.padding(top = 8.dp),
                color = MangroTheme.colors.textBody,
                style = MangroTheme.typography.body.body03.copy(fontFamily = PretendardFont.Regular),
            )
        }
    }
}

@Preview(name = "Consumer", widthDp = 360, heightDp = 800)
@Composable
private fun RequestLocationPermissionScreenPreview() {
    MangroTheme {
        RequestLocationPermissionScreen(
            onAllowClick = {},
            onLaterClick = {},
        )
    }
}
