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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
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
import com.swyp.mangro.feature.owner.setting.component.StoreInformationCard
import com.swyp.mangro.feature.owner.setting.model.SettingsMenu
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

@Composable
internal fun OwnerSettingRoute(
    navigateToLogin: () -> Unit,
    navigateToHome: () -> Unit,
    navigateToProducts: () -> Unit,
    navigateToPolicy: (SettingsMenu) -> Unit,
    viewModel: OwnerSettingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showLogOutConfirmDialog by remember { mutableStateOf(false) }
    var showLogOutErrorDialog by remember { mutableStateOf(false) }

    var showWithdrawConfirmDialog by remember { mutableStateOf(false) }
    var showWithdrawErrorDialog by remember { mutableStateOf(false) }

    BackHandler { viewModel.handleAction(OwnerSettingAction.NavigationBackClicked) }

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                OwnerSettingEvent.NavigateToLogin -> navigateToLogin()
                OwnerSettingEvent.NavigateToHome -> navigateToHome()
                OwnerSettingEvent.NavigateToProducts -> navigateToProducts()
                is OwnerSettingEvent.NavigateToPolicy -> navigateToPolicy(event.policy)
                OwnerSettingEvent.ShowLogoutConfirmDialog -> showLogOutConfirmDialog = true
                OwnerSettingEvent.ShowWithdrawConfirmDialog -> showWithdrawConfirmDialog = true
                OwnerSettingEvent.ShowLogoutErrorDialog -> showLogOutErrorDialog = true
                OwnerSettingEvent.ShowWithdrawErrorDialog -> showWithdrawErrorDialog = true
            }
        }
    }

    MangroDialogContainer(
        show = showLogOutConfirmDialog,
        onDismissRequest = { showLogOutConfirmDialog = false },
        title = { Text(stringResource(R.string.owner_setting_logout_confirmation)) },
        actions = {
            MangroButton(
                text = stringResource(R.string.owner_setting_logout),
                onClick = {
                    viewModel.handleAction(OwnerSettingAction.LogoutConfirmed)
                    showLogOutConfirmDialog = false
                },
                style = MangroButtonStyle.DESTRUCTIVE,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoggingOut,
            )
            MangroButton(
                text = stringResource(R.string.owner_setting_cancel),
                onClick = { showLogOutConfirmDialog = false },
                style = MangroButtonStyle.DEFAULT,
                modifier = Modifier.fillMaxWidth(),
            )
        },
    )

    MangroDialogContainer(
        show = showLogOutErrorDialog,
        onDismissRequest = { showLogOutErrorDialog = false },
        title = { Text(stringResource(R.string.owner_setting_logout_error)) },
        actions = {
            MangroButton(
                text = stringResource(R.string.owner_setting_confirm),
                onClick = { showLogOutErrorDialog = false },
                style = MangroButtonStyle.ACTIVE,
                modifier = Modifier.fillMaxWidth(),
            )
        },
    )

    MangroDialogContainer(
        show = showWithdrawConfirmDialog,
        onDismissRequest = { showWithdrawConfirmDialog = false },
        title = { Text(stringResource(R.string.owner_setting_withdraw_confirmation)) },
        content = {
            Text(
                text = stringResource(R.string.owner_setting_withdraw_confirmation_content),
                textAlign = TextAlign.Center,
                style = MangroTheme.typography.body.bodyM.copy(lineBreak = LineBreak.Paragraph),
            )
        },
        actions = {
            MangroButton(
                text = stringResource(R.string.owner_setting_withdraw),
                onClick = {
                    viewModel.handleAction(OwnerSettingAction.WithdrawConfirmed)
                    showWithdrawConfirmDialog = false
                },
                style = MangroButtonStyle.DESTRUCTIVE,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoggingOut,
            )
            MangroButton(
                text = stringResource(R.string.owner_setting_cancel),
                onClick = { showWithdrawConfirmDialog = false },
                style = MangroButtonStyle.DEFAULT,
                modifier = Modifier.fillMaxWidth(),
            )
        },
    )

    MangroDialogContainer(
        show = showWithdrawErrorDialog,
        onDismissRequest = { showWithdrawErrorDialog = false },
        title = { Text(stringResource(R.string.owner_setting_withdraw_error)) },
        actions = {
            MangroButton(
                text = stringResource(R.string.owner_setting_confirm),
                onClick = { showWithdrawErrorDialog = false },
                style = MangroButtonStyle.ACTIVE,
                modifier = Modifier.fillMaxWidth(),
            )
        },
    )

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
                onMenuClick = { onAction(OwnerSettingAction.NavigationMenuClicked(it)) },
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
                    Text(
                        text = stringResource(R.string.owner_setting_store_info),
                        style = MangroTheme.typography.title.titleM,
                        color = MangroTheme.colors.textTitle,
                    )
                    StoreInformationCard(
                        storeName = uiState.storeName,
                        storePhone = uiState.storePhone,
                    )
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = stringResource(R.string.owner_setting_policy_info),
                        style = MangroTheme.typography.title.titleM,
                        color = MangroTheme.colors.textTitle,
                    )
                    SettingsMenuContent(
                        items = persistentListOf(SettingsMenu.TERMS_OF_SERVICE, SettingsMenu.PRIVACY_POLICY),
                        onItemClick = { onAction(OwnerSettingAction.SettingsMenuClicked(it)) },
                    )
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = stringResource(R.string.owner_setting_account_management),
                        style = MangroTheme.typography.title.titleM,
                        color = MangroTheme.colors.textTitle,
                    )
                    SettingsMenuContent(
                        items = persistentListOf(SettingsMenu.LOGOUT, SettingsMenu.WITHDRAW),
                        onItemClick = { onAction(OwnerSettingAction.SettingsMenuClicked(it)) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsMenuContent(
    items: PersistentList<SettingsMenu>,
    modifier: Modifier = Modifier,
    onItemClick: (SettingsMenu) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MangroTheme.colors.surfaceNormal),
    ) {
        items.forEachIndexed { index, item ->
            if (index > 0) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    color = MangroTheme.colors.borderSubtle,
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(role = Role.Button, onClick = { onItemClick(item) })
                    .heightIn(min = 64.dp)
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = stringResource(item.titleRes),
                    modifier = Modifier.weight(1f),
                    style = MangroTheme.typography.body.bodyM,
                    color = if (item == SettingsMenu.WITHDRAW || item == SettingsMenu.LOGOUT) {
                        MangroTheme.colors.dangerNormal
                    } else {
                        MangroTheme.colors.textTitle
                    },
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
