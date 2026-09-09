package com.swyp.mangro.feature.owner.product

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.swyp.mangro.core.designsystem.R as DesignR
import com.swyp.mangro.core.designsystem.component.MangroButton
import com.swyp.mangro.core.designsystem.component.MangroButtonStyle
import com.swyp.mangro.core.designsystem.component.appbar.MangroDefaultStartAlignedTopAppBar
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import java.text.NumberFormat
import java.util.Locale

internal fun Int.won(): String = "${NumberFormat.getIntegerInstance(Locale.KOREA).format(this)}원"

@Composable
internal fun ProductPage(
    title: String,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    bottom: @Composable () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    Scaffold(
        modifier = modifier.imePadding(),
        containerColor = MangroTheme.colors.surfaceNormal,
        topBar = {
            MangroDefaultStartAlignedTopAppBar(
                title = { Text(title, style = MangroTheme.typography.title.titleM, color = MangroTheme.colors.textTitle) },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack, modifier = Modifier.size(32.dp)) {
                            Icon(painterResource(DesignR.drawable.ic_arrow_left), "뒤로", tint = MangroTheme.colors.textTitle)
                        }
                    }
                },
            )
        },
        bottomBar = {
            Column(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 20.dp, vertical = 12.dp)) {
                bottom()
            }
        },
    ) { padding ->
        Column(
            Modifier.padding(padding).verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            content = content,
        )
    }
}

@Composable
internal fun ProductHeading(text: String) {
    Text(text, style = MangroTheme.typography.heading.headingM, color = MangroTheme.colors.textTitle)
}

@Composable
internal fun ProductLabel(text: String, hint: String? = null, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text, style = MangroTheme.typography.title.titleM, color = MangroTheme.colors.textTitle)
        if (hint != null) ProductHint(hint)
    }
}

@Composable
internal fun ProductHint(text: String) {
    Text(text, style = MangroTheme.typography.caption.captionS, color = MangroTheme.colors.textSubtitle)
}

@Composable
internal fun ProductCta(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    MangroButton(text, onClick, MangroButtonStyle.ACTIVE, modifier.fillMaxWidth(), enabled)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ProductSheet(onDismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MangroTheme.colors.surfaceNormal,
    ) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            content = content,
        )
    }
}

@Composable
internal fun ProductConfirmation(
    title: String,
    description: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    ProductSheet(onDismiss) {
        ProductHeading(title)
        ProductHint(description)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MangroButton("아니요", onDismiss, MangroButtonStyle.OUTLINED)
            ProductCta("네, 맞아요", onConfirm, Modifier.weight(1f))
        }
    }
}
