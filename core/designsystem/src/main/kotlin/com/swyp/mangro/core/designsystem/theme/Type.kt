package com.swyp.mangro.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.swyp.mangro.core.designsystem.R

object PretendardFont {
    val Regular = FontFamily(Font(R.font.pretendard_regular))
    val Medium = FontFamily(Font(R.font.pretendard_medium))
    val Semibold = FontFamily(Font(R.font.pretendard_semibold))
    val Bold = FontFamily(Font(R.font.pretendard_bold))
}

@Immutable
data class Heading(
    val heading01: TextStyle,
    val heading02: TextStyle,
    val heading03: TextStyle,
)

@Immutable
data class Title(
    val title01: TextStyle,
    val title02: TextStyle,
)

@Immutable
data class Body(
    val body01: TextStyle,
    val body02: TextStyle,
)

@Immutable
data class Caption(
    val caption: TextStyle,
)

@Immutable
data class Label(
    val label01: TextStyle,
    val label02: TextStyle,
    val label03: TextStyle? = null,
)

@Immutable
data class Number(
    val number01: TextStyle,
    val number02: TextStyle,
)

private fun mangroTextStyle(
    fontFamily: FontFamily,
    fontSize: TextUnit,
    lineHeightPercent: Int = 140,
    letterSpacingPercent: Int = -2,
): TextStyle = TextStyle(
    fontFamily = fontFamily,
    fontSize = fontSize,
    lineHeight = (lineHeightPercent / 100f).em,
    letterSpacing = (letterSpacingPercent / 100f).em,
    lineHeightStyle = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.None,
    ),
)

@Immutable
data class MangroTypography(
    val heading: Heading,
    val title: Title,
    val body: Body,
    val caption: Caption,
    val label: Label,
    val number: Number,
)

val MangroHeading = Heading(
    heading01 = mangroTextStyle(
        fontFamily = PretendardFont.Bold,
        fontSize = 28.sp,
        lineHeightPercent = 130,
    ),
    heading02 = mangroTextStyle(
        fontFamily = PretendardFont.Bold,
        fontSize = 24.sp,
        lineHeightPercent = 130,
    ),
    heading03 = mangroTextStyle(
        fontFamily = PretendardFont.Semibold,
        fontSize = 20.sp,
    ),
)

val ConsumerMangroTitle = Title(
    title01 = mangroTextStyle(
        fontFamily = PretendardFont.Semibold,
        fontSize = 18.sp,
    ),
    title02 = mangroTextStyle(
        fontFamily = PretendardFont.Semibold,
        fontSize = 16.sp,
    ),
)

val ConsumerMangroBody = Body(
    body01 = mangroTextStyle(
        fontFamily = PretendardFont.Regular,
        fontSize = 16.sp,
        lineHeightPercent = 150,
        letterSpacingPercent = -3,
    ),
    body02 = mangroTextStyle(
        fontFamily = PretendardFont.Regular,
        fontSize = 14.sp,
        lineHeightPercent = 150,
        letterSpacingPercent = -3,
    ),
)

val ConsumerMangroCaption = Caption(
    caption = mangroTextStyle(
        fontFamily = PretendardFont.Regular,
        fontSize = 12.sp,
    ),
)

val ConsumerMangroLabel = Label(
    label01 = mangroTextStyle(
        fontFamily = PretendardFont.Semibold,
        fontSize = 16.sp,
    ),
    label02 = mangroTextStyle(
        fontFamily = PretendardFont.Medium,
        fontSize = 14.sp,
        lineHeightPercent = 150,
    ),
)

val ConsumerMangroNumber = Number(
    number01 = mangroTextStyle(
        fontFamily = PretendardFont.Semibold,
        fontSize = 20.sp,
        letterSpacingPercent = -1,
    ),
    number02 = mangroTextStyle(
        fontFamily = PretendardFont.Semibold,
        fontSize = 18.sp,
        lineHeightPercent = 150,
        letterSpacingPercent = -1,
    ),
)

val OwnerMangroTitle = Title(
    title01 = mangroTextStyle(
        fontFamily = PretendardFont.Semibold,
        fontSize = 20.sp,
        letterSpacingPercent = -1,
    ),
    title02 = mangroTextStyle(
        fontFamily = PretendardFont.Semibold,
        fontSize = 18.sp,
        lineHeightPercent = 150,
        letterSpacingPercent = -1,
    ),
)

val OwnerMangroBody = Body(
    body01 = mangroTextStyle(
        fontFamily = PretendardFont.Regular,
        fontSize = 18.sp,
        lineHeightPercent = 150,
    ),
    body02 = mangroTextStyle(
        fontFamily = PretendardFont.Regular,
        fontSize = 16.sp,
        lineHeightPercent = 150,
    ),
)

val OwnerMangroCaption = Caption(
    caption = mangroTextStyle(
        fontFamily = PretendardFont.Regular,
        fontSize = 14.sp,
        letterSpacingPercent = -1,
    ),
)

val OwnerMangroLabel = Label(
    label01 = mangroTextStyle(
        fontFamily = PretendardFont.Semibold,
        fontSize = 18.sp,
        letterSpacingPercent = -1,
    ),
    label02 = mangroTextStyle(
        fontFamily = PretendardFont.Medium,
        fontSize = 16.sp,
        lineHeightPercent = 150,
        letterSpacingPercent = -1,
    ),
    label03 = mangroTextStyle(
        fontFamily = PretendardFont.Medium,
        fontSize = 14.sp,
        letterSpacingPercent = -1,
    ),
)

val OwnerMangroNumber = Number(
    number01 = mangroTextStyle(
        fontFamily = PretendardFont.Semibold,
        fontSize = 24.sp,
        letterSpacingPercent = -1,
    ),
    number02 = mangroTextStyle(
        fontFamily = PretendardFont.Semibold,
        fontSize = 20.sp,
        lineHeightPercent = 150,
        letterSpacingPercent = -1,
    ),
)

val ConsumerMangroTypography = MangroTypography(
    heading = MangroHeading,
    title = ConsumerMangroTitle,
    body = ConsumerMangroBody,
    caption = ConsumerMangroCaption,
    label = ConsumerMangroLabel,
    number = ConsumerMangroNumber,
)

val OwnerMangroTypography = MangroTypography(
    heading = MangroHeading,
    title = OwnerMangroTitle,
    body = OwnerMangroBody,
    caption = OwnerMangroCaption,
    label = OwnerMangroLabel,
    number = OwnerMangroNumber,
)
