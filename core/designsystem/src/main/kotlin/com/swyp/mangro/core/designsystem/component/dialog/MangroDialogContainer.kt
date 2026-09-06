package com.swyp.mangro.core.designsystem.component.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.swyp.mangro.core.designsystem.component.MangroButton
import com.swyp.mangro.core.designsystem.component.MangroButtonStyle
import com.swyp.mangro.core.designsystem.theme.MangroTheme

@Composable
fun MangroDialogContainer(
    show: Boolean,
    onDismissRequest: () -> Unit,
    dialogProperties: DialogProperties = DialogProperties(),
    title: @Composable BoxScope.() -> Unit,
    actions: @Composable ColumnScope.() -> Unit,
    content: @Composable (BoxScope.() -> Unit)? = null,
) {
    if (show) {
        Dialog(
            onDismissRequest = onDismissRequest,
            properties = dialogProperties,
        ) {
            MangroDialogContent(
                title = title,
                content = content,
                actions = actions,
            )
        }
    }
}

@Composable
fun MangroDialogContent(
    title: @Composable BoxScope.() -> Unit,
    actions: @Composable ColumnScope.() -> Unit,
    content: @Composable (BoxScope.() -> Unit)? = null,
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(MangroTheme.colors.surfaceNormal)
            .padding(top = 48.dp, start = 20.dp, end = 20.dp, bottom = 20.dp),
    ) {
        LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
            item {
                CompositionLocalProvider(
                    LocalTextStyle provides MangroTheme.typography.title.titleL.copy(
                        textAlign = TextAlign.Center,
                        lineBreak = LineBreak.Heading,
                        fontWeight = FontWeight(700),
                    ),
                    LocalContentColor provides MangroTheme.colors.textTitle,
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                        content = title,
                    )
                }
            }

            item {
                Spacer(Modifier.height(16.dp))
            }

            item {
                if (content != null) {
                    CompositionLocalProvider(
                        LocalContentColor provides MangroTheme.colors.textSubtitle,
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center,
                            content = content,
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier.padding(top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = actions,
        )
    }
}

@Preview
@Composable
fun DialogPreview() {
    MangroTheme {
        MangroDialogContainer(
            show = true,
            onDismissRequest = { /*TODO*/ },
            title = {
                Text(text = "힝? 속았지?! 약오르G? 화나G? 짜증나지? 못잡겠쥐?")
            },
            content = {
                Text(
                    text = "알림 권한 거부 시, 가게 사정으로 주문이\n 취소되어도 빠르게 확인할 수 없어요.",
                    textAlign = TextAlign.Center,
                    style = MangroTheme.typography.body.bodyM.copy(lineBreak = LineBreak.Paragraph),
                )
            },
            actions = {
                MangroButton(
                    onClick = { },
                    style = MangroButtonStyle.ACTIVE,
                    modifier = Modifier.fillMaxWidth(),
                    content = {
                        Text(
                            text = "네, 맞워요",
                            style = MangroTheme.typography.title.titleL,
                        )
                    },
                )

                MangroButton(
                    onClick = { },
                    style = MangroButtonStyle.DEFAULT,
                    modifier = Modifier.fillMaxWidth(),
                    content = {
                        Text(
                            text = "아니요",
                            style = MangroTheme.typography.title.titleL,
                        )
                    },
                )

                MangroButton(
                    onClick = { },
                    style = MangroButtonStyle.GHOST,
                    modifier = Modifier.fillMaxWidth(),
                    content = {
                        Text(
                            text = "나중에 하기",
                            textDecoration = TextDecoration.Underline,
                            style = MangroTheme.typography.body.bodyM,
                        )
                    },
                )
            },
        )
    }
}
