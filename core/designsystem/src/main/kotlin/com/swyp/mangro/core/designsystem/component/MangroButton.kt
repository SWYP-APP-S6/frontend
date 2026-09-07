package com.swyp.mangro.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.core.designsystem.theme.White

enum class MangroButtonStyle {
    DEFAULT,
    ACTIVE,
    GHOST,
    SUBTLE,
    TEXT,
}

private val MangroButtonStyle.textColor: Color
    @Composable
    get() = when (this) {
        MangroButtonStyle.DEFAULT -> MangroTheme.colors.textBody
        MangroButtonStyle.ACTIVE -> MangroTheme.colors.textOnBrandWhite
        MangroButtonStyle.GHOST -> MangroTheme.colors.textCanceled
        else -> MangroTheme.colors.primaryNormal
    }

private val MangroButtonStyle.backgroundColor: Color
    @Composable
    get() = when (this) {
        MangroButtonStyle.DEFAULT -> MangroTheme.colors.surfaceDisabled
        MangroButtonStyle.ACTIVE -> MangroTheme.colors.primaryNormal
        MangroButtonStyle.GHOST -> MangroTheme.colors.textOnBrandWhite
        MangroButtonStyle.SUBTLE -> MangroTheme.colors.primaryLight
        MangroButtonStyle.TEXT -> Color.Transparent
    }

@Composable
fun MangroButton(
    onClick: () -> Unit,
    style: MangroButtonStyle,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    val backgroundColor = when {
        style == MangroButtonStyle.TEXT -> Color.Transparent
        !enabled -> MangroTheme.colors.surfaceDisabled
        else -> style.backgroundColor
    }
    val textColor = if (!enabled) MangroTheme.colors.textCanceled else style.textColor
    val shape = RoundedCornerShape(12.dp)
    val contentPadding = if (style == MangroButtonStyle.TEXT) {
        PaddingValues(10.dp)
    } else {
        PaddingValues(16.dp)
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor)
            .clickable(
                role = Role.Button,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = enabled,
                onClick = onClick,
            )
            .padding(contentPadding),
        contentAlignment = Alignment.Center,
    ) {
        CompositionLocalProvider(LocalContentColor provides textColor) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(
                    space = 2.dp,
                    alignment = Alignment.CenterHorizontally,
                ),
                verticalAlignment = Alignment.CenterVertically,
                content = content,
            )
        }
    }
}

@Composable
fun MangroButton(
    text: String,
    onClick: () -> Unit,
    style: MangroButtonStyle,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    textStyle: TextStyle = MangroTheme.typography.title.titleM,
) {
    MangroButton(
        onClick = onClick,
        style = style,
        modifier = modifier,
        enabled = enabled,
    ) {
        Text(
            text = text,
            color = LocalContentColor.current,
            style = textStyle,
        )
    }
}

@Composable
fun MangroButton(
    label: String,
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    MangroButton(
        onClick = onClick,
        style = MangroButtonStyle.SUBTLE,
        modifier = modifier,
        enabled = enabled,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = label,
                color = LocalContentColor.current,
                style = MangroTheme.typography.title.titleL,
            )

            Text(
                text = "$count",
                color = LocalContentColor.current,
                style = MangroTheme.typography.number.numberM.copy(
                    lineHeight = MangroTheme.typography.title.titleL.lineHeight,
                ),
            )
        }
    }
}

private class MangroButtonPreviewProvider : PreviewParameterProvider<MangroButtonStyle> {
    override val values: Sequence<MangroButtonStyle>
        get() = MangroButtonStyle.entries
            .filter { it != MangroButtonStyle.SUBTLE && it != MangroButtonStyle.TEXT }
            .asSequence()
}

private class MangroTextButtonPreviewProvider : PreviewParameterProvider<Boolean> {
    override val values: Sequence<Boolean>
        get() = sequenceOf(true, false)
}

@Preview
@Composable
private fun MangroButtonPreview(
    @PreviewParameter(MangroButtonPreviewProvider::class) type: MangroButtonStyle,
) {
    MangroTheme {
        Box(
            modifier = Modifier
                .background(White)
                .padding(16.dp),
        ) {
            MangroButton(
                text = type.name,
                onClick = {},
                style = type,
                modifier = Modifier.width(320.dp),
            )
        }
    }
}

@Preview
@Composable
private fun MangroTextButtonPreview(
    @PreviewParameter(MangroTextButtonPreviewProvider::class) enabled: Boolean,
) {
    MangroTheme {
        Box(
            modifier = Modifier
                .background(White)
                .padding(16.dp),
        ) {
            MangroButton(
                text = "주소 복사",
                style = MangroButtonStyle.TEXT,
                onClick = {},
                enabled = enabled,
            )
        }
    }
}

@Preview
@Composable
private fun MangroDisabledButtonPreview() {
    MangroTheme {
        Box(
            modifier = Modifier
                .background(White)
                .padding(16.dp),
        ) {
            MangroButton(
                text = "DISABLED",
                onClick = {},
                style = MangroButtonStyle.DEFAULT,
                modifier = Modifier.width(320.dp),
                enabled = false,
            )
        }
    }
}

@Preview
@Composable
private fun MangroSubtleButtonPreview() {
    MangroTheme {
        Box(
            modifier = Modifier
                .background(White)
                .padding(16.dp),
        ) {
            MangroButton(
                label = "SUBTLE",
                count = 3,
                onClick = {},
                modifier = Modifier.width(320.dp),
            )
        }
    }
}
