package com.rihla.english.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/*
 * "رحلة" visual identity: a desert-journey palette — oasis teal, saffron gold, sunset coral and
 * a night-sky navy over warm sand. Deliberately far from the usual green-owl look.
 */
val Oasis = Color(0xFF12B5A5)
val OasisDark = Color(0xFF0B8C80)
val OasisLight = Color(0xFFD3F5F0)
val Saffron = Color(0xFFFFB020)
val SaffronDark = Color(0xFFDB9300)
val Coral = Color(0xFFFF5F57)
val CoralDark = Color(0xFFDB423B)
val CoralLight = Color(0xFFFFE1DE)
val Sky = Color(0xFF4F8DF7)
val SkyDark = Color(0xFF2F6BD6)
val SkyLight = Color(0xFFDDE9FF)
val Plum = Color(0xFF8B5CF6)
val PlumDark = Color(0xFF6D3FD8)
val Night = Color(0xFF16213E)
val NightSoft = Color(0xFF253461)
val Sand = Color(0xFFFFF8EC)
val SandDeep = Color(0xFFF1E2C4)
val Ink = Color(0xFF2B2D42)
val InkSoft = Color(0xFF6B6F80)
val Ember = Color(0xFFFF8A1F)

// Semantic aliases used across the UI.
val Green = Oasis
val GreenDark = OasisDark
val GreenLight = OasisLight
val Blue = Sky
val BlueDark = SkyDark
val BlueLight = SkyLight
val Red = Coral
val RedDark = CoralDark
val RedLight = CoralLight
val Orange = Ember
val OrangeDark = Color(0xFFDB6E0B)
val Gold = Saffron
val GoldDark = SaffronDark
val Purple = Plum
val PurpleDark = PlumDark
val Gray = Color(0xFFE6E1D6)
val GrayDark = Color(0xFFB9B2A3)

/** Night-sky gradient used by hero cards. */
val NightSkyBrush = Brush.verticalGradient(listOf(Night, NightSoft, Color(0xFF3B3F7A)))

/** Sunset gradient for celebrations. */
val SunsetBrush = Brush.linearGradient(listOf(Coral, Saffron))

fun gradientFor(color: Color): Brush = Brush.linearGradient(listOf(color, color.copy(alpha = 0.75f).compositeOver(Night)))

private fun Color.compositeOver(other: Color): Color {
    val a = alpha
    return Color(red * a + other.red * (1 - a), green * a + other.green * (1 - a), blue * a + other.blue * (1 - a), 1f)
}

/** Extra semantic colours that Material's scheme doesn't cover. */
@Immutable
data class RihlaColors(
    val border: Color,
    val cardBackground: Color,
    val screenBackground: Color,
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
    border = SandDeep,
    cardBackground = Color.White,
    screenBackground = Sand,
    locked = Gray,
    lockedShadow = GrayDark,
    correctBackground = OasisLight,
    correctText = OasisDark,
    wrongBackground = CoralLight,
    wrongText = CoralDark,
    selectedBackground = SkyLight,
    selectedBorder = Sky,
    textPrimary = Ink,
    textSecondary = InkSoft,
)

val DarkRihlaColors = RihlaColors(
    border = Color(0xFF2E3B5E),
    cardBackground = Color(0xFF1B2747),
    screenBackground = Color(0xFF0F1830),
    locked = Color(0xFF2E3B5E),
    lockedShadow = Color(0xFF1F2A47),
    correctBackground = Color(0xFF113F3B),
    correctText = Color(0xFF4FE0CF),
    wrongBackground = Color(0xFF4A1F25),
    wrongText = Color(0xFFFF8A84),
    selectedBackground = Color(0xFF1D3567),
    selectedBorder = Sky,
    textPrimary = Color(0xFFF4F1EA),
    textSecondary = Color(0xFFA9B1C9),
)

val LocalRihlaColors = staticCompositionLocalOf { LightRihlaColors }
