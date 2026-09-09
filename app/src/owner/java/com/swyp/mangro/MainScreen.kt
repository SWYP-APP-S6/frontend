package com.swyp.mangro

import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.swyp.mangro.core.designsystem.R as designSystemR
import com.swyp.mangro.core.designsystem.component.appbar.BottomAppBarContainer
import com.swyp.mangro.core.designsystem.component.appbar.BottomAppBarItem
import com.swyp.mangro.core.designsystem.theme.OwnerMangroTypography
import com.swyp.mangro.feature.owner.home.screen.OwnerHomeScreen
import com.swyp.mangro.feature.owner.home.screen.model.OwnerHomeAction
import com.swyp.mangro.theme.MangroTheme
import kotlinx.coroutines.launch

@Composable
internal fun MainScreen() {
    val context = LocalContext.current
    val initialState = remember { initialOwnerHomeState(context) }
    var attentionDismissed by rememberSaveable { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val unavailable = stringResource(R.string.owner_feature_preparing)
    val showUnavailable: () -> Unit = {
        snackbar.currentSnackbarData?.dismiss()
        scope.launch { snackbar.showSnackbar(unavailable) }
    }

    MangroTheme {
        OwnerHomeScreen(
            state = initialState.copy(isAttentionDismissed = attentionDismissed),
            onAction = { action ->
                when (action) {
                    OwnerHomeAction.DismissAttention -> attentionDismissed = true
                    else -> showUnavailable()
                }
            },
            bottomBar = {
                OwnerBottomAppBar(
                    currentMenu = OwnerMenu.HOME,
                    onMenuClick = { if (it != OwnerMenu.HOME) showUnavailable() },
                )
            },
            snackbarHost = { SnackbarHost(snackbar) },
        )
    }
}

enum class OwnerMenu { HOME, STORE, SETTINGS }

@Composable
private fun OwnerBottomAppBar(
    currentMenu: OwnerMenu,
    onMenuClick: (OwnerMenu) -> Unit,
    modifier: Modifier = Modifier,
) {
    BottomAppBarContainer(modifier) {
        OwnerMenu.entries.forEach { menu ->
            BottomAppBarItem(
                isSelected = currentMenu == menu,
                drawResId = when (menu) {
                    OwnerMenu.HOME -> designSystemR.drawable.ic_home
                    OwnerMenu.STORE -> designSystemR.drawable.ic_owner_manage
                    OwnerMenu.SETTINGS -> designSystemR.drawable.ic_owner_settings
                },
                stringResId = when (menu) {
                    OwnerMenu.HOME -> designSystemR.string.bottom_app_bar_home
                    OwnerMenu.STORE -> designSystemR.string.owner_bottom_store
                    OwnerMenu.SETTINGS -> designSystemR.string.owner_bottom_settings
                },
                onClick = { onMenuClick(menu) },
                textStyle = OwnerMangroTypography.title.titleS ?: OwnerMangroTypography.title.titleM,
            )
        }
    }
}
