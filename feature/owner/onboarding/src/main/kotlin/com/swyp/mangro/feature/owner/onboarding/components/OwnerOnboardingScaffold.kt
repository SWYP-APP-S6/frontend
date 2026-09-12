package com.swyp.mangro.feature.owner.onboarding.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swyp.mangro.core.designsystem.R as DesignR
import com.swyp.mangro.core.designsystem.component.MangroButton
import com.swyp.mangro.core.designsystem.component.MangroButtonStyle
import com.swyp.mangro.core.designsystem.component.appbar.MangroDefaultStartAlignedTopAppBar
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.feature.owner.onboarding.R

@Composable
internal fun OwnerOnboardingScaffold(
    buttonLabel: String,
    enabled: Boolean,
    onContinue: () -> Unit,
    onBack: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Scaffold(
        modifier = Modifier
            .imePadding()
            .fillMaxSize(),
        topBar = {
            MangroDefaultStartAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.store_info),
                        color = MangroTheme.colors.grayScale900,
                        style = MangroTheme.typography.heading.headingS.copy(fontSize = 18.sp),
                    )
                },
                navigationIcon = {
                    Icon(
                        painter = painterResource(DesignR.drawable.ic_arrow_left),
                        contentDescription = stringResource(R.string.back),
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .size(24.dp)
                            .clickable(role = Role.Button, onClick = onBack),
                        tint = MangroTheme.colors.textTitle,
                    )
                },
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                MangroButton(
                    onClick = onContinue,
                    style = MangroButtonStyle.ACTIVE,
                    enabled = enabled,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = buttonLabel,
                        style = MangroTheme.typography.title.titleL,
                    )
                }
            }
        },
        containerColor = MangroTheme.colors.surfaceNormal,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(40.dp),
            content = content,
        )
    }
}
