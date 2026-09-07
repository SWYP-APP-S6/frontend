package com.swyp.mangro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.swyp.mangro.core.designsystem.component.appbar.BottomAppBar
import com.swyp.mangro.core.designsystem.component.appbar.MangroDefaultStartAlignedTopAppBar
import com.swyp.mangro.core.designsystem.component.appbar.Menu
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import kotlinx.collections.immutable.toPersistentList

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MangroTheme {
                MainScreen()
            }
        }
    }
}

@Composable
private fun MainScreen() {
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
            Greeting()
        }
    }
}

@Composable
private fun Greeting() {
    Text(
        text = stringResource(R.string.greeting_message),
        color = MangroTheme.colors.primaryNormal,
    )
}

@Preview(showBackground = true)
@Composable
private fun MainScreenPreview() {
    MangroTheme {
        MainScreen()
    }
}
