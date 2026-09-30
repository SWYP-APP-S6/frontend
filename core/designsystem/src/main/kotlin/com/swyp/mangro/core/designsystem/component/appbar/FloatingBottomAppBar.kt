package com.swyp.mangro.core.designsystem.component.appbar

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.swyp.mangro.core.designsystem.theme.ConsumerMangroBody
import com.swyp.mangro.core.designsystem.theme.MangroTheme

private val barHeight = 76.dp
private val barVerticalMargin = 12.dp

/** Space occupied by the floating bar above the system navigation inset. */
val FloatingBottomAppBarOverlayHeight = barHeight + barVerticalMargin * 2
val LocalFloatingBottomAppBarOverlayHeight = staticCompositionLocalOf { 0.dp }

/** A reusable tab item. Navigation remains with the caller. */
data class FloatingBottomBarItem<T>(
    val id: T,
    @DrawableRes val iconRes: Int,
    @StringRes val labelRes: Int,
)

@Composable
fun <T> FloatingBottomAppBar(
    items: List<FloatingBottomBarItem<T>>,
    selectedItem: T,
    onItemClick: (T) -> Unit,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = ConsumerMangroBody.body03,
) {
    val colors = MangroTheme.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 16.dp, vertical = barVerticalMargin)
            .height(barHeight)
            .shadow(elevation = 10.dp, shape = CircleShape)
            .clip(CircleShape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        colors.surfaceNormal.copy(alpha = 0.82f),
                        colors.grayScale500.copy(alpha = 0.52f),
                    ),
                ),
            )
            .border(1.dp, colors.surfaceNormal.copy(alpha = 0.78f), CircleShape)
            .padding(4.dp)
            .selectableGroup(),
    ) {
        items.forEach { item ->
            val selected = item.id == selectedItem
            val tint by animateColorAsState(
                targetValue = if (selected) colors.primaryNormal else colors.grayScale700,
                label = "FloatingBottomBarItemTint",
            )
            val selectedBackground = if (selected) {
                Modifier
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                colors.nutsNormal.copy(alpha = 0.58f),
                                colors.nutsNormal.copy(alpha = 0.38f),
                            ),
                        ),
                    )
                    .border(1.dp, colors.surfaceNormal.copy(alpha = 0.55f), CircleShape)
            } else {
                Modifier
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .then(selectedBackground)
                    .selectable(
                        selected = selected,
                        role = Role.Tab,
                        onClick = { onItemClick(item.id) },
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    painter = painterResource(item.iconRes),
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(24.dp),
                )
                Text(
                    text = stringResource(item.labelRes),
                    modifier = Modifier.fillMaxWidth(),
                    color = tint,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = textStyle,
                )
            }
        }
    }
}
