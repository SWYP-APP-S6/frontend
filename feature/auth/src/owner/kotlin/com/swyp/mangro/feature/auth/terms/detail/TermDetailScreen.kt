package com.swyp.mangro.feature.auth.terms.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.swyp.mangro.core.designsystem.component.MangroButton
import com.swyp.mangro.core.designsystem.component.MangroButtonStyle
import com.swyp.mangro.core.designsystem.component.appbar.MangroDefaultStartAlignedTopAppBar
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.data.owner.terms.model.TermsFailure
import com.swyp.mangro.feature.auth.R

@Composable
fun TermDetailScreen(state: TermDetailState, onAction: (TermDetailAction) -> Unit, modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier,
        containerColor = MangroTheme.colors.textOnBrandWhite,
        topBar = {
            MangroDefaultStartAlignedTopAppBar(
                title = { Text(state.detail?.document?.title ?: stringResource(R.string.owner_terms_detail_title)) },
                navigationIcon = {
                    IconButton(onClick = { onAction(TermDetailAction.BackClicked) }) {
                        Icon(ImageVector.vectorResource(com.swyp.mangro.core.designsystem.R.drawable.ic_arrow_left), contentDescription = stringResource(R.string.owner_terms_back))
                    }
                },
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            when {
                state.isLoading -> CircularProgressIndicator()
                state.failure != null -> Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(if (state.failure == TermsFailure.NOT_FOUND) stringResource(R.string.owner_terms_not_found) else stringResource(R.string.owner_terms_load_error))
                    if (state.failure == TermsFailure.NOT_FOUND) {
                        MangroButton(style = MangroButtonStyle.ACTIVE, text = stringResource(R.string.owner_terms_refresh), onClick = { onAction(TermDetailAction.RefreshListClicked) })
                    } else {
                        MangroButton(style = MangroButtonStyle.ACTIVE, text = stringResource(R.string.owner_terms_retry), onClick = { onAction(TermDetailAction.RetryClicked) })
                    }
                }
                state.detail != null -> TermsMarkdownView(markdown = state.detail.contentMarkdown, modifier = Modifier.fillMaxSize())
            }
        }
    }
}
