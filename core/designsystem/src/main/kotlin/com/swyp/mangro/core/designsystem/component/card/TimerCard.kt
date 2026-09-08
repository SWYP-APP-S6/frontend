package com.swyp.mangro.core.designsystem.component.card

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swyp.mangro.core.designsystem.R
import com.swyp.mangro.core.designsystem.component.badge.MangroBadge
import com.swyp.mangro.core.designsystem.component.progress.MangroGradientProgressBar
import com.swyp.mangro.core.designsystem.component.progress.MangroProgressBar
import com.swyp.mangro.core.designsystem.theme.Amber100
import com.swyp.mangro.core.designsystem.theme.ConsumerMangroTypography
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.core.designsystem.theme.PretendardFont
import com.swyp.mangro.core.designsystem.theme.White
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

enum class TimerCardPhase {
    DEFAULT,
    EXPIRED,
    CAUTION,
}

@Stable
class TimerCardState(
    requestTimeMillis: Long,
    private val endTimeMillis: Long,
) {
    var nowMillis by mutableLongStateOf(System.currentTimeMillis())
        private set

    private val totalDurationMillis = (endTimeMillis - requestTimeMillis).coerceAtLeast(1L)

    val remainingMillis: Long by derivedStateOf { (endTimeMillis - nowMillis).coerceAtLeast(0L) }

    val phase: TimerCardPhase by derivedStateOf {
        when {
            remainingMillis <= 0L -> TimerCardPhase.EXPIRED
            remainingMillis.toFloat() / totalDurationMillis <= 1f / 3f -> TimerCardPhase.CAUTION
            else -> TimerCardPhase.DEFAULT
        }
    }

    val progress: Float by derivedStateOf {
        (remainingMillis.toFloat() / totalDurationMillis).coerceIn(0f, 1f)
    }

    fun tick() {
        nowMillis = System.currentTimeMillis()
    }
}

@Composable
fun rememberTimerCardState(
    requestTimeMillis: Long,
    endTimeMillis: Long,
): TimerCardState {
    val state = remember(requestTimeMillis, endTimeMillis) {
        TimerCardState(requestTimeMillis, endTimeMillis)
    }

    LaunchedEffect(state) {
        while (isActive && state.phase != TimerCardPhase.EXPIRED) {
            state.tick()
            delay(1.seconds)
        }
    }

    return state
}

private data class TimerCardStyle(
    val background: Color,
    val timeColor: Color,
    val titleRes: Int,
    val badgeTextRes: Int?,
    val badgeColor: Color?,
    val badgeIconRes: Int?,
)

private val TimerCardTimeTextStyle = TextStyle(
    fontFamily = PretendardFont.Semibold,
    fontSize = 40.sp,
    lineHeight = 52.sp,
    letterSpacing = (-0.8).sp,
)

@Composable
private fun TimerCardPhase.toStyle(): TimerCardStyle = when (this) {
    TimerCardPhase.DEFAULT -> TimerCardStyle(
        background = Amber100,
        timeColor = MangroTheme.colors.grayScale900,
        titleRes = R.string.timer_card_active,
        badgeTextRes = R.string.timer_card_badge_relaxed,
        badgeColor = MangroTheme.colors.primaryNormal,
        badgeIconRes = R.drawable.ic_alarm_on_16px,
    )

    TimerCardPhase.EXPIRED -> TimerCardStyle(
        background = MangroTheme.colors.surfaceDisabled,
        timeColor = MangroTheme.colors.textCanceled,
        titleRes = R.string.timer_card_expired,
        badgeTextRes = null,
        badgeColor = null,
        badgeIconRes = null,
    )

    TimerCardPhase.CAUTION -> TimerCardStyle(
        background = MangroTheme.colors.dangerBg,
        timeColor = MangroTheme.colors.dangerNormal,
        titleRes = R.string.timer_card_active,
        badgeTextRes = R.string.timer_card_badge_urgent,
        badgeColor = MangroTheme.colors.dangerNormal,
        badgeIconRes = R.drawable.ic_acute,
    )
}

@Composable
fun TimerCard(
    requestTimeMillis: Long,
    endTimeMillis: Long,
    modifier: Modifier = Modifier,
) {
    val timeState = rememberTimerCardState(requestTimeMillis, endTimeMillis)
    val style = timeState.phase.toStyle()
    val shape = RoundedCornerShape(16.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(style.background)
            .padding(
                vertical = 32.dp,
                horizontal = 16.dp,
            ),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(style.titleRes),
            color = MangroTheme.colors.textTitle,
            style = MangroTheme.typography.label.labelM,
        )

        Text(
            text = formatRemaining(timeState.remainingMillis),
            color = style.timeColor,
            style = TimerCardTimeTextStyle,
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (timeState.phase == TimerCardPhase.EXPIRED) {
            MangroProgressBar(
                progress = 0f,
            )
        } else {
            MangroGradientProgressBar(
                progress = 1f - timeState.progress,
                thresholdValue = 1f / 3f,
            )

            Spacer(modifier = Modifier.height(12.dp))

            val badgeTextRes = style.badgeTextRes
            val badgeColor = style.badgeColor

            if (badgeTextRes != null && badgeColor != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    MangroBadge(
                        text = stringResource(badgeTextRes),
                        containerColor = MangroTheme.colors.surfaceNormal,
                        contentColor = badgeColor,
                        textStyle = ConsumerMangroTypography.label.labelXS!!,
                        iconResId = R.drawable.ic_alarm_on_16px,
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.timer_card_end_time, formatClock(endTimeMillis)),
                            color = MangroTheme.colors.textBody,
                            style = ConsumerMangroTypography.caption.captionS,
                        )

                        Text(
                            text = formatClock(endTimeMillis),
                            color = MangroTheme.colors.textTitle,
                            style = ConsumerMangroTypography.label.labelM,
                        )
                    }
                }
            }
        }
    }
}

private fun formatRemaining(millis: Long): String {
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

private fun formatClock(millis: Long): String {
    val formatter = SimpleDateFormat("HH:mm", Locale.KOREA)
    return formatter.format(Date(millis))
}

private data class TimerCardPreviewParam(
    val label: String,
    val requestTimeMillis: Long,
    val endTimeMillis: Long,
)

private class TimerCardPreviewParamProvider : PreviewParameterProvider<TimerCardPreviewParam> {
    private val now = System.currentTimeMillis()

    override val values = sequenceOf(
        TimerCardPreviewParam("DEFAULT", now - 5_000, now + 9 * 60_000),
        TimerCardPreviewParam("CAUTION", now - 11 * 60_000, now + 4 * 60_000),
        TimerCardPreviewParam("EXPIRED", now - 20 * 60_000, now - 5 * 60_000),
    )
}

@Preview
@Composable
private fun TimerCardPreview(
    @PreviewParameter(TimerCardPreviewParamProvider::class) param: TimerCardPreviewParam,
) {
    MangroTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(White)
                .padding(20.dp),
            contentAlignment = Alignment.Center,
        ) {
            TimerCard(
                requestTimeMillis = param.requestTimeMillis,
                endTimeMillis = param.endTimeMillis,
            )
        }
    }
}
