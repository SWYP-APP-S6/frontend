package com.swyp.mangro.feature.auth.terms

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.swyp.mangro.core.designsystem.component.MangroButton
import com.swyp.mangro.core.designsystem.component.MangroButtonStyle
import com.swyp.mangro.core.designsystem.component.MangroCheckbox
import com.swyp.mangro.core.designsystem.component.appbar.MangroDefaultStartAlignedTopAppBar
import com.swyp.mangro.core.designsystem.theme.Gray600
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.feature.auth.R

private fun TermsType.toLabelRes(): Int = when (this) {
    TermsType.SERVICE -> R.string.terms_service
    TermsType.PRIVACY -> R.string.terms_privacy
    TermsType.LOCATION -> R.string.terms_location
    TermsType.PRIVACY_THIRD_PARTY -> R.string.terms_privacy_third_party
    TermsType.MARKETING -> R.string.terms_marketing
}

@Composable
fun TermsScreen(
    uiState: TermsUiState,
    onAction: (TermsUiAction) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MangroTheme.colors.textOnBrandWhite,
        topBar = {
            MangroDefaultStartAlignedTopAppBar(
                title = {},
                navigationIcon = {
                    Icon(
                        imageVector = ImageVector.vectorResource(com.swyp.mangro.core.designsystem.R.drawable.ic_arrow_left),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onBackClick,
                        ),
                    )
                },
            )
        },
        bottomBar = {
            MangroButton(
                text = stringResource(if (uiState.loadFailed) R.string.auth_retry else R.string.terms_confirm_button),
                onClick = { onAction(if (uiState.loadFailed) TermsUiAction.RetryClicked else TermsUiAction.ConfirmClicked) },
                style = MangroButtonStyle.ACTIVE,
                enabled = (uiState.loadFailed || uiState.isRequiredAllChecked) && !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(
                        top = 20.dp,
                        bottom = 12.dp,
                        start = 20.dp,
                        end = 20.dp,
                    ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 20.dp,
                        vertical = 40.dp,
                    ),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = stringResource(R.string.terms_title_prefix),
                    style = MangroTheme.typography.heading.headingM,
                    color = MangroTheme.colors.textTitle,
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = stringResource(R.string.terms_title_suffix),
                        style = MangroTheme.typography.heading.headingM,
                        color = MangroTheme.colors.textTitle,
                    )

                    Image(
                        painter = painterResource(com.swyp.mangro.core.designsystem.R.drawable.ic_memo),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(
                modifier = Modifier
                    .fillMaxWidth(),
            ) {
                AllAgreeRow(
                    isChecked = uiState.isAllChecked,
                    onCheckedChange = { onAction(TermsUiAction.AllAgreeClicked) },
                )

                uiState.items.forEach { item ->
                    TermsRow(
                        item = item,
                        onCheckedChange = { onAction(TermsUiAction.ItemToggled(item.type)) },
                        onDetailClick = { onAction(TermsUiAction.ItemDetailClicked(item.type)) },
                    )
                }
            }
        }
    }
}

@Composable
private fun AllAgreeRow(
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MangroTheme.colors.primaryLight)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onCheckedChange(!isChecked) },
            )
            .padding(
                start = 5.dp,
                end = 20.dp,
                top = 4.dp,
                bottom = 4.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MangroCheckbox(
            isChecked = isChecked,
            onCheckedChange = onCheckedChange,
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = stringResource(R.string.terms_all_agree),
            style = MangroTheme.typography.title.titleS ?: MangroTheme.typography.title.titleM,
            color = MangroTheme.colors.textTitle,
            modifier = Modifier.weight(1f),
        )

        Icon(
            imageVector = ImageVector.vectorResource(com.swyp.mangro.core.designsystem.R.drawable.ic_chevron_right),
            contentDescription = null,
            tint = Gray600,
        )
    }
}

@Composable
private fun TermsRow(
    item: TermsItem,
    onCheckedChange: (Boolean) -> Unit,
    onDetailClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDetailClick,
            )
            .padding(
                start = 5.dp,
                end = 20.dp,
                top = 4.dp,
                bottom = 4.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MangroCheckbox(
            isChecked = item.isChecked,
            onCheckedChange = onCheckedChange,
        )

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = if (item.title.isBlank()) stringResource(item.type.toLabelRes()) else stringResource(if (item.required) R.string.terms_required_title else R.string.terms_optional_title, item.title),
            style = MangroTheme.typography.body.bodyM,
            color = MangroTheme.colors.textTitle,
            modifier = Modifier.weight(1f),
        )

        Icon(
            imageVector = ImageVector.vectorResource(com.swyp.mangro.core.designsystem.R.drawable.ic_chevron_right),
            contentDescription = null,
            tint = Gray600,
        )
    }
}

private class TermsUiStatePreviewProvider : PreviewParameterProvider<TermsUiState> {
    override val values: Sequence<TermsUiState>
        get() = sequenceOf(
            TermsUiState(),
            TermsUiState(
                items = TermsType.entries.map { TermsItem(type = it, isChecked = true) },
            ),
            TermsUiState(
                items = TermsType.entries.map {
                    TermsItem(type = it, isChecked = it != TermsType.MARKETING)
                },
            ),
        )
}

@Preview(showBackground = true)
@Composable
private fun TermsScreenPreview(
    @PreviewParameter(TermsUiStatePreviewProvider::class) uiState: TermsUiState,
) {
    MangroTheme {
        TermsScreen(
            uiState = uiState,
            onAction = {},
            onBackClick = {},
        )
    }
}
