package com.swyp.mangro.feature.splash

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.swyp.mangro.core.designsystem.theme.MangroTheme

@Composable
fun SplashRoute(
    iconVisible: Boolean = true,
) {
    HideNavigationBarDuringSplash()
    SplashScreen(iconVisible = iconVisible)
}

@Composable
fun SplashScreen(
    modifier: Modifier = Modifier,
    iconVisible: Boolean = true,
) {
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
                    modifier = Modifier.size(width = 75.dp, height = 24.dp),
                )
            }
        }
    }
}

@Composable
private fun HideNavigationBarDuringSplash() {
    val context = LocalContext.current

    DisposableEffect(context) {
        val activity = context.findActivity()
        activity?.hideNavigationBar()
        onDispose { activity?.showNavigationBar() }
    }
}

private fun Activity.hideNavigationBar() {
    val controller = WindowCompat.getInsetsController(
        window,
        window.decorView,
    )

    controller.hide(WindowInsetsCompat.Type.navigationBars())
    controller.systemBarsBehavior =
        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
}

private fun Activity.showNavigationBar() {
    val controller = WindowCompat.getInsetsController(
        window,
        window.decorView,
    )

    controller.show(WindowInsetsCompat.Type.navigationBars())
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
