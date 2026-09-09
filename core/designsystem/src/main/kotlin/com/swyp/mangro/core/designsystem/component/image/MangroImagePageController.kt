package com.swyp.mangro.core.designsystem.component.image

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swyp.mangro.core.designsystem.R
import com.swyp.mangro.core.designsystem.theme.MangroTheme

/** 0부터 시작하는 [currentPage]를 표시합니다. 이미지가 1장 이하면 표시하지 않습니다. */
@Composable
fun MangroImagePageDots(currentPage: Int, pageCount: Int, modifier: Modifier = Modifier) {
    if (pageCount <= 1) return
    require(currentPage in 0 until pageCount)
    val description = stringResource(R.string.image_page_description, currentPage + 1, pageCount)
    Row(
        modifier = modifier.clearAndSetSemantics { contentDescription = description },
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { page ->
            val color by animateColorAsState(
                targetValue = if (page == currentPage) {
                    MangroTheme.colors.grayScale700
                } else {
                    MangroTheme.colors.textOnBrandWhite
                },
                label = "PageDotColor",
            )
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(color, CircleShape),
            )
        }
    }
}

/** 숫자형 pill. 두 자리 이상의 페이지 수와 글꼴 배율에 따라 너비가 늘어납니다. */
@Composable
fun MangroImagePageNumbers(currentPage: Int, pageCount: Int, modifier: Modifier = Modifier) {
    if (pageCount <= 1) return
    require(currentPage in 0 until pageCount)

    Text(
        text = "${currentPage + 1} / $pageCount",
        modifier = modifier
            .widthIn(min = 45.dp)
            .background(MangroTheme.colors.grayScale700, CircleShape)
            .padding(horizontal = 10.dp, vertical = 2.dp),
        color = MangroTheme.colors.textOnBrandWhite,
        style = MangroTheme.typography.label.labelM.copy(
            fontSize = 12.sp,
            lineHeight = 18.sp,
            letterSpacing = (-0.24).sp,
        ),
        textAlign = TextAlign.Center,
        maxLines = 1,
    )
}
