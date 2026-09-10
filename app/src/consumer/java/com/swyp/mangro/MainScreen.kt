package com.swyp.mangro

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.tooling.preview.Preview
import com.naver.maps.map.compose.ExperimentalNaverMapApi
import com.naver.maps.map.compose.NaverMap
import com.swyp.mangro.core.designsystem.component.appbar.BottomAppBar
import com.swyp.mangro.core.designsystem.component.appbar.MangroDefaultStartAlignedTopAppBar
import com.swyp.mangro.core.designsystem.component.appbar.Menu
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.core.utils.HideNavigationBarWhileVisible
import com.swyp.mangro.feature.auth.LoginRoute
import com.swyp.mangro.feature.splash.SplashRoute
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.delay

private enum class Stage { SPLASH, LOGIN, HOME }

@Composable
internal fun MainScreen() {
    var stage by remember { mutableStateOf(Stage.SPLASH) }
    var splashIconVisible by remember { mutableStateOf(true) }

    HideNavigationBarWhileVisible(hidden = stage != Stage.HOME)

    LaunchedEffect(Unit) {
        delay(1000.milliseconds)
        splashIconVisible = false
        stage = Stage.LOGIN
    }

    val gradient = Brush.verticalGradient(
        colorStops = arrayOf(
            0.4045f to MangroTheme.colors.primaryStrong,
            1f to MangroTheme.colors.primaryNormal,
        ),
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient),
    ) {
        AnimatedVisibility(
            visible = splashIconVisible,
            exit = fadeOut(animationSpec = tween(durationMillis = 400)) +
                slideOutVertically(
                    targetOffsetY = { fullHeight -> -fullHeight / 4 },
                    animationSpec = tween(durationMillis = 400),
                ),
            modifier = Modifier.align(Alignment.Center),
        ) {
            SplashRoute()
        }

        AnimatedVisibility(
            visible = stage == Stage.LOGIN,
            enter = fadeIn(animationSpec = tween(durationMillis = 400, delayMillis = 200)),
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            LoginRoute(
                onKakaoLoginClick = {},
                onBrowseWithoutLoginClick = { stage = Stage.HOME },
                onPrivacyPolicyClick = {},
            )
        }
    }

    AnimatedVisibility(
        visible = stage == Stage.HOME,
        enter = fadeIn(animationSpec = tween(durationMillis = 400)),
    ) {
        ConsumerHomeScreen()
    }
}

@OptIn(ExperimentalNaverMapApi::class)
@Composable
private fun ConsumerHomeScreen() {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            MangroDefaultStartAlignedTopAppBar(
                title = {
                    Text(
                        text = "찜 내역",
                        modifier = Modifier.fillMaxWidth(),
                        style = MangroTheme.typography.heading.headingM,
                    )
                },
            )
        },
        bottomBar = {
            BottomAppBar(
                menus = Menu.entries.toPersistentList(),
                currentMenu = Menu.HOME,
                onMenuClick = { /* TODO() */ },
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center,
        ) {
            NaverMap(modifier = Modifier.fillMaxSize())
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MainScreenPreview() {
    MangroTheme {
        MainScreen()
    }
}
