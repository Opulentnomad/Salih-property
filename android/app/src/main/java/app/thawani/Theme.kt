package app.thawani

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object Pal {
    val bgL = Color(0xFFF7F7F3); val cardL = Color.White; val inkL = Color(0xFF0F2A22); val ink2L = Color(0xFF5F6B66)
    val lineL = Color(0xFFE6E6DD); val softL = Color(0xFFF3F4EE); val soft2L = Color(0xFFE8F4F0); val accL = Color(0xFF087E68)
    val bgD = Color(0xFF0E1714); val cardD = Color(0xFF14221E); val inkD = Color(0xFFEEF4F0); val ink2D = Color(0xFF9DB0A8)
    val lineD = Color(0xFF243630); val softD = Color(0xFF182823); val soft2D = Color(0xFF10312A); val accD = Color(0xFF4FD1AD)
    val red = Color(0xFFD0392B); val redText = Color(0xFFB13A2E); val redSoft = Color(0xFFFCE8E5)
    val gold = Color(0xFFE3B45C); val caut = Color(0xFFFFF4D9); val cautLine = Color(0xFFEDD08A)
    val cautD = Color(0xFF2B2A1F); val redSoftD = Color(0xFF2A2321)
}

class ThColors(
    val bg: Color, val card: Color, val ink: Color, val ink2: Color, val line: Color, val soft: Color, val soft2: Color,
    val acc: Color, val onAcc: Color, val caut: Color, val cautLine: Color, val redSoft: Color, val redText: Color, val dark: Boolean
)

val LightTh = ThColors(Pal.bgL, Pal.cardL, Pal.inkL, Pal.ink2L, Pal.lineL, Pal.softL, Pal.soft2L, Pal.accL, Color.White, Pal.caut, Pal.cautLine, Pal.redSoft, Pal.redText, false)
val DarkTh = ThColors(Pal.bgD, Pal.cardD, Pal.inkD, Pal.ink2D, Pal.lineD, Pal.softD, Pal.soft2D, Pal.accD, Color(0xFF06241C), Pal.cautD, Color(0x66EBC06A), Pal.redSoftD, Color(0xFFFF8A78), true)

val LocalTh = androidx.compose.runtime.staticCompositionLocalOf { LightTh }

/** لون مستوى الخطورة */
fun lvColor(lv: String, dark: Boolean): Color = when (lv) {
    "c", "d" -> if (dark) Color(0xFFFF8A78) else Color(0xFFC5483A)
    "w" -> if (dark) Color(0xFFEBB45A) else Color(0xFFBA7A20)
    "t" -> if (dark) Color(0xFF8FB0FF) else Color(0xFF527CB3)
    else -> if (dark) Color(0xFF6FD9A8) else Color(0xFF53866A)
}

@Composable
fun ThawaniTheme(mode: String, content: @Composable () -> Unit) {
    val dark = when (mode) { "dark" -> true; "light" -> false; else -> isSystemInDarkTheme() }
    val th = if (dark) DarkTh else LightTh
    val scheme = if (dark) darkColorScheme(primary = th.acc, onPrimary = th.onAcc, background = th.bg, surface = th.card, onSurface = th.ink, onBackground = th.ink)
    else lightColorScheme(primary = th.acc, onPrimary = th.onAcc, background = th.bg, surface = th.card, onSurface = th.ink, onBackground = th.ink)
    androidx.compose.runtime.CompositionLocalProvider(LocalTh provides th) {
        MaterialTheme(colorScheme = scheme, content = content)
    }
}
