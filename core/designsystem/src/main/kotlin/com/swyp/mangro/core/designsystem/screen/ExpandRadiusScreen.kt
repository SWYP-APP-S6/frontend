package com.swyp.mangro.core.designsystem.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.swyp.mangro.core.designsystem.R
import com.swyp.mangro.core.designsystem.component.MangroButton
import com.swyp.mangro.core.designsystem.component.MangroButtonStyle
import com.swyp.mangro.core.designsystem.theme.MangroTheme

/**
 * 주변에 마감상품이 없을 때 호출 화면 위에 표시한다. 시스템 바 여백은 호출부에서 적용한다.
 * 검색 반경을 5km로 변경하고 상품을 다시 조회하는 처리는 [onExpandRadius]의 호출부에서 담당한다.
 */
@Composable
fun ExpandRadiusScreen(
    modifier: Modifier = Modifier,
    onExpandRadius: () -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MangroTheme.colors.surfaceNormal.copy(alpha = 0.59f),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_warning),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(bottom = 12.dp)
                        .size(48.dp),
                    tint = Color(0xFFBDBDBD),
                )
                Text(
                    text = stringResource(R.string.expand_radius_title),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                    color = MangroTheme.colors.textTitle,
                    style = MangroTheme.typography.title.titleL,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(R.string.expand_radius_description),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 23.dp),
                    color = MangroTheme.colors.textSubtitle,
                    style = MangroTheme.typography.caption.captionS.copy(
                        lineHeight = 1.5.em,
                        letterSpacing = (-0.03).em,
                    ),
                    textAlign = TextAlign.Center,
                )
                MangroButton(
                    text = stringResource(R.string.expand_radius_action),
                    onClick = onExpandRadius,
                    style = MangroButtonStyle.ACTIVE,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun ExpandRadiusScreenPreview() {
    MangroTheme {
        ExpandRadiusScreen {}
    }
}
