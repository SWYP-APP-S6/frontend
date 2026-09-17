package com.swyp.mangro.feature.owner.setting.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.swyp.mangro.core.designsystem.R as DesignR
import com.swyp.mangro.core.designsystem.component.MangroButton
import com.swyp.mangro.core.designsystem.component.MangroButtonStyle
import com.swyp.mangro.core.designsystem.component.appbar.MangroDefaultStartAlignedTopAppBar
import com.swyp.mangro.core.designsystem.component.appbar.OwnerBottomAppBar
import com.swyp.mangro.core.designsystem.component.appbar.OwnerMenu
import com.swyp.mangro.core.designsystem.component.dialog.MangroDialogContainer
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.feature.owner.setting.R
import com.swyp.mangro.feature.owner.setting.component.PolicyList
import com.swyp.mangro.feature.owner.setting.component.StoreInformationCard
import com.swyp.mangro.feature.owner.setting.model.OwnerPolicy

@Composable
internal fun OwnerSettingRoute(
    navigateToLogin: () -> Unit,
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
                OwnerSettingEvent.NavigateToLogin -> navigateToLogin()
                OwnerSettingEvent.NavigateToHome -> navigateToHome()
                OwnerSettingEvent.NavigateToProducts -> navigateToProducts()
                is OwnerSettingEvent.NavigateToPolicy -> navigateToPolicy(event.policy)
            }
        }
    }
    OwnerSettingScreen(
        uiState = uiState,
        onAction = viewModel::handleAction,
    )
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
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SettingSectionTitle(stringResource(R.string.owner_setting_store_info))
                    StoreInformationCard(
                        storeName = uiState.storeName,
                        storePhone = uiState.storePhone,
                    )
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SettingSectionTitle(stringResource(R.string.owner_setting_policy_info))
                    PolicyList(onPolicyClick = { onAction(OwnerSettingAction.PolicyClicked(it)) })
                }
            }
            item {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MangroTheme.colors.surfaceNormal)
                            .clickable(enabled = !uiState.isLoggingOut, role = Role.Button, onClick = { onAction(OwnerSettingAction.LogoutClicked) })
                            .heightIn(min = 64.dp)
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = stringResource(if (uiState.isLoggingOut) R.string.owner_setting_logging_out else R.string.owner_setting_logout),
                            modifier = Modifier.weight(1f),
                            style = MangroTheme.typography.body.bodyM,
                            color = MangroTheme.colors.dangerNormal,
                        )
                        Icon(
                            painter = painterResource(DesignR.drawable.ic_arrow_right),
                            contentDescription = null,
                            tint = MangroTheme.colors.textSubtitle,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }
    }

    MangroDialogContainer(
        show = uiState.showLogoutConfirmation,
        onDismissRequest = { onAction(OwnerSettingAction.LogoutDismissed) },
        title = { Text(stringResource(R.string.owner_setting_logout_confirmation)) },
        actions = {
            MangroButton(
                text = stringResource(R.string.owner_setting_logout),
                onClick = { onAction(OwnerSettingAction.LogoutConfirmed) },
                style = MangroButtonStyle.DESTRUCTIVE,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoggingOut,
            )
            MangroButton(
                text = stringResource(R.string.owner_setting_cancel),
                onClick = { onAction(OwnerSettingAction.LogoutDismissed) },
                style = MangroButtonStyle.DEFAULT,
                modifier = Modifier.fillMaxWidth(),
            )
        },
    )

    MangroDialogContainer(
        show = uiState.hasLogoutError,
        onDismissRequest = { onAction(OwnerSettingAction.LogoutErrorDismissed) },
        title = { Text(stringResource(R.string.owner_setting_logout_error)) },
        actions = {
            MangroButton(
                text = stringResource(R.string.owner_setting_confirm),
                onClick = { onAction(OwnerSettingAction.LogoutErrorDismissed) },
                style = MangroButtonStyle.ACTIVE,
                modifier = Modifier.fillMaxWidth(),
            )
        },
    )
}

@Composable
private fun SettingSectionTitle(text: String) {
    Text(text = text, style = MangroTheme.typography.title.titleM, color = MangroTheme.colors.textTitle)
}
