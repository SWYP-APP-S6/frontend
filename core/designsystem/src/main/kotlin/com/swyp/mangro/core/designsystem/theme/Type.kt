package com.swyp.mangro.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
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
    val headingL: TextStyle,
    val headingM: TextStyle,
    val headingS: TextStyle,
    val headingXXS: TextStyle,
)

@Immutable
data class Title(
    val titleL: TextStyle,
    val titleM: TextStyle,
)

@Immutable
data class Body(
    val bodyL: TextStyle,
    val bodyM: TextStyle,
    val body03: TextStyle,
)

@Immutable
data class Caption(
    val captionL: TextStyle? = null,
    val captionM: TextStyle? = null,
    val captionS: TextStyle,
)

@Immutable
data class Label(
    val labelL: TextStyle,
    val labelM: TextStyle,
    val labelS: TextStyle? = null,
)

@Immutable
data class Number(
    val numberL: TextStyle,
    val numberM: TextStyle,
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
    headingL = mangroTextStyle(
        fontFamily = PretendardFont.Bold,
        fontSize = 28.sp,
        lineHeightPercent = 130,
    ),
    headingM = mangroTextStyle(
        fontFamily = PretendardFont.Bold,
        fontSize = 24.sp,
        lineHeightPercent = 130,
    ),
    headingS = mangroTextStyle(
        fontFamily = PretendardFont.Semibold,
        fontSize = 20.sp,
    ),
    headingXXS = mangroTextStyle(
        fontFamily = PretendardFont.Bold,
        fontSize = 18.sp,
        lineHeightPercent = 150,
        letterSpacingPercent = -1,
    ),
)

val ConsumerMangroTitle = Title(
    titleL = mangroTextStyle(
        fontFamily = PretendardFont.Semibold,
        fontSize = 18.sp,
    ),
    titleM = mangroTextStyle(
        fontFamily = PretendardFont.Semibold,
        fontSize = 16.sp,
    ),
)

val ConsumerMangroBody = Body(
    bodyL = mangroTextStyle(
        fontFamily = PretendardFont.Regular,
        fontSize = 16.sp,
        lineHeightPercent = 150,
        letterSpacingPercent = -3,
    ),
    bodyM = mangroTextStyle(
        fontFamily = PretendardFont.Regular,
        fontSize = 14.sp,
        lineHeightPercent = 150,
        letterSpacingPercent = -3,
    ),
    body03 = mangroTextStyle(
        fontFamily = PretendardFont.Medium,
        fontSize = 12.sp,
        lineHeightPercent = 150,
        letterSpacingPercent = -3,
    ),
)

val ConsumerMangroCaption = Caption(
    captionS = mangroTextStyle(
        fontFamily = PretendardFont.Regular,
        fontSize = 12.sp,
    ),
)

val ConsumerMangroLabel = Label(
    labelL = mangroTextStyle(
        fontFamily = PretendardFont.Semibold,
        fontSize = 16.sp,
    ),
    labelM = mangroTextStyle(
        fontFamily = PretendardFont.Medium,
        fontSize = 14.sp,
        lineHeightPercent = 150,
    ),
)

val ConsumerMangroNumber = Number(
    numberL = mangroTextStyle(
        fontFamily = PretendardFont.Semibold,
        fontSize = 20.sp,
        letterSpacingPercent = -1,
    ),
    numberM = mangroTextStyle(
        fontFamily = PretendardFont.Semibold,
        fontSize = 18.sp,
        lineHeightPercent = 150,
        letterSpacingPercent = -1,
    ),
)

val OwnerMangroTitle = Title(
    titleL = mangroTextStyle(
        fontFamily = PretendardFont.Semibold,
        fontSize = 20.sp,
        letterSpacingPercent = -1,
    ),
    titleM = mangroTextStyle(
        fontFamily = PretendardFont.Semibold,
        fontSize = 18.sp,
        lineHeightPercent = 150,
        letterSpacingPercent = -1,
    ),
)

val OwnerMangroBody = Body(
    bodyL = mangroTextStyle(
        fontFamily = PretendardFont.Regular,
        fontSize = 18.sp,
        lineHeightPercent = 150,
    ),
    bodyM = mangroTextStyle(
        fontFamily = PretendardFont.Regular,
        fontSize = 16.sp,
        lineHeightPercent = 150,
    ),
    body03 = mangroTextStyle(
        fontFamily = PretendardFont.Medium,
        fontSize = 14.sp,
        lineHeightPercent = 150,
        letterSpacingPercent = -3,
    ),
)

val OwnerMangroCaption = Caption(
    captionL = mangroTextStyle(
        fontFamily = PretendardFont.Bold,
        fontSize = 14.sp,
        letterSpacingPercent = -1,
    ),
    captionM = mangroTextStyle(
        fontFamily = PretendardFont.Medium,
        fontSize = 14.sp,
        letterSpacingPercent = -1,
    ),
    captionS = mangroTextStyle(
        fontFamily = PretendardFont.Regular,
        fontSize = 14.sp,
        letterSpacingPercent = -1,
    ),
)

val OwnerMangroLabel = Label(
    labelL = mangroTextStyle(
        fontFamily = PretendardFont.Semibold,
        fontSize = 18.sp,
        letterSpacingPercent = -1,
    ),
    labelM = mangroTextStyle(
        fontFamily = PretendardFont.Medium,
        fontSize = 16.sp,
        lineHeightPercent = 150,
        letterSpacingPercent = -1,
    ),
    labelS = mangroTextStyle(
        fontFamily = PretendardFont.Medium,
        fontSize = 14.sp,
        letterSpacingPercent = -1,
    ),
)

val OwnerMangroNumber = Number(
    numberL = mangroTextStyle(
        fontFamily = PretendardFont.Semibold,
        fontSize = 24.sp,
        letterSpacingPercent = -1,
    ),
    numberM = mangroTextStyle(
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

val LocalMangroTypography = staticCompositionLocalOf { ConsumerMangroTypography }
