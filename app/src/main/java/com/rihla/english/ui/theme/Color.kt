package com.rihla.english.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val Green = Color(0xFF58CC02)
val GreenDark = Color(0xFF46A302)
val GreenLight = Color(0xFFD7FFB8)
val Blue = Color(0xFF1CB0F6)
val BlueDark = Color(0xFF1899D6)
val BlueLight = Color(0xFFDDF4FF)
val Red = Color(0xFFFF4B4B)
val RedDark = Color(0xFFEA2B2B)
val RedLight = Color(0xFFFFDFE0)
val Orange = Color(0xFFFF9600)
val OrangeDark = Color(0xFFE08600)
val Gold = Color(0xFFFFC800)
val GoldDark = Color(0xFFE5B400)
val Purple = Color(0xFFCE82FF)
val PurpleDark = Color(0xFFA568CC)
val Gray = Color(0xFFE5E5E5)
val GrayDark = Color(0xFFAFAFAF)
val Ink = Color(0xFF3C3C3C)
val InkSoft = Color(0xFF777777)

/** Extra semantic colours that Material's scheme doesn't cover. */
@Immutable
data class RihlaColors(
    val border: Color,
    val cardBackground: Color,
    val locked: Color,
    val lockedShadow: Color,
    val correctBackground: Color,
    val correctText: Color,
    val wrongBackground: Color,
    val wrongText: Color,
    val selectedBackground: Color,
    val selectedBorder: Color,
    val textPrimary: Color,
    val textSecondary: Color,
)

val LightRihlaColors = RihlaColors(
    border = Gray,
    cardBackground = Color.White,
    locked = Gray,
    lockedShadow = GrayDark,
    correctBackground = GreenLight,
    correctText = GreenDark,
    wrongBackground = RedLight,
    wrongText = RedDark,
    selectedBackground = BlueLight,
    selectedBorder = Blue,
    textPrimary = Ink,
    textSecondary = InkSoft,
)

val DarkRihlaColors = RihlaColors(
    border = Color(0xFF37464F),
    cardBackground = Color(0xFF1F2F35),
    locked = Color(0xFF37464F),
    lockedShadow = Color(0xFF253238),
    correctBackground = Color(0xFF203A1A),
    correctText = Color(0xFF79D634),
    wrongBackground = Color(0xFF3F1F22),
    wrongText = Color(0xFFFF7878),
    selectedBackground = Color(0xFF1A3A4F),
    selectedBorder = Blue,
    textPrimary = Color(0xFFF1F7FB),
    textSecondary = Color(0xFF9EB1BC),
)

val LocalRihlaColors = staticCompositionLocalOf { LightRihlaColors }
