package com.swyp.mangro.feature.consumer.myinfo

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.swyp.mangro.core.designsystem.R as DesignR
import com.swyp.mangro.core.designsystem.component.MangroButton
import com.swyp.mangro.core.designsystem.component.MangroButtonStyle
import com.swyp.mangro.core.designsystem.component.appbar.ConsumerBottomAppBar
import com.swyp.mangro.core.designsystem.component.appbar.ConsumerMenu
import com.swyp.mangro.core.designsystem.component.appbar.MangroDefaultStartAlignedTopAppBar
import com.swyp.mangro.core.designsystem.component.dialog.MangroDialogContainer
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.data.auth.model.TermsKind
import kotlinx.collections.immutable.persistentListOf

@Composable
fun MyInfoScreen(
    uiState: MyInfoUiState,
    onAction: (MyInfoUiAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            MangroDefaultStartAlignedTopAppBar(
                modifier = Modifier.background(MangroTheme.colors.surfaceNormal),
                title = {
                    Text(
                        text = stringResource(R.string.myinfo_title),
                        style = MangroTheme.typography.label.labelL,
                        color = MangroTheme.colors.textTitle,
                    )
                },
            )
        },
        bottomBar = {
            ConsumerBottomAppBar(
                menus = persistentListOf(ConsumerMenu.HOME, ConsumerMenu.WISH_LIST, ConsumerMenu.MY),
                currentMenu = ConsumerMenu.MY,
                onMenuClick = { if (!uiState.isLoggingOut) onAction(MyInfoUiAction.MenuClicked(it)) },
            )
        },
        containerColor = MangroTheme.colors.surfaceAlter,
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
                    SectionTitle(stringResource(R.string.myinfo_user_info))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = MangroTheme.colors.surfaceNormal,
                                shape = RoundedCornerShape(16.dp),
                            )
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                    ) {
                        when {
                            uiState.isLoading -> {
                            }

                            uiState.hasProfileError -> {
                            }

                            uiState.isGuest -> {
                                Text(
                                    text = stringResource(R.string.myinfo_guest),
                                    style = MangroTheme.typography.title.titleM,
                                    color = MangroTheme.colors.textTitle,
                                )
                                MangroButton(
                                    text = stringResource(R.string.myinfo_link_kakao),
                                    onClick = { onAction(MyInfoUiAction.LinkKakaoClicked) },
                                    style = MangroButtonStyle.ACTIVE,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }

                            else -> {
                                ProfileField(
                                    label = stringResource(R.string.myinfo_nickname),
                                    value = uiState.nickname,
                                )
                            }
                        }
                    }
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionTitle(text = stringResource(R.string.myinfo_policy_info))
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(MangroTheme.colors.surfaceNormal),
                    ) {
                        ActionRow(
                            text = stringResource(R.string.myinfo_terms),
                            enabled = !uiState.isLoggingOut,
                            onClick = { onAction(MyInfoUiAction.PolicyClicked(TermsKind.SERVICE)) },
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            color = MangroTheme.colors.borderSubtle,
                        )
                        ActionRow(
                            text = stringResource(R.string.myinfo_privacy),
                            enabled = !uiState.isLoggingOut,
                            onClick = { onAction(MyInfoUiAction.PolicyClicked(TermsKind.PRIVACY_POLICY)) },
                        )
                    }
                }
            }
            if (!uiState.isLoading && !uiState.isGuest) {
                item {
                    Column(
                        Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(MangroTheme.colors.surfaceNormal),
                    ) {
                        ActionRow(
                            text = stringResource(if (uiState.isLoggingOut) R.string.myinfo_logging_out else R.string.myinfo_logout),
                            enabled = !uiState.isLoggingOut,
                            destructive = true,
                        ) { onAction(MyInfoUiAction.LogoutClicked) }
                    }
                }
            }
        }
    }
    MangroDialogContainer(
        show = uiState.showLogoutConfirmation,
        onDismissRequest = { onAction(MyInfoUiAction.LogoutDismissed) },
        title = { Text(stringResource(R.string.myinfo_logout_confirmation)) },
        actions = {
            MangroButton(
                text = stringResource(R.string.myinfo_logout),
                onClick = { onAction(MyInfoUiAction.LogoutConfirmed) },
                style = MangroButtonStyle.DESTRUCTIVE,
                modifier = Modifier.fillMaxWidth(),
            )
            MangroButton(
                text = stringResource(R.string.myinfo_cancel),
                onClick = { onAction(MyInfoUiAction.LogoutDismissed) },
                style = MangroButtonStyle.DEFAULT,
                modifier = Modifier.fillMaxWidth(),
            )
        },
    )
    MangroDialogContainer(
        show = uiState.hasLogoutError,
        onDismissRequest = { onAction(MyInfoUiAction.LogoutErrorDismissed) },
        title = { Text(stringResource(R.string.myinfo_logout_error)) },
        actions = {
            MangroButton(
                text = stringResource(R.string.myinfo_confirm),
                onClick = { onAction(MyInfoUiAction.LogoutErrorDismissed) },
                style = MangroButtonStyle.ACTIVE,
                modifier = Modifier.fillMaxWidth(),
            )
        },
    )
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MangroTheme.typography.title.titleM, color = MangroTheme.colors.textTitle)
}

@Composable
private fun ProfileField(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = MangroTheme.typography.caption.captionS, color = MangroTheme.colors.textSubtitle)
        Text(
            text = value.ifBlank { stringResource(R.string.myinfo_unregistered) },
            style = MangroTheme.typography.title.titleM,
            color = if (value.isBlank()) MangroTheme.colors.textCanceled else MangroTheme.colors.textTitle,
        )
    }
}

@Composable
private fun ActionRow(text: String, enabled: Boolean, destructive: Boolean = false, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .heightIn(min = 64.dp)
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text,
            modifier = Modifier.weight(1f),
            style = MangroTheme.typography.body.bodyM,
            color = if (destructive) MangroTheme.colors.dangerNormal else MangroTheme.colors.textTitle,
        )
        Icon(painterResource(DesignR.drawable.ic_arrow_right), contentDescription = null, tint = MangroTheme.colors.textSubtitle, modifier = Modifier.size(20.dp))
    }
}

@Preview
@Composable
private fun GuestMyInfoPreview() {
    MangroTheme { MyInfoScreen(MyInfoUiState(isLoading = false, isGuest = true), {}) }
}

@Preview
@Composable
private fun MemberMyInfoPreview() {
    MangroTheme { MyInfoScreen(MyInfoUiState(isLoading = false, nickname = "망그로", phone = "01012345678"), {}) }
}
