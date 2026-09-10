package com.swyp.mangro.feature.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.swyp.mangro.core.designsystem.component.KakaoButton
import com.swyp.mangro.core.designsystem.component.MangroButton
import com.swyp.mangro.core.designsystem.component.MangroButtonStyle
import com.swyp.mangro.core.designsystem.theme.MangroTheme

@Composable
fun LoginRoute(
    onKakaoLoginClick: () -> Unit,
    onBrowseWithoutLoginClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LoginScreen(
        onKakaoLoginClick = onKakaoLoginClick,
        onBrowseWithoutLoginClick = onBrowseWithoutLoginClick,
        onPrivacyPolicyClick = onPrivacyPolicyClick,
        modifier = modifier,
    )
}

@Composable
fun LoginScreen(
    onKakaoLoginClick: () -> Unit,
    onBrowseWithoutLoginClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val loginTextStyle = MangroTheme.typography.heading.headingM.copy(
        color = MangroTheme.colors.textOnBrandWhite,
    )

    Column(
        modifier = modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = stringResource(R.string.login_greeting),
                    style = loginTextStyle,
                )
                Image(
                    painter = painterResource(R.drawable.ic_wave_hand),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = stringResource(R.string.login_message_prefix),
                    style = loginTextStyle,
                )
                Spacer(modifier = Modifier.width(2.dp))
                Image(
                    painter = painterResource(R.drawable.ic_corn),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = stringResource(R.string.login_message_suffix),
                    style = loginTextStyle,
                )
            }

            Text(
                text = stringResource(R.string.login_message_closing),
                style = loginTextStyle,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(modifier = Modifier.height(58.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = 20.dp,
                    bottom = 32.dp,
                    start = 20.dp,
                    end = 20.dp,
                ),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            KakaoButton(
                onClick = onKakaoLoginClick,
            )

            MangroButton(
                onClick = onBrowseWithoutLoginClick,
                style = MangroButtonStyle.DEFAULT,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = stringResource(R.string.login_browse_without_login_button),
                    color = Color(0xFF3B1E1E),
                    style = MangroTheme.typography.heading.headingXXS,
                )
            }
        }

        Spacer(modifier = Modifier.height(50.dp))

        Text(
            text = stringResource(R.string.login_privacy_policy),
            color = MangroTheme.colors.textTitle,
            style = MangroTheme.typography.caption.captionS.copy(
                textDecoration = TextDecoration.Underline,
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onPrivacyPolicyClick,
                ),
        )

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    MangroTheme {
        val gradient = Brush.verticalGradient(
            colorStops = arrayOf(
                0.4045f to MangroTheme.colors.primaryStrong,
                1f to MangroTheme.colors.primaryNormal,
            ),
        )

        LoginScreen(
            onKakaoLoginClick = {},
            onBrowseWithoutLoginClick = {},
            onPrivacyPolicyClick = {},
            modifier = Modifier.background(gradient),
        )
    }
}
