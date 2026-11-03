package com.rihla.english.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rihla.english.ui.theme.NightSkyBrush
import com.rihla.english.ui.theme.RihlaTheme
import com.rihla.english.ui.theme.SandDeep
import com.rihla.english.ui.theme.Saffron
import kotlin.random.Random

/** White rounded card with a soft sand border — the base surface of the redesign. */
@Composable
fun RihlaCard(
    modifier: Modifier = Modifier,
    background: Color = RihlaTheme.colors.cardBackground,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(background)
            .border(2.dp, RihlaTheme.colors.border, RoundedCornerShape(24.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = content,
    )
}

/** Night-sky card with twinkling stars and dunes along the bottom — used for hero areas. */
@Composable
fun NightSkyCard(
    modifier: Modifier = Modifier,
    brush: Brush = NightSkyBrush,
    content: @Composable BoxScope.() -> Unit,
) {
    val stars = remember {
        val r = Random(7)
        List(26) { Triple(r.nextFloat(), r.nextFloat() * 0.6f, 1.2f + r.nextFloat() * 2.4f) }
    }
    Box(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(brush),
    ) {
        Canvas(Modifier.matchParentSize()) {
            stars.forEach { (x, y, radius) ->
                drawCircle(Color.White.copy(alpha = 0.55f), radius = radius, center = Offset(x * size.width, y * size.height))
            }
            // Two dune layers.
            val back = Path().apply {
                moveTo(0f, size.height * 0.82f)
                cubicTo(size.width * 0.25f, size.height * 0.68f, size.width * 0.55f, size.height * 0.95f, size.width, size.height * 0.75f)
                lineTo(size.width, size.height); lineTo(0f, size.height); close()
            }
            drawPath(back, SandDeep.copy(alpha = 0.18f))
            val front = Path().apply {
                moveTo(0f, size.height * 0.92f)
                cubicTo(size.width * 0.35f, size.height * 0.80f, size.width * 0.7f, size.height * 1.02f, size.width, size.height * 0.88f)
                lineTo(size.width, size.height); lineTo(0f, size.height); close()
            }
            drawPath(front, Saffron.copy(alpha = 0.22f))
        }
        content()
    }
}

/** Circular progress ring (e.g. daily goal) with content in the middle. */
@Composable
fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 88.dp,
    stroke: Dp = 10.dp,
    color: Color = Saffron,
    track: Color = Color.White.copy(alpha = 0.18f),
    content: @Composable BoxScope.() -> Unit = {},
) {
    val animated by animateFloatAsState(progress.coerceIn(0f, 1f), tween(900), label = "ring")
    Box(modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.matchParentSize()) {
            val s = stroke.toPx()
            val arcSize = Size(this.size.width - s, this.size.height - s)
            val topLeft = Offset(s / 2, s / 2)
            drawArc(track, -90f, 360f, false, topLeft, arcSize, style = Stroke(s, cap = StrokeCap.Round))
            drawArc(color, -90f, 360f * animated, false, topLeft, arcSize, style = Stroke(s, cap = StrokeCap.Round))
        }
        content()
    }
}

/** Ramadan challenge: 30 crescents, [lit] of them glowing. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CrescentRow(lit: Int, modifier: Modifier = Modifier, total: Int = 30) {
    FlowRow(modifier, horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(total) { i ->
            Text("🌙", fontSize = 16.sp, modifier = Modifier.alpha(if (i < lit) 1f else 0.22f))
        }
    }
}

/** Screen header used by the tab screens: large title + supporting line. */
@Composable
fun ScreenHeader(title: String, subtitle: String, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.headlineLarge, color = RihlaTheme.colors.textPrimary)
        Text(subtitle, style = MaterialTheme.typography.bodyLarge, color = RihlaTheme.colors.textSecondary)
    }
}
