package com.swyp.mangro.core.designsystem.component.dropdown

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swyp.mangro.core.designsystem.R
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.core.designsystem.theme.OwnerMangroTypography

/**
 * 필드를 누르면 [options] 목록을 열고 선택 시 닫는다. 바깥 터치와 뒤로 가기도 목록을 닫는다.
 * 호출부는 [onOptionSelected]에서 [selectedOption]을 갱신한다.
 * 값이 없으면 [placeholder]를 표시하며, 빈 목록은 비활성 처리한다.
 */
@Composable
fun MangroDropdownField(
    options: List<String>,
    selectedOption: String?,
    onOptionSelected: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    enabled: Boolean = true,
    textAlign: TextAlign = TextAlign.Start,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    borderWidth: Dp = 1.dp,
    placeholderColor: Color = MangroTheme.colors.textBody,
    iconTint: Color = Color.Unspecified,
) {
    var expanded by remember(enabled, options) { mutableStateOf(false) }
    var anchorWidth by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current

    Box(modifier = modifier) {
        MangroDropdownField(
            text = selectedOption ?: placeholder,
            onClick = { expanded = !expanded },
            modifier = Modifier.onSizeChanged { anchorWidth = it.width },
            expanded = expanded,
            isPlaceholder = selectedOption == null,
            isError = isError,
            enabled = enabled && options.isNotEmpty(),
            textAlign = textAlign,
            contentPadding = contentPadding,
            borderWidth = borderWidth,
            placeholderColor = placeholderColor,
            iconTint = if (iconTint == Color.Unspecified) {
                if (expanded) MangroTheme.colors.grayScale900 else MangroTheme.colors.textCanceled
            } else {
                iconTint
            },
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .width(with(density) { anchorWidth.toDp() })
                .heightIn(max = 280.dp),
            shape = RoundedCornerShape(8.dp),
            containerColor = MangroTheme.colors.surfaceNormal,
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option,
                            color = if (option == selectedOption) MangroTheme.colors.primaryNormal else MangroTheme.colors.textTitle,
                            style = MangroTheme.typography.body.bodyM,
                        )
                    },
                    onClick = {
                        expanded = false
                        onOptionSelected(option)
                    },
                )
            }
        }
    }
}

/**
 * 선택 UI를 여는 필드. 선택창과 값 변경은 호출부에서 관리한다.
 * [expanded]는 선택 UI의 열림 상태이며 키보드 입력 포커스와 무관하다.
 * [isPlaceholder]는 표시 중인 [text]가 기본 안내 값인지 나타낸다.
 * 가운데 정렬에서는 화살표 반대편에도 같은 공간을 확보한다.
 */
@Composable
fun MangroDropdownField(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    expanded: Boolean = false,
    isPlaceholder: Boolean = false,
    isError: Boolean = false,
    enabled: Boolean = true,
    textAlign: TextAlign = TextAlign.Start,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    borderWidth: Dp = 1.dp,
    placeholderColor: Color = MangroTheme.colors.textBody,
    iconTint: Color = if (expanded) MangroTheme.colors.grayScale900 else MangroTheme.colors.textCanceled,
) {
    val shape = RoundedCornerShape(8.dp)
    val borderColor = when {
        !enabled -> MangroTheme.colors.borderDefault
        isError -> MangroTheme.colors.dangerNormal
        expanded -> MangroTheme.colors.borderPrimarySubtle
        else -> MangroTheme.colors.borderDefault
    }
    val textColor = when {
        !enabled -> MangroTheme.colors.textCanceled
        isPlaceholder && !expanded -> placeholderColor
        else -> MangroTheme.colors.textTitle
    }

    Row(
        modifier = modifier
            .clip(shape)
            .background(if (enabled) MangroTheme.colors.surfaceNormal else MangroTheme.colors.surfaceDisabled)
            .border(borderWidth, borderColor, shape)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(contentPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (textAlign == TextAlign.Center) {
            Spacer(Modifier.size(20.dp))
        }
        Text(
            text = text,
            modifier = Modifier.weight(1f),
            color = textColor,
            style = MangroTheme.typography.title.titleM.copy(
                fontSize = 16.sp,
                lineHeight = 24.sp,
                letterSpacing = (-0.16).sp,
            ),
            textAlign = textAlign,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Icon(
            painter = painterResource(R.drawable.ic_dropdown_chevron_down_20px),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = if (enabled) iconTint else MangroTheme.colors.textCanceled,
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun MangroDropdownFieldPickupStatesPreview() {
    MangroTheme(typography = OwnerMangroTypography) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(3) { index ->
                MangroDropdownField(
                    text = "오늘 오후 20:00",
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                    expanded = index == 1,
                    isPlaceholder = index == 0,
                    textAlign = TextAlign.Center,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 16.dp),
                    borderWidth = 1.2.dp,
                    placeholderColor = MangroTheme.colors.textSubtitle,
                    iconTint = MangroTheme.colors.textCanceled,
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun MangroDropdownFieldTimeRangeStatesPreview() {
    MangroTheme(typography = OwnerMangroTypography) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(4) { index ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MangroDropdownField(
                        text = "오전 09:00",
                        onClick = {},
                        modifier = Modifier.weight(1f),
                        expanded = index == 1,
                        isPlaceholder = index == 0 || index == 3,
                    )
                    Text(
                        text = "~",
                        modifier = Modifier.width(32.dp),
                        textAlign = TextAlign.Center,
                        color = MangroTheme.colors.textCanceled,
                    )
                    MangroDropdownField(
                        text = "오후 20:00",
                        onClick = {},
                        modifier = Modifier.weight(1f),
                        expanded = index == 1,
                        isPlaceholder = index == 0,
                        isError = index == 3,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Preview(showBackground = true, widthDp = 320, fontScale = 1.5f)
@Composable
private fun MangroDropdownFieldPickupPreview() {
    MangroTheme(typography = OwnerMangroTypography) {
        var selectedOption by remember { mutableStateOf<String?>(null) }
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            MangroDropdownField(
                options = listOf("오늘 오후 18:00", "오늘 오후 19:00", "오늘 오후 20:00"),
                selectedOption = selectedOption,
                onOptionSelected = { selectedOption = it },
                placeholder = "픽업 종료시간 선택",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 16.dp),
                borderWidth = 1.2.dp,
                placeholderColor = MangroTheme.colors.textSubtitle,
                iconTint = MangroTheme.colors.textCanceled,
            )
            MangroDropdownField(
                text = "선택할 수 없는 시간",
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun MangroDropdownFieldTimeRangePreview() {
    MangroTheme(typography = OwnerMangroTypography) {
        var startTime by remember { mutableStateOf<String?>(null) }
        var endTime by remember { mutableStateOf<String?>(null) }
        Row(Modifier.padding(20.dp).fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            MangroDropdownField(
                options = listOf("오전 09:00", "오전 09:30", "오전 10:00"),
                selectedOption = startTime,
                onOptionSelected = { startTime = it },
                placeholder = "오전 09:00",
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "~",
                modifier = Modifier.width(32.dp),
                textAlign = TextAlign.Center,
                color = MangroTheme.colors.textCanceled,
            )
            MangroDropdownField(
                options = listOf("오후 18:00", "오후 19:00", "오후 20:00"),
                selectedOption = endTime,
                onOptionSelected = { endTime = it },
                placeholder = "오후 20:00",
                modifier = Modifier.weight(1f),
            )
        }
    }
}
