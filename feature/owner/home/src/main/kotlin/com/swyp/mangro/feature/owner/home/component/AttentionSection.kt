package com.swyp.mangro.feature.owner.home.component

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.feature.owner.home.R
import com.swyp.mangro.feature.owner.home.component.card.AttentionCard
import com.swyp.mangro.feature.owner.home.screen.OwnerHomeAction

@Composable
fun AttentionSection(
    hasAttention: Boolean,
    isShowAttention: Boolean,
    cancellationRequiredCount: Int,
    needsPickupConfirmation: Boolean,
    modifier: Modifier = Modifier,
    onAction: (OwnerHomeAction) -> Unit,
) {
    Crossfade(isShowAttention) { state ->
        if (state) {
            AttentionCard(
                cancellationRequiredCount = cancellationRequiredCount,
                needsPickupConfirmation = needsPickupConfirmation,
                modifier = modifier.padding(horizontal = 20.dp, vertical = 24.dp),
                onAction = onAction,
            )
        } else {
            Column(modifier = modifier.padding(horizontal = 20.dp, vertical = 40.dp)) {
                Text(
                    text = stringResource(R.string.owner_home_greeting),
                    style = MangroTheme.typography.heading.headingXXS,
                    color = MangroTheme.colors.textTitle,
                )
                if (!hasAttention) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.owner_home_no_issues),
                        style = MangroTheme.typography.caption.captionS,
                        color = MangroTheme.colors.textSubtitle,
                    )
                }
            }
        }
    }
}
