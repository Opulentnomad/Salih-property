package com.wijha.app.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.wijha.app.data.COUNTRY_NAMES
import com.wijha.app.data.MONTHS
import com.wijha.app.data.Net
import com.wijha.app.data.Phase
import com.wijha.app.data.Stamp
import kotlinx.coroutines.delay
import java.time.LocalDate

/** حالة الرحلة كشريحة ملوّنة. */
@Composable
fun StatusChip(phase: Phase, label: String, modifier: Modifier = Modifier) {
    val (bg, fg) = when (phase) {
        Phase.ONTIME, Phase.BOARDING -> OkBg to OkFg
        Phase.DELAYED -> WarnBg to WarnFg
        Phase.CANCELLED -> BadBg to BadFg
        Phase.LANDED -> InfoBg to InfoFg
    }
    Row(
        modifier.clip(CircleShape).background(bg).padding(horizontal = 10.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Box(Modifier.size(7.dp).clip(CircleShape).background(fg))
        Text(label, color = fg, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun GlassChip(text: String, modifier: Modifier = Modifier, dot: Color? = null) {
    Row(
        modifier.clip(CircleShape).background(Color.White.copy(alpha = 0.92f)).padding(horizontal = 10.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        if (dot != null) Box(Modifier.size(8.dp).clip(CircleShape).background(dot))
        Text(text, color = Ink, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun DateBubble(date: LocalDate, idx: Int = date.dayOfMonth, modifier: Modifier = Modifier) {
    val (bg, fg) = Bubbles[idx % Bubbles.size]
    Column(
        modifier.size(46.dp).clip(CircleShape).background(bg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(MONTHS[date.monthValue - 1], color = fg, fontSize = 9.sp, lineHeight = 10.sp)
        Text(date.dayOfMonth.toString(), color = fg, fontWeight = FontWeight.Bold, fontSize = 17.sp, lineHeight = 18.sp)
    }
}

/** رمز المطار على شكل لوحة قلب، مع حركة تظهر مرة واحدة. */
@Composable
fun CodeTiles(code: String, fontSize: Int, animate: Boolean, tile: Color = Ink, textColor: Color = Color.White) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            code.forEachIndexed { i, ch ->
                val rot = remember { Animatable(if (animate) -100f else 0f) }
                LaunchedEffect(Unit) {
                    if (animate) {
                        delay(i * 70L)
                        rot.animateTo(0f, tween(650))
                    }
                }
                Box(
                    Modifier
                        .graphicsLayer { rotationX = rot.value; cameraDistance = 14f * density; alpha = if (rot.value < -90f) 0f else 1f }
                        .clip(RoundedCornerShape(7.dp))
                        .background(tile)
                        .padding(horizontal = 6.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(ch.toString(), style = CodeStyle, color = textColor, fontSize = fontSize.sp)
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, trailing: String? = null, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        if (trailing != null) Text(trailing, style = MaterialTheme.typography.bodySmall, color = Mute)
    }
}

@Composable
fun EmptyState(title: String, text: String, action: String? = null, onAction: () -> Unit = {}) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Color.White).padding(22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(text, style = MaterialTheme.typography.bodyMedium, color = Mute, textAlign = TextAlign.Center)
        if (action != null) {
            Spacer(Modifier.height(6.dp))
            Button(onClick = onAction, colors = ButtonDefaults.buttonColors(containerColor = Ink)) { Text(action) }
        }
    }
}

/** صورة وجهة من الإنترنت فوق تدرج لوني يظهر أثناء التحميل أو دون اتصال. */
@Composable
fun WikiImage(title: String, modifier: Modifier = Modifier, colors: List<Color> = listOf(Color(0xFF6F9BF0), Color(0xFFF6C4D8))) {
    val url by produceState<String?>(null, title) { value = Net.hero(title) }
    Box(modifier.background(Brush.verticalGradient(colors))) {
        url?.let {
            AsyncImage(model = it, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        }
    }
}

val DestGradients = mapOf(
    "mv" to listOf(Color(0xFF7FE4E2), Color(0xFF1B9AC4)),
    "gr" to listOf(Color(0xFF6F9BF0), Color(0xFFF6C4D8)),
    "tr" to listOf(Color(0xFF8FA8E6), Color(0xFFFFB98A)),
    "ae" to listOf(Color(0xFF9FC8F0), Color(0xFFC9B48A)),
    "sa" to listOf(Color(0xFFF0B27A), Color(0xFFF8E0C0)),
    "eg" to listOf(Color(0xFFF6D28C), Color(0xFFC98A3E)),
)

fun gradientFor(cc: String): List<Color> = DestGradients[cc] ?: listOf(Color(0xFF6F9BF0), Color(0xFFF6C4D8))

/** ختم جواز مرسوم بالكانفاس: دائري أو مستطيل أو بيضاوي، بحبر ملوّن. */
@Composable
fun StampView(s: Stamp, width: Dp, modifier: Modifier = Modifier) {
    val tm = rememberTextMeasurer()
    val ink = StampInks[s.ink % StampInks.size]
    val country = COUNTRY_NAMES[s.cc] ?: s.cc
    val date = "${s.date.dayOfMonth.toString().padStart(2, '0')}.${s.date.monthValue.toString().padStart(2, '0')}.${s.date.year}"
    val ratio = when (s.shape) { 0 -> 1f; 1 -> 0.64f; else -> 0.65f }
    Canvas(modifier.width(width).height(width * ratio)) {
        val w = size.width
        val h = size.height
        fun centered(text: String, cy: Float, sizeSp: Float, bold: Boolean = true) {
            val r = tm.measure(text, TextStyle(color = ink, fontSize = sizeSp.sp, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal))
            drawText(r, topLeft = Offset(w / 2f - r.size.width / 2f, cy - r.size.height / 2f))
        }
        rotate(s.rot, pivot = Offset(w / 2f, h / 2f)) {
            val sw = w * 0.026f
            when (s.shape) {
                0 -> {
                    drawCircle(ink, radius = w / 2f - sw, style = Stroke(sw))
                    drawCircle(ink, radius = w * 0.34f, style = Stroke(sw * 0.4f))
                    centered(country, h * 0.27f, w * 0.1f / density * 1f)
                    centered(date.take(5), h * 0.47f, w * 0.1f / density)
                    centered(date.takeLast(4), h * 0.58f, w * 0.08f / density, false)
                    centered(s.city, h * 0.78f, w * 0.085f / density)
                }
                1 -> {
                    drawRoundRect(ink, Offset(sw, sw), Size(w - 2 * sw, h - 2 * sw), androidx.compose.ui.geometry.CornerRadius(w * 0.06f), style = Stroke(sw))
                    drawRoundRect(ink, Offset(w * 0.075f, h * 0.11f), Size(w * 0.85f, h * 0.78f), androidx.compose.ui.geometry.CornerRadius(w * 0.03f), style = Stroke(sw * 0.4f))
                    centered(country, h * 0.22f, w * 0.105f / density)
                    centered(s.city, h * 0.5f, w * 0.1f / density)
                    centered(date, h * 0.79f, w * 0.085f / density, false)
                }
                else -> {
                    drawOval(ink, Offset(sw, sw), Size(w - 2 * sw, h - 2 * sw), style = Stroke(sw))
                    drawOval(ink, Offset(w * 0.07f, h * 0.12f), Size(w * 0.86f, h * 0.76f), style = Stroke(sw * 0.4f))
                    centered(country, h * 0.34f, w * 0.095f / density)
                    centered(s.city, h * 0.53f, w * 0.09f / density)
                    centered(date, h * 0.72f, w * 0.075f / density, false)
                }
            }
        }
    }
}

@Composable
fun GhostStamp(width: Dp, label: String, modifier: Modifier = Modifier) {
    Box(
        modifier.width(width).height(width * 0.64f).clip(RoundedCornerShape(10.dp)).background(Soft.copy(alpha = 0.6f)),
        contentAlignment = Alignment.Center,
    ) { Text(label, color = Mute, style = MaterialTheme.typography.labelMedium) }
}

@Composable
fun CardColumn(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Color.White)) { content() }
}
