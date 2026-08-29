package com.swyp.mangro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.swyp.mangro.core.designsystem.theme.MangroTheme

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
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MangroTheme.colors.surfaceNormal,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding(),
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
