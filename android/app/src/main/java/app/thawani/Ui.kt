package app.thawani

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ThCard(modifier: Modifier = Modifier, color: Color = LocalTh.current.card, radius: Dp = 18.dp, content: @Composable () -> Unit) {
    val th = LocalTh.current
    Surface(modifier = modifier, shape = RoundedCornerShape(radius), color = color, border = BorderStroke(1.dp, th.line)) { content() }
}

@Composable
fun Btn(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, kind: String = "solid", enabled: Boolean = true) {
    val th = LocalTh.current
    val (bg, fg, border) = when (kind) {
        "red" -> Triple(Pal.red, Color.White, null)
        "ghost" -> Triple(th.card, th.ink, BorderStroke(1.dp, th.line))
        else -> Triple(th.acc, th.onAcc, null)
    }
    Button(
        onClick = onClick, enabled = enabled, modifier = modifier.heightIn(min = 48.dp), shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = bg, contentColor = fg, disabledContainerColor = th.soft, disabledContentColor = th.ink2),
        border = border, contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) { Text(text, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, textAlign = TextAlign.Center) }
}

@Composable
fun Chip(text: String, selected: Boolean, onClick: () -> Unit) {
    val th = LocalTh.current
    Box(
        Modifier.heightIn(min = 44.dp).clip(RoundedCornerShape(99.dp))
            .background(if (selected) th.acc else th.card)
            .border(1.dp, if (selected) th.acc else th.line, RoundedCornerShape(99.dp))
            .clickable(role = Role.Button, onClick = onClick).padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) { Text(text, color = if (selected) th.onAcc else th.ink, fontWeight = FontWeight.SemiBold, fontSize = 15.sp) }
}

fun kindEmoji(c: Card): String {
    val t = c.title
    return when {
        Regex("غاز").containsMatchIn(t) -> "🛢"
        Regex("صعق|كهرب").containsMatchIn(t) -> "⚡"
        Regex("مصعد").containsMatchIn(t) -> "🛗"
        Regex("حريق|اشتعال|حروق|حرائق").containsMatchIn(t) -> "🔥"
        Regex("غرق|سيول|مياه").containsMatchIn(t) -> "🌊"
        Regex("شمس|حرار|جفاف|الحر").containsMatchIn(t) -> "☀"
        Regex("لدغ|لسعة|عضة").containsMatchIn(t) -> "⚠"
        Regex("عين").containsMatchIn(t) -> "👁"
        Regex("كسور|عظام|عمود").containsMatchIn(t) -> "🦴"
        Regex("سكر|دواء|أدوية").containsMatchIn(t) && !t.contains("نوبة قلبية") -> "💊"
        Regex("ربو|غبار|غصة|اختناق|الفحم|دوار").containsMatchIn(t) -> "💨"
        Regex("نزيف|جروح|جرح|بتر|رعاف").containsMatchIn(t) -> "🩸"
        Regex("إنعاش|قلب|جلطة|إغماء|حساسية").containsMatchIn(t) -> "❤"
        Regex("طفل|رضيع|أطفال|كبار|سقوط").containsMatchIn(t) -> "🧒"
        Regex("مركبة|إطار|حوادث|المفتاح|بطارية|الخط السريع|المرور|التغريز|الصحراء|سفر").containsMatchIn(t) -> "🚗"
        Regex("إخلاء|منزل|التسمم الغذائي").containsMatchIn(t) -> "🏠"
        else -> "✚"
    }
}

@Composable
fun CardIcon(c: Card, size: Dp = 46.dp) {
    val th = LocalTh.current
    val col = lvColor(c.lv, th.dark)
    Box(Modifier.size(size).clip(RoundedCornerShape(14.dp)).background(col.copy(alpha = .16f)), contentAlignment = Alignment.Center) {
        Text(kindEmoji(c), fontSize = (size.value * .5f).sp)
    }
}

@Composable
fun Illus(cardId: String, step: Int, modifier: Modifier = Modifier) {
    val svg = Data.illus(cardId, step) ?: return
    val bmp = remember(cardId, step) { svgToBitmap(svg, 660, 450) } ?: return
    Image(
        bitmap = bmp, contentDescription = null, contentScale = ContentScale.Fit,
        modifier = modifier.fillMaxWidth().aspectRatio(220f / 150f).clip(RoundedCornerShape(14.dp))
    )
}

@Composable
fun SectionTitle(text: String, sub: String? = null) {
    val th = LocalTh.current
    Column(Modifier.padding(top = 22.dp, bottom = 10.dp)) {
        if (sub != null) Text(sub, color = th.ink2, fontSize = 13.sp)
        Text(text, color = th.ink, fontSize = 24.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun Banner(text: String, modifier: Modifier = Modifier) {
    val th = LocalTh.current
    Box(modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(th.caut).border(1.dp, th.cautLine, RoundedCornerShape(14.dp)).padding(14.dp)) {
        Text(text, color = th.ink, fontSize = 15.sp, lineHeight = 22.sp)
    }
}

@Composable
fun RowSpace(content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit) =
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically, content = content)

@Composable
fun DotCircle(color: Color, size: Dp = 10.dp) = Box(Modifier.size(size).clip(CircleShape).background(color))
