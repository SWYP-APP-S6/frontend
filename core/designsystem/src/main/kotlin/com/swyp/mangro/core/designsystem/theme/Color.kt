package com.swyp.mangro.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val Orange50 = Color(0xFFFFF8E0)
val Orange100 = Color(0xFFFFEBB2)
val Orange200 = Color(0xFFFFDE80)
val Orange300 = Color(0xFFFFD24C)
val Orange400 = Color(0xFFFFC723)
val Orange500 = Color(0xFFFFBD00)
val Orange600 = Color(0xFFFFAF00)
val Orange700 = Color(0xFFFF9C00)
val Orange800 = Color(0xFFFF8B00)
val Orange900 = Color(0xFFFF6900)

val Yellow50 = Color(0xFFFFF2C5)
val Yellow500 = Color(0xFFFFE383)

val Lime50 = Color(0xFFE5F7CD)
val Lime500 = Color(0xFFAAE459)

val Pink50 = Color(0xFFFCEEFF)
val Pink500 = Color(0xFFF6C5FF)

val Cyan50 = Color(0xFFD5FBF3)
val Cyan500 = Color(0xFF72F2D7)

val Brown50 = Color(0xFFE1DDD8)
val Brown500 = Color(0xFF6B533E)

val Green50 = Color(0xFFE6F6EA)
val Green100 = Color(0xFFC2E9CC)
val Green200 = Color(0xFF9BDCAC)
val Green300 = Color(0xFF6FCF8B)
val Green400 = Color(0xFF49C471)
val Green500 = Color(0xFF14B857)
val Green600 = Color(0xFF03A94D)

val Red50 = Color(0xFFFFEAED)
val Red100 = Color(0xFFFFCCD0)
val Red200 = Color(0xFFF49896)
val Red300 = Color(0xFFEC6F6D)
val Red400 = Color(0xFFF64C47)
val Red500 = Color(0xFFFA392A)
val Red600 = Color(0xFFEC2D2A)

val Amber50 = Color(0xFFFFF8E1)
val Amber100 = Color(0xFFFFEBB3)
val Amber200 = Color(0xFFFFDF82)
val Amber300 = Color(0xFFFFD350)
val Amber400 = Color(0xFFFFC82A)
val Amber500 = Color(0xFFFFC00E)
val Amber600 = Color(0xFFFFB108)

val Gray50 = Color(0xFFFAFAFA)
val Gray200 = Color(0xFFF5F5F5)
val Gray300 = Color(0xFFEBEBEA)
val Gray400 = Color(0xFFE0E0E0)
val Gray500 = Color(0xFFBDBDBD)
val Gray600 = Color(0xFF808080)
val Gray700 = Color(0xFF616161)
val Gray800 = Color(0xFF333333)
val Gray900 = Color(0xFF212121)

val GrayBlue50 = Color(0xFFF5F6FA)

val White = Color(0xFFFFFFFF)
val Black = Color(0xFF000000)

@Immutable
data class MangroColors(
    val primaryNormal: Color,
    val primaryStrong: Color,
    val primaryLight: Color,

    val secondaryNormal: Color,

    val grainsNormal: Color,
    val grainsBg: Color,

    val vegetablesNormal: Color,
    val vegetablesBg: Color,

    val meatNormal: Color,
    val meatBg: Color,

    val seafoodNormal: Color,
    val seafoodBg: Color,

    val nutsNormal: Color,
    val nutsBg: Color,

    val textTitle: Color,
    val textBody: Color,
    val textSubtitle: Color,
    val textCanceled: Color,
    val textOnBrandWhite: Color,
    val textOnBrandBlack: Color,

    val surfaceNormal: Color,
    val surfaceDisabled: Color,
    val surfaceAlter: Color,

    val grayScale900: Color,
    val grayScale700: Color,
    val grayScale50: Color,

    val borderFocus: Color,
    val borderDefault: Color,
    val borderSubtle: Color,
    val borderPrimarySubtle: Color,

    val positiveNormal: Color,
    val positiveBg: Color,

    val dangerNormal: Color,
    val dangerBg: Color,

    val warningNormal: Color,
    val warningBg: Color,
)

val defaultMangroColors = MangroColors(
    primaryNormal = Orange900,
    primaryStrong = Orange800,
    primaryLight = Orange50,

    secondaryNormal = GrayBlue50,

    grainsNormal = Yellow500,
    grainsBg = Yellow50,

    vegetablesNormal = Lime500,
    vegetablesBg = Lime50,

    meatNormal = Pink500,
    meatBg = Pink50,

    seafoodNormal = Cyan500,
    seafoodBg = Cyan50,

    nutsNormal = Brown500,
    nutsBg = Brown50,

    textTitle = Gray800,
    textBody = Gray700,
    textSubtitle = Gray600,
    textCanceled = Gray500,
    textOnBrandWhite = White,
    textOnBrandBlack = Black,

    surfaceNormal = White,
    surfaceDisabled = Gray300,
    surfaceAlter = Gray200,

    grayScale900 = Gray900,
    grayScale700 = Gray700,
    grayScale50 = Gray50,

    borderFocus = Gray600,
    borderDefault = Gray400,
    borderSubtle = Gray200,
    borderPrimarySubtle = Orange200,

    positiveNormal = Green600,
    positiveBg = Green50,

    dangerNormal = Red600,
    dangerBg = Red50,

    warningNormal = Amber600,
    warningBg = Amber50,
)

val LocalMangroColors = staticCompositionLocalOf { defaultMangroColors }
