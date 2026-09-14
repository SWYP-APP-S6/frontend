package com.swyp.mangro.feature.owner.setting.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.swyp.mangro.core.designsystem.component.appbar.MangroDefaultStartAlignedTopAppBar
import com.swyp.mangro.core.designsystem.component.appbar.OwnerBottomAppBar
import com.swyp.mangro.core.designsystem.component.appbar.OwnerMenu
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.feature.owner.setting.R
import com.swyp.mangro.feature.owner.setting.component.PolicyList
import com.swyp.mangro.feature.owner.setting.component.StoreInformationCard
import com.swyp.mangro.feature.owner.setting.model.OwnerPolicy

@Composable
internal fun OwnerSettingRoute(
    navigateToHome: () -> Unit,
    navigateToProducts: () -> Unit,
    navigateToPolicy: (OwnerPolicy) -> Unit,
    viewModel: OwnerSettingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    BackHandler { viewModel.handleAction(OwnerSettingAction.NavigationBackClicked) }
    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                OwnerSettingEvent.NavigateToHome -> navigateToHome()
                OwnerSettingEvent.NavigateToProducts -> navigateToProducts()
                is OwnerSettingEvent.NavigateToPolicy -> navigateToPolicy(event.policy)
            }
        }
    }
    OwnerSettingScreen(uiState = uiState, onAction = viewModel::handleAction)
}

@Composable
fun OwnerSettingScreen(
    uiState: OwnerSettingUiState,
    onAction: (OwnerSettingAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MangroTheme.colors.surfaceAlter,
        topBar = {
            MangroDefaultStartAlignedTopAppBar(
                modifier = Modifier.background(MangroTheme.colors.surfaceNormal),
                title = {
                    Text(
                        text = stringResource(R.string.owner_setting_title),
                        style = MangroTheme.typography.label.labelL,
                        color = MangroTheme.colors.textTitle,
                    )
                },
            )
        },
        bottomBar = {
            OwnerBottomAppBar(
                currentMenu = OwnerMenu.SETTINGS,
                onMenuClick = { onAction(OwnerSettingAction.MenuClicked(it)) },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SettingSectionTitle(stringResource(R.string.owner_setting_store_info))
                    StoreInformationCard(storeName = uiState.storeName, storePhone = uiState.storePhone)
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SettingSectionTitle(stringResource(R.string.owner_setting_policy_info))
                    PolicyList(onPolicyClick = { onAction(OwnerSettingAction.PolicyClicked(it)) })
                }
            }
        }
    }
}

@Composable
private fun SettingSectionTitle(text: String) {
    Text(text = text, style = MangroTheme.typography.title.titleM, color = MangroTheme.colors.textTitle)
}
