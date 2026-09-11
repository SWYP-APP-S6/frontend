package com.swyp.mangro.feature.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.swyp.mangro.core.designsystem.theme.MangroTheme

@Composable
fun SplashRoute(
    navigateToLogin: () -> Unit,
    navigateToHome: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel(),
) {
    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                SplashUiEvent.NavigateToLogin -> navigateToLogin()
                SplashUiEvent.NavigateToHome -> navigateToHome()
            }
        }
    }

    SplashScreen()
}

@Composable
fun SplashScreen(
    modifier: Modifier = Modifier,
) {
    var iconVisible by remember { mutableStateOf(true) }

    val gradient = Brush.verticalGradient(
        colorStops = arrayOf(
            0.4045f to MangroTheme.colors.primaryStrong,
            1f to MangroTheme.colors.primaryNormal,
        ),
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(gradient),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedVisibility(
            visible = iconVisible,
            exit = fadeOut(animationSpec = tween(durationMillis = 400)) +
                slideOutVertically(
                    targetOffsetY = { fullHeight -> -fullHeight / 4 },
                    animationSpec = tween(durationMillis = 400),
                ),
            modifier = modifier,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_app_icon),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(80.dp),
                )

                Spacer(modifier = Modifier.height(12.dp))

                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_mangro_wordmark),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(
                        width = 75.dp,
                        height = 24.dp,
                    ),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SplashScreenPreview() {
    MangroTheme {
        SplashScreen()
    }
}
