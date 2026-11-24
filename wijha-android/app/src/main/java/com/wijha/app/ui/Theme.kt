package com.wijha.app.ui

import android.graphics.Typeface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp

// لوحة «إيجه»: أزرق جليدي وكحلي وكوبالت
val Bg = Color(0xFFF3F7FC)
val Ink = Color(0xFF1C2740)
val Acc = Color(0xFF2F5FD0)
val Soft = Color(0xFFE1EAFB)
val Mute = Color(0xFF5F6C85)
val Line = Color(0xFFD9E2F1)
val OkBg = Color(0xFFD3EBDB); val OkFg = Color(0xFF17663F)
val WarnBg = Color(0xFFF8E6B8); val WarnFg = Color(0xFF8A5200)
val BadBg = Color(0xFFF6D6D1); val BadFg = Color(0xFFA1281F)
val InfoBg = Color(0xFFDFE7FB); val InfoFg = Color(0xFF27408B)
val Heart = Color(0xFFE5304A)

/** فقاعات التواريخ الباستيل: الخلفية والنص. */
val Bubbles = listOf(
    Color(0xFFDFE8FF) to Color(0xFF2B3F8F),
    Color(0xFFD4EFE9) to Color(0xFF1F5F58),
    Color(0xFFEADFFF) to Color(0xFF5B3A8C),
)

/** ألوان الأختام الأربعة. */
val StampInks = listOf(Color(0xFF2F5FD0), Color(0xFFD64D7B), Color(0xFF7A4FD0), Color(0xFF1F8F76))

/** خط ضيّق وعريض لرموز المطارات، من خطوط النظام. */
val CodeFont = FontFamily(Typeface.create("sans-serif-condensed", Typeface.BOLD))

private val AppTypography = Typography().let { t ->
    t.copy(
        headlineMedium = t.headlineMedium.copy(fontWeight = FontWeight.Bold, fontSize = 26.sp, lineHeight = 34.sp),
        headlineSmall = t.headlineSmall.copy(fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 30.sp),
        titleLarge = t.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 20.sp),
        titleMedium = t.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp),
        bodyLarge = t.bodyLarge.copy(fontSize = 16.sp, lineHeight = 26.sp),
        bodyMedium = t.bodyMedium.copy(fontSize = 14.sp, lineHeight = 22.sp),
        bodySmall = t.bodySmall.copy(fontSize = 12.sp, lineHeight = 18.sp),
        labelLarge = t.labelLarge.copy(fontWeight = FontWeight.Bold),
    )
}

@Composable
fun WijhaTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = lightColorScheme(
                primary = Ink, onPrimary = Color.White, primaryContainer = Soft, onPrimaryContainer = Ink,
                secondary = Acc, onSecondary = Color.White, secondaryContainer = Soft, onSecondaryContainer = Ink,
                background = Bg, onBackground = Ink, surface = Color.White, onSurface = Ink,
                surfaceVariant = Bg, onSurfaceVariant = Mute, outline = Line, outlineVariant = Line, error = BadFg,
                surfaceContainer = Color.White, surfaceContainerHigh = Color.White, surfaceContainerLow = Color.White,
            ),
            typography = AppTypography,
            content = content,
        )
    }
}

val CodeStyle = TextStyle(fontFamily = CodeFont, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
