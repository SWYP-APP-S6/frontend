package com.swyp.mangro.feature.consumer.home.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.swyp.mangro.core.designsystem.R
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.feature.consumer.home.HomeSortOption
import com.swyp.mangro.feature.consumer.home.toLabelRes

@Composable
internal fun SortDropdown(
    selectedOption: HomeSortOption,
    onOptionSelected: (HomeSortOption) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(20.dp)

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { expanded = true },
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(selectedOption.toLabelRes()),
                style = MangroTheme.typography.label.labelS ?: MangroTheme.typography.label.labelM,
                color = MangroTheme.colors.textSubtitle,
            )

            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_chevron_down),
                contentDescription = null,
                tint = MangroTheme.colors.textCanceled,
                modifier = Modifier.size(20.dp),
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            tonalElevation = 0.dp,
            shadowElevation = 6.dp,
            offset = DpOffset(x = 0.dp, y = 8.dp),
            shape = shape,
            containerColor = MangroTheme.colors.textOnBrandWhite,
        ) {
            HomeSortOption.entries.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(option.toLabelRes()),
                            style = MangroTheme.typography.label.labelM,
                            color = if (option == selectedOption) {
                                MangroTheme.colors.primaryNormal
                            } else {
                                MangroTheme.colors.textTitle
                            },
                        )
                    },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    },
                    contentPadding = PaddingValues(horizontal = 16.dp),
                )
            }
        }
    }
}
