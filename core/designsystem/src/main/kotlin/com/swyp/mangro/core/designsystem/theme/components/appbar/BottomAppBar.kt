package com.swyp.mangro.core.designsystem.theme.components.appbar

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.swyp.mangro.core.designsystem.R
import com.swyp.mangro.core.designsystem.theme.ConsumerMangroBody
import com.swyp.mangro.core.designsystem.theme.defaultMangroColors
import com.swyp.mangro.core.designsystem.theme.utils.dropShadow
import kotlinx.collections.immutable.PersistentList

@Composable
fun BottomAppBar(
    menus: PersistentList<Menu>,
    currentMenu: Menu,
    onMenuClick: (Menu) -> Unit,
) {
    BottomAppBarContainer {
        menus.forEach { menu ->
            BottomAppBarItem(
                isSelected = currentMenu == menu,
                drawResId = menu.iconResId(),
                stringResId = menu.stringResId(),
                onClick = { onMenuClick(menu) },
            )
        }
    }
}

@Composable
private fun BottomAppBarContainer(
    modifier: Modifier = Modifier,
    tabs: @Composable (RowScope.() -> Unit),
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .dropShadow(
                shape = RoundedCornerShape(0.dp),
                color = Color.Black.copy(alpha = 0.12f),
                blur = 4.dp,
                offsetY = 0.dp,
                offsetX = 4.dp,
                spread = 0.dp,
            )
            .background(color = defaultMangroColors.surfaceNormal)
            .windowInsetsPadding(WindowInsets.navigationBars),
        content = tabs,
    )
}

@Composable
private fun RowScope.BottomAppBarItem(
    isSelected: Boolean,
    @DrawableRes drawResId: Int,
    @StringRes stringResId: Int,
    onClick: () -> Unit,
) {
    val tint by animateColorAsState(
        if (isSelected) defaultMangroColors.primaryNormal else defaultMangroColors.grayScale500,
    )

    Column(
        modifier = Modifier
            .weight(1f)
            .height(54.dp)
            .padding(4.dp)
            .clip(RoundedCornerShape(4.dp))
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            painter = painterResource(drawResId),
            tint = tint,
            contentDescription = null,
        )

        Text(
            text = stringResource(stringResId),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = tint,
            style = ConsumerMangroBody.body03,
        )
    }
}

enum class Menu { HOME, WISH_LIST, MY }

@DrawableRes
private fun Menu.iconResId(): Int = when (this) {
    Menu.HOME -> R.drawable.ic_home
    Menu.WISH_LIST -> R.drawable.ic_alarm_on_24px
    Menu.MY -> R.drawable.ic_local_library
}

@StringRes
private fun Menu.stringResId(): Int = when (this) {
    Menu.HOME -> R.string.bottom_app_bar_home
    Menu.WISH_LIST -> R.string.bottom_app_bar_wish_list
    Menu.MY -> R.string.bottom_app_bar_my
}
