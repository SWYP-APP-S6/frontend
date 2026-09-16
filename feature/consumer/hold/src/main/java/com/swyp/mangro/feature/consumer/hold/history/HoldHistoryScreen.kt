package com.swyp.mangro.feature.consumer.hold.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.swyp.mangro.core.designsystem.component.appbar.ConsumerBottomAppBar
import com.swyp.mangro.core.designsystem.component.appbar.ConsumerMenu
import com.swyp.mangro.core.designsystem.component.appbar.MangroDefaultStartAlignedTopAppBar
import com.swyp.mangro.core.designsystem.component.card.wishlist.WishHistoryCard
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.feature.consumer.hold.R.string
import kotlinx.collections.immutable.persistentListOf

@Composable
fun HoldHistoryScreen(
    uiState: HoldHistoryUiState,
    onAction: (HoldHistoryUiAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MangroTheme.colors.surfaceNormal,
        topBar = {
            MangroDefaultStartAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(string.hold_history_title),
                        style = MangroTheme.typography.heading.headingXXS,
                        color = MangroTheme.colors.grayScale900,
                    )
                },
            )
        },
        bottomBar = {
            ConsumerBottomAppBar(
                menus = persistentListOf(ConsumerMenu.HOME, ConsumerMenu.WISH_LIST, ConsumerMenu.MY),
                currentMenu = ConsumerMenu.WISH_LIST,
                onMenuClick = { menu -> onAction(HoldHistoryUiAction.OnMenuClick(menu)) },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            if (uiState.inProgressItems.isNotEmpty()) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(string.hold_history_in_progress_title),
                        style = MangroTheme.typography.title.titleS ?: MangroTheme.typography.title.titleM,
                        color = MangroTheme.colors.textTitle,
                        modifier = Modifier.padding(horizontal = 20.dp),
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    uiState.inProgressItems.forEach { item ->
                        WishHistoryCard(
                            item = item,
                            modifier = Modifier.clickable {
                                onAction(HoldHistoryUiAction.OnItemClick(item.id))
                            },
                        )
                    }
                }

                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(MangroTheme.colors.surfaceAlter),
                )
            }

            Column {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(string.hold_history_past_title),
                    style = MangroTheme.typography.title.titleS ?: MangroTheme.typography.title.titleM,
                    color = MangroTheme.colors.textSubtitle,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )

                Spacer(modifier = Modifier.height(16.dp))

                uiState.pastItems.forEach { item ->
                    WishHistoryCard(
                        item = item,
                        modifier = Modifier.clickable {
                            onAction(HoldHistoryUiAction.OnItemClick(item.id))
                        },
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HoldHistoryScreenPreview() {
    MangroTheme {
        HoldHistoryScreen(
            uiState = dummyHoldHistoryUiState,
            onAction = {},
        )
    }
}
