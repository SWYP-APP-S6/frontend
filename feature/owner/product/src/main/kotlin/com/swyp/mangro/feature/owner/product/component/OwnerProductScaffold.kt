package com.swyp.mangro.feature.owner.product.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.swyp.mangro.core.designsystem.R as DesignR
import com.swyp.mangro.core.designsystem.component.appbar.MangroDefaultStartAlignedTopAppBar
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.feature.owner.product.R

@Composable
internal fun OwnerProductScaffold(
    title: String,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    contentSpacing: Dp = 32.dp,
    bottomBarContent: @Composable BoxScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .imePadding(),
        topBar = {
            MangroDefaultStartAlignedTopAppBar(
                title = {
                    Text(
                        text = title,
                        style = MangroTheme.typography.title.titleM,
                        color = MangroTheme.colors.textTitle,
                    )
                },
                navigationIcon = {
                    if (onBack != null) {
                        Icon(
                            painter = painterResource(DesignR.drawable.ic_arrow_left),
                            contentDescription = stringResource(R.string.owner_product_back),
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .size(24.dp)
                                .clickable(role = Role.Button, onClick = onBack),
                            tint = MangroTheme.colors.textTitle,
                        )
                    }
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
                content = bottomBarContent,
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(contentSpacing),
            content = content,
        )
    }
}
