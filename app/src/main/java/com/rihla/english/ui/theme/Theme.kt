package com.rihla.english.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rihla.english.R

private val LightScheme = lightColorScheme(
    primary = Green,
    onPrimary = Color.White,
    secondary = Blue,
    onSecondary = Color.White,
    tertiary = Orange,
    error = Red,
    background = Sand,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceContainerLow = Color.White,
    surfaceVariant = Color(0xFFFFF1DA),
    onSurfaceVariant = InkSoft,
    outline = SandDeep,
)

private val DarkScheme = darkColorScheme(
    primary = Green,
    onPrimary = Color.White,
    secondary = Blue,
    onSecondary = Color.White,
    tertiary = Orange,
    error = Red,
    background = Color(0xFF0F1830),
    onBackground = Color(0xFFF4F1EA),
    surface = Color(0xFF1B2747),
    onSurface = Color(0xFFF4F1EA),
    surfaceVariant = Color(0xFF1B2747),
    onSurfaceVariant = Color(0xFFA9B1C9),
    outline = Color(0xFF2E3B5E),
)

/** Rounded, friendly display face with full Arabic + Latin coverage (SIL OFL). */
val RihlaFont = FontFamily(
    Font(R.font.baloo_bhaijaan_500, FontWeight.Normal),
    Font(R.font.baloo_bhaijaan_500, FontWeight.Medium),
    Font(R.font.baloo_bhaijaan_700, FontWeight.SemiBold),
    Font(R.font.baloo_bhaijaan_700, FontWeight.Bold),
    Font(R.font.baloo_bhaijaan_800, FontWeight.ExtraBold),
)

/** Highly legible rounded Latin face for English learning content (SIL OFL). */
val EnglishFont = FontFamily(
    Font(R.font.nunito_500, FontWeight.Normal),
    Font(R.font.nunito_500, FontWeight.Medium),
    Font(R.font.nunito_700, FontWeight.SemiBold),
    Font(R.font.nunito_700, FontWeight.Bold),
    Font(R.font.nunito_800, FontWeight.ExtraBold),
)

private val base = TextStyle(fontFamily = RihlaFont)
private val english = TextStyle(fontFamily = EnglishFont)

val RihlaTypography = Typography(
    displayLarge = base.copy(fontSize = 44.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 52.sp),
    displayMedium = base.copy(fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 44.sp),
    headlineLarge = base.copy(fontSize = 28.sp, fontWeight = FontWeight.Bold, lineHeight = 36.sp),
    headlineMedium = base.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold, lineHeight = 32.sp),
    titleLarge = base.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold, lineHeight = 28.sp),
    titleMedium = base.copy(fontSize = 17.sp, fontWeight = FontWeight.Bold, lineHeight = 24.sp),
    bodyLarge = base.copy(fontSize = 17.sp, fontWeight = FontWeight.Normal, lineHeight = 26.sp),
    bodyMedium = base.copy(fontSize = 15.sp, fontWeight = FontWeight.Normal, lineHeight = 22.sp),
    labelLarge = base.copy(fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 20.sp),
    labelMedium = base.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold, lineHeight = 16.sp),
)

/** Large, high-contrast style for English learning content (always LTR). */
object EnglishTextStyles {
    val hero = english.copy(fontSize = 40.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 48.sp, letterSpacing = 0.5.sp)
    val large = english.copy(fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 36.sp)
    val medium = english.copy(fontSize = 22.sp, fontWeight = FontWeight.Bold, lineHeight = 30.sp)
    val chip = english.copy(fontSize = 19.sp, fontWeight = FontWeight.Bold, lineHeight = 24.sp)
}

private val RihlaShapes = Shapes(
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
)

@Composable
fun RihlaTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalRihlaColors provides if (darkTheme) DarkRihlaColors else LightRihlaColors) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkScheme else LightScheme,
            typography = RihlaTypography,
            shapes = RihlaShapes,
            content = content,
        )
    }
}

object RihlaTheme {
    val colors: RihlaColors
        @Composable get() = LocalRihlaColors.current
}
