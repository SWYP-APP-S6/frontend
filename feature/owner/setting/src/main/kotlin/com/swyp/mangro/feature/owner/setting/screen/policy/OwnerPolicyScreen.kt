package com.swyp.mangro.feature.owner.setting.screen.policy

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mikepenz.markdown.compose.components.markdownComponents
import com.mikepenz.markdown.compose.elements.MarkdownTable
import com.mikepenz.markdown.compose.elements.MarkdownTableHeader
import com.mikepenz.markdown.compose.elements.MarkdownTableRow
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography
import com.swyp.mangro.core.designsystem.R as DesignR
import com.swyp.mangro.core.designsystem.component.MangroButton
import com.swyp.mangro.core.designsystem.component.MangroButtonStyle
import com.swyp.mangro.core.designsystem.component.appbar.MangroDefaultStartAlignedTopAppBar
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.feature.owner.setting.R

@Composable
internal fun OwnerPolicyRoute(
    navigateBack: () -> Unit,
    viewModel: OwnerPolicyViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    BackHandler { viewModel.handleAction(OwnerPolicyAction.NavigationBackClicked) }
    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                OwnerPolicyEvent.NavigateBack -> navigateBack()
            }
        }
    }
    OwnerPolicyScreen(uiState = uiState, onAction = viewModel::handleAction)
}

@Composable
fun OwnerPolicyScreen(
    uiState: OwnerPolicyUiState,
    onAction: (OwnerPolicyAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MangroTheme.colors.surfaceNormal,
        topBar = {
            MangroDefaultStartAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(uiState.policy.titleRes),
                        style = MangroTheme.typography.label.labelL,
                        color = MangroTheme.colors.textTitle,
                    )
                },
                navigationIcon = {
                    Icon(
                        painter = painterResource(DesignR.drawable.ic_arrow_left),
                        contentDescription = stringResource(R.string.owner_setting_back),
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .size(24.dp)
                            .clickable(role = Role.Button, onClick = { onAction(OwnerPolicyAction.NavigationBackClicked) }),
                        tint = MangroTheme.colors.textTitle,
                    )
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = MangroTheme.colors.primaryStrong)
                    }
                }

                uiState.hasError -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(stringResource(R.string.owner_setting_policy_error))
                        MangroButton(
                            onClick = { onAction(OwnerPolicyAction.RetryClicked) },
                            text = stringResource(R.string.owner_setting_retry),
                            style = MangroButtonStyle.ACTIVE,
                        )
                    }
                }

                else -> {
                    SelectionContainer {
                        Markdown(
                            content = uiState.content,
                            colors = markdownColor(text = MangroTheme.colors.textBody),
                            typography = markdownTypography(paragraph = MangroTheme.typography.body.bodyM),
                            components = termsMarkdownComponents,
                        )
                    }
                }
            }
        }
    }
}

private val termsMarkdownComponents = markdownComponents(
    table = { model ->
        MarkdownTable(
            content = model.content,
            node = model.node,
            style = model.typography.table,
            headerBlock = { content, header, tableWidth, style ->
                MarkdownTableHeader(
                    content = content,
                    header = header,
                    tableWidth = tableWidth,
                    style = style,
                    maxLines = Int.MAX_VALUE,
                    overflow = TextOverflow.Clip,
                )
            },
            rowBlock = { content, row, tableWidth, style ->
                MarkdownTableRow(
                    content = content,
                    header = row,
                    tableWidth = tableWidth,
                    style = style,
                    maxLines = Int.MAX_VALUE,
                    overflow = TextOverflow.Clip,
                )
            },
        )
    },
)
