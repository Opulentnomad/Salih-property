package com.rihla.english.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.rihla.english.R

/** Reem Kufi — cinematic Arabic display face for titles (SIL OFL). */
val KufiFont = FontFamily(
    Font(R.font.reem_kufi_500, FontWeight.Medium),
    Font(R.font.reem_kufi_500, FontWeight.SemiBold),
    Font(R.font.reem_kufi_700, FontWeight.Bold),
)

/** IBM Plex Sans Arabic — calm, legible Arabic + Latin body face (SIL OFL). */
val PlexFont = FontFamily(
    Font(R.font.plex_arabic_400, FontWeight.Normal),
    Font(R.font.plex_arabic_400, FontWeight.Medium),
    Font(R.font.plex_arabic_600, FontWeight.SemiBold),
    Font(R.font.plex_arabic_700, FontWeight.Bold),
)

/** Inter — film-subtitle face for English (SIL OFL). */
val InterFont = FontFamily(
    Font(R.font.inter_500, FontWeight.Normal),
    Font(R.font.inter_500, FontWeight.Medium),
    Font(R.font.inter_700, FontWeight.SemiBold),
    Font(R.font.inter_700, FontWeight.Bold),
    Font(R.font.inter_800, FontWeight.ExtraBold),
)

private val body = TextStyle(fontFamily = PlexFont, color = Cine.Text)
private val display = TextStyle(fontFamily = KufiFont, color = Cine.Text)

val CineTypography = Typography(
    displayMedium = display.copy(fontSize = 34.sp, fontWeight = FontWeight.Bold, lineHeight = 44.sp),
    headlineLarge = display.copy(fontSize = 30.sp, fontWeight = FontWeight.Bold, lineHeight = 38.sp),
    headlineMedium = display.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold, lineHeight = 32.sp),
    titleLarge = display.copy(fontSize = 20.sp, fontWeight = FontWeight.SemiBold, lineHeight = 28.sp),
    titleMedium = body.copy(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, lineHeight = 24.sp),
    bodyLarge = body.copy(fontSize = 16.sp, lineHeight = 26.sp),
    bodyMedium = body.copy(fontSize = 14.sp, lineHeight = 22.sp),
    labelLarge = body.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold, lineHeight = 20.sp),
    labelMedium = body.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold, lineHeight = 16.sp),
)

/** English learning text in the subtitle face (always rendered LTR by EnglishText). */
object EnglishTextStyles {
    val subtitle = TextStyle(fontFamily = InterFont, fontSize = 24.sp, fontWeight = FontWeight.Bold, lineHeight = 32.sp)
    val hero = TextStyle(fontFamily = InterFont, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 36.sp)
    val option = TextStyle(fontFamily = InterFont, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, lineHeight = 22.sp)
    val small = TextStyle(fontFamily = InterFont, fontSize = 13.sp, fontWeight = FontWeight.Medium, lineHeight = 18.sp)
    val stamp = TextStyle(fontFamily = InterFont, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.5.sp)
}

private val CineScheme = darkColorScheme(
    primary = Cine.Amber,
    onPrimary = Cine.Ink,
    secondary = Cine.Blue,
    onSecondary = Cine.Ink,
    background = Cine.Ink,
    onBackground = Cine.Text,
    surface = Cine.Panel,
    onSurface = Cine.Text,
    surfaceVariant = Cine.Panel,
    onSurfaceVariant = Cine.Muted,
    surfaceContainerHigh = Cine.Panel,
    outline = Cine.Line,
)

/** The app is always "in the cinema": one dark theme by design. */
@Composable
fun RihlaTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = CineScheme, typography = CineTypography, content = content)
}
