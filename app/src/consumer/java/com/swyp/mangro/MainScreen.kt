package com.swyp.mangro

import android.content.Intent
import androidx.compose.runtime.Composable
import com.swyp.mangro.navigation.AppNavGraph

@Composable
@Suppress("UNUSED_PARAMETER")
internal fun MainScreen(notificationIntent: Intent? = null) {
    AppNavGraph()
}
