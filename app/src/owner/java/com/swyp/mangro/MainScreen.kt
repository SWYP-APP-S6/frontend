package com.swyp.mangro

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.swyp.mangro.core.designsystem.component.MangroButton
import com.swyp.mangro.core.designsystem.component.MangroButtonStyle
import com.swyp.mangro.core.designsystem.theme.MangroTheme

@Composable
internal fun MainScreen() {
    com.swyp.mangro.theme.MangroTheme {
        OwnerMainScreen()
    }
}

@Composable
private fun OwnerMainScreen() {
    var showPickups by rememberSaveable { mutableStateOf(false) }
    val stateHolder = rememberSaveableStateHolder()
    BackHandler(enabled = showPickups) { showPickups = false }
    if (showPickups) {
        stateHolder.SaveableStateProvider("owner-pickup") {
            OwnerPickupEntry(onHomeClick = { showPickups = false })
        }
    } else {
        Column(
            Modifier.fillMaxSize().safeDrawingPadding().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(stringResource(R.string.greeting_message), color = MangroTheme.colors.primaryNormal)
            MangroButton(stringResource(R.string.owner_pickup_open), { showPickups = true }, MangroButtonStyle.ACTIVE)
            if (IS_PICKUP_DEMO) Text(stringResource(R.string.owner_pickup_demo_notice), style = MangroTheme.typography.caption.captionS)
        }
    }
}
