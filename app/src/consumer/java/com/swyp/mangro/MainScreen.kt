package com.swyp.mangro

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
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
import androidx.compose.ui.tooling.preview.Preview
import com.naver.maps.map.compose.ExperimentalNaverMapApi
import com.naver.maps.map.compose.NaverMap
import com.swyp.mangro.core.designsystem.component.appbar.BottomAppBar
import com.swyp.mangro.core.designsystem.component.appbar.MangroDefaultStartAlignedTopAppBar
import com.swyp.mangro.core.designsystem.component.appbar.Menu
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.feature.splash.SplashRoute
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.delay

@OptIn(ExperimentalNaverMapApi::class)
@Composable
internal fun MainScreen() {
    var iconVisible by remember { mutableStateOf(true) }
    var splashVisible by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(1000.milliseconds)
        iconVisible = false
        delay(400.milliseconds)
        splashVisible = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = !splashVisible,
            enter = fadeIn(animationSpec = tween(durationMillis = 400)),
        ) {
            ConsumerHomeScreen()
        }

        if (splashVisible) {
            SplashRoute(iconVisible = iconVisible)
        }
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
