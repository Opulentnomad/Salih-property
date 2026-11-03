package com.rihla.english.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import com.rihla.english.core.journey.SceneArt
import com.rihla.english.ui.theme.Cine
import com.rihla.english.ui.theme.InterFont

private val Skin1 = Color(0xFFC9A27A)
private val Skin2 = Color(0xFF8B6A4E)
private val Cream = Color(0xFFE9E3D8)

/**
 * Code-drawn cinematic backdrops, one per [SceneArt]. Everything is laid out on a 390×360
 * design grid and scaled to the available size, so scenes stay crisp at any resolution.
 */
@Composable
fun SceneBackdrop(art: SceneArt, modifier: Modifier = Modifier) {
    val measurer = rememberTextMeasurer()
    Canvas(modifier) {
        val sx = size.width / 390f
        val sy = size.height / 360f
        val g = Grid(sx, sy, this, measurer)
        when (art) {
            SceneArt.DEPARTURES -> g.departures()
            SceneArt.PLANE -> g.plane()
            SceneArt.ARRIVALS -> g.arrivals()
            SceneArt.TAXI -> g.street()
            SceneArt.HOTEL -> g.hotel()
            SceneArt.CAFE -> g.cafe()
            SceneArt.TUBE -> g.tube()
            SceneArt.SHOP -> g.shop()
            SceneArt.CLINIC -> g.clinic()
            SceneArt.CAMPUS -> g.campus()
            SceneArt.OFFICE -> g.office()
            SceneArt.DINNER -> g.dinner()
        }
    }
}

/** Tiny drawing DSL in design-grid units. */
private class Grid(val sx: Float, val sy: Float, val d: DrawScope, val measurer: TextMeasurer) {
    fun o(x: Float, y: Float) = Offset(x * sx, y * sy)
    fun s(w: Float, h: Float) = Size(w * sx, h * sy)

    fun sky(vararg colors: Long) = d.drawRect(Brush.verticalGradient(colors.map { Color(it) }), size = d.size)
    fun rect(x: Float, y: Float, w: Float, h: Float, c: Color, r: Float = 0f) =
        d.drawRoundRect(c, o(x, y), s(w, h), CornerRadius(r * sx, r * sy))
    fun outline(x: Float, y: Float, w: Float, h: Float, c: Color, r: Float = 0f, width: Float = 2f) =
        d.drawRoundRect(c, o(x, y), s(w, h), CornerRadius(r * sx, r * sy), style = Stroke(width * sx))
    fun circle(x: Float, y: Float, r: Float, c: Color) = d.drawCircle(c, r * sx, o(x, y))
    fun ring(x: Float, y: Float, r: Float, c: Color, width: Float) = d.drawCircle(c, r * sx, o(x, y), style = Stroke(width * sx))
    fun line(x1: Float, y1: Float, x2: Float, y2: Float, c: Color, width: Float = 2f) = d.drawLine(c, o(x1, y1), o(x2, y2), width * sx)
    fun poly(c: Color, vararg pts: Float) {
        val p = Path()
        pts.toList().chunked(2).forEachIndexed { i, (x, y) -> if (i == 0) p.moveTo(x * sx, y * sy) else p.lineTo(x * sx, y * sy) }
        p.close(); d.drawPath(p, c)
    }
    fun text(t: String, x: Float, y: Float, c: Color, size: Float, weight: FontWeight = FontWeight.ExtraBold) {
        val layout = measurer.measure(t, TextStyle(fontFamily = InterFont, fontSize = (size * sx).sp, fontWeight = weight, color = c, letterSpacing = (size * 0.12f * sx).sp))
        d.drawText(layout, topLeft = Offset(x * sx - layout.size.width / 2f, y * sy - layout.size.height / 2f))
    }
    /** A standing person: head + shoulders, seen from the front. */
    fun person(x: Float, base: Float, skin: Color, cloth: Color, scale: Float = 1f) {
        circle(x, base - 104 * scale, 22 * scale, skin)
        val p = Path().apply {
            moveTo((x - 36 * scale) * sx, base * sy)
            cubicTo((x - 36 * scale) * sx, (base - 62 * scale) * sy, (x + 36 * scale) * sx, (base - 62 * scale) * sy, (x + 36 * scale) * sx, base * sy)
            close()
        }
        d.drawPath(p, cloth)
    }
    /** A traveller with a white thobe and ghutra, the story's hero. */
    fun hero(x: Float, base: Float, scale: Float = 1f) {
        person(x, base, Skin2, Cream, scale)
        poly(Cream, x - 24 * scale, base - 112 * scale, x, base - 132 * scale, x + 24 * scale, base - 112 * scale, x + 26 * scale, base - 84 * scale, x - 26 * scale, base - 84 * scale)
        circle(x, base - 100 * scale, 15 * scale, Skin2)
    }
    fun floor(y: Float, c: Color = Color(0xFF0B0F1C)) = rect(0f, y, 390f, 360f - y, c)
    fun skyline(base: Float, c: Color) {
        rect(20f, base - 44, 34f, 44f, c); rect(60f, base - 70, 22f, 70f, c); rect(68f, base - 90, 6f, 22f, c)
        rect(94f, base - 36, 48f, 36f, c); poly(c, 158f, base, 158f, base - 90, 170f, base - 108, 182f, base - 90, 182f, base)
        rect(194f, base - 54, 40f, 54f, c); ring(262f, base - 44, 34f, c, 5f)
        line(262f, base - 78, 262f, base - 10, c, 3f); line(228f, base - 44, 296f, base - 44, c, 3f)
        rect(304f, base - 32, 30f, 32f, c); rect(340f, base - 50, 40f, 50f, c)
    }

    // --------------------------------------------------------------------------------- scenes

    fun departures() {
        sky(0xFF0E1428, 0xFF1C2546)
        rect(20f, 40f, 350f, 150f, Color(0xFF0A0F1F), 8f)
        for (i in 0..5) circle(40f + i * 60f, 168f, 3f, Cine.Amber)
        poly(Color(0xFF2A3354), 90f, 150f, 300f, 140f, 320f, 150f, 110f, 158f)
        poly(Color(0xFF2A3354), 190f, 146f, 230f, 118f, 244f, 120f, 222f, 146f)
        rect(250f, 210f, 120f, 46f, Color(0xFF141B30), 4f)
        text("DEPARTURES", 310f, 233f, Cine.Amber, 11f)
        rect(30f, 240f, 190f, 60f, Color(0xFF262F4F), 6f)
        person(120f, 240f, Skin1, Color(0xFF1F3A5F), 0.8f)
        floor(300f)
        hero(300f, 330f, 0.9f)
        rect(330f, 290f, 34f, 40f, Cine.Blue, 6f)
    }

    fun plane() {
        sky(0xFF1A2038, 0xFF262D4A)
        for (i in 0..3) {
            val x = 40f + i * 92f
            rect(x, 60f, 56f, 80f, Color(0xFF0E1428), 28f)
            rect(x + 8f, 70f, 40f, 60f, Color(0xFF3A4C7A), 20f)
            circle(x + 30f, 88f, 6f, Cine.AmberSoft.copy(alpha = 0.7f))
        }
        for (i in 0..2) {
            val x = 30f + i * 120f
            rect(x, 170f, 90f, 130f, Color(0xFF2E3657), 18f)
            rect(x + 10f, 160f, 70f, 30f, Color(0xFF3A4370), 10f)
        }
        person(270f, 300f, Skin1, Color(0xFF8A3B48), 0.95f)
        rect(150f, 250f, 70f, 50f, Color(0xFFB9B2A3), 6f)
        floor(300f, Color(0xFF151A2E))
    }

    fun arrivals() {
        sky(0xFF11172A, 0xFF1D2540)
        rect(0f, 40f, 390f, 70f, Color(0xFF141B30))
        rect(120f, 58f, 150f, 34f, Cine.Amber, 4f)
        text("ARRIVALS", 195f, 75f, Cine.Ink, 15f)
        for (x in listOf(60f, 160f, 240f, 330f)) line(x, 300f, x + (x - 195f) * 0.3f, 360f, Color(0xFF2E3757))
        line(0f, 300f, 390f, 300f, Color(0xFF2E3757))
        rect(196f, 170f, 170f, 130f, Color(0xFF262F4F), 6f)
        rect(196f, 160f, 170f, 16f, Color(0xFF323C62), 4f)
        person(300f, 230f, Skin1, Color(0xFF1F3A5F), 0.9f)
        rect(282f, 118f, 36f, 12f, Color(0xFF13213A), 3f)
        hero(100f, 300f)
        rect(140f, 250f, 40f, 50f, Cine.Blue, 6f)
    }

    fun street() {
        sky(0xFF1B2340, 0xFF3A2E4A, 0xFFC9783A)
        circle(300f, 200f, 46f, Cine.AmberSoft.copy(alpha = 0.5f))
        skyline(240f, Cine.Ink.copy(alpha = 0.92f))
        floor(240f, Cine.Ink)
        rect(60f, 270f, 230f, 50f, Color(0xFF05070D), 18f)
        rect(100f, 246f, 140f, 34f, Color(0xFF05070D), 14f)
        rect(112f, 252f, 54f, 22f, Color(0xFF3A4C7A), 6f); rect(174f, 252f, 54f, 22f, Color(0xFF3A4C7A), 6f)
        circle(110f, 322f, 16f, Color(0xFF1A1F2E)); circle(240f, 322f, 16f, Color(0xFF1A1F2E))
        rect(150f, 238f, 40f, 10f, Cine.Amber, 3f)
        circle(284f, 290f, 6f, Cine.AmberSoft)
    }

    fun hotel() {
        sky(0xFF2A1E1A, 0xFF3B2A22)
        for (i in 0..2) rect(40f + i * 120f, 40f, 80f, 120f, Color(0xFF4A3529), 40f)
        circle(195f, 60f, 18f, Cine.AmberSoft.copy(alpha = 0.8f))
        line(195f, 30f, 195f, 44f, Cine.AmberSoft, 2f)
        rect(40f, 200f, 310f, 100f, Color(0xFF5A3E2B), 8f)
        rect(40f, 196f, 310f, 12f, Color(0xFF7A563C), 6f)
        person(250f, 200f, Skin1, Color(0xFF1F2A44), 0.85f)
        rect(80f, 150f, 70f, 40f, Color(0xFF2A1E1A), 4f)
        for (r in 0..1) for (c in 0..3) circle(92f + c * 15f, 162f + r * 16f, 3f, Cine.Amber)
        floor(300f, Color(0xFF1A120E))
    }

    fun cafe() {
        sky(0xFF1E2A2E, 0xFF2B3A3C)
        rect(30f, 40f, 200f, 150f, Color(0xFF3E5C66), 6f)
        for (i in 0..8) line(40f + i * 22f, 50f, 34f + i * 22f, 80f, Color(0xFF8FB3BF), 1.5f)
        for (i in 0..7) line(50f + i * 22f, 110f, 44f + i * 22f, 140f, Color(0xFF8FB3BF), 1.5f)
        rect(250f, 70f, 110f, 120f, Color(0xFF3A2C24), 8f)
        rect(268f, 90f, 74f, 50f, Color(0xFFB9B2A3), 6f)
        circle(305f, 160f, 8f, Cine.Amber)
        rect(20f, 230f, 350f, 70f, Color(0xFF4A3426), 8f)
        rect(160f, 200f, 34f, 30f, Cream, 6f)
        outline(190f, 206f, 14f, 14f, Cream, 7f, 3f)
        line(170f, 196f, 166f, 184f, Cream.copy(alpha = 0.5f), 2f); line(182f, 196f, 186f, 182f, Cream.copy(alpha = 0.5f), 2f)
        floor(300f, Color(0xFF151C1E))
    }

    fun tube() {
        sky(0xFF14182A, 0xFF1F2440)
        d.drawArc(Color(0xFF0A0D18), 180f, 180f, true, o(20f, 60f), s(350f, 360f))
        ring(195f, 120f, 46f, Color(0xFFD2393F), 14f)
        rect(130f, 110f, 130f, 22f, Color(0xFF1C3F94), 2f)
        text("UNDERGROUND", 195f, 121f, Color.White, 9f)
        rect(0f, 250f, 390f, 14f, Cine.Amber)
        floor(264f, Color(0xFF0E1120))
        for (i in 0..4) rect(30f + i * 72f, 200f, 50f, 40f, Color(0xFF263056), 6f)
        hero(320f, 340f, 0.85f)
    }

    fun shop() {
        sky(0xFF241B2E, 0xFF34284A)
        rect(30f, 40f, 330f, 200f, Color(0xFF1A1424), 8f)
        line(50f, 70f, 340f, 70f, Color(0xFFB9B2A3), 3f)
        val colors = listOf(Color(0xFF8A3B48), Color(0xFF1F3A5F), Cine.Amber, Color(0xFF3C6E5A), Cream)
        colors.forEachIndexed { i, c ->
            val x = 70f + i * 58f
            line(x, 70f, x, 84f, Color(0xFFB9B2A3), 2f)
            poly(c, x - 22f, 92f, x + 22f, 92f, x + 26f, 180f, x - 26f, 180f)
            poly(c, x - 22f, 92f, x - 36f, 130f, x - 26f, 136f, x - 16f, 104f)
            poly(c, x + 22f, 92f, x + 36f, 130f, x + 26f, 136f, x + 16f, 104f)
        }
        floor(280f, Color(0xFF140F1C))
        person(300f, 300f, Skin1, Color(0xFF5A2E6A), 0.85f)
        hero(90f, 330f, 0.85f)
    }

    fun clinic() {
        sky(0xFF16302F, 0xFF1F3F3E)
        rect(250f, 50f, 90f, 90f, Color(0xFF0E2221), 8f)
        rect(283f, 64f, 24f, 62f, Cream, 3f); rect(264f, 83f, 62f, 24f, Cream, 3f)
        rect(30f, 210f, 200f, 50f, Color(0xFFDCE6E4), 10f)
        rect(30f, 190f, 60f, 30f, Color(0xFFF2F5F4), 12f)
        person(300f, 300f, Skin1, Color(0xFFF2F5F4), 0.9f)
        line(282f, 230f, 300f, 262f, Color(0xFF3C6E6A), 3f); circle(300f, 264f, 6f, Color(0xFF3C6E6A))
        floor(300f, Color(0xFF0C1B1A))
    }

    fun campus() {
        sky(0xFF1F2A44, 0xFF6C4A3A)
        rect(40f, 90f, 310f, 190f, Color(0xFF7A3B2E), 4f)
        poly(Color(0xFF5A2A20), 30f, 92f, 195f, 30f, 360f, 92f)
        for (i in 0..4) {
            val x = 62f + i * 58f
            rect(x, 120f, 34f, 56f, Color(0xFF1A1F33), 17f)
            rect(x, 200f, 34f, 56f, Color(0xFF1A1F33), 17f)
            circle(x + 17f, 140f, 5f, Cine.AmberSoft.copy(alpha = 0.7f))
        }
        circle(195f, 66f, 14f, Cream)
        floor(280f, Color(0xFF1E2A1E))
        circle(350f, 230f, 40f, Color(0xFF2E4A2E)); rect(345f, 250f, 10f, 40f, Color(0xFF3A2A1E))
        hero(110f, 340f, 0.8f)
    }

    fun office() {
        sky(0xFF101A2E, 0xFF243656)
        for (b in 0..3) {
            val x = 20f + b * 92f
            val top = 60f + (b % 2) * 40f
            rect(x, top, 78f, 300f - top, Color(0xFF1A2846), 2f)
            var y = top + 12f
            while (y < 280f) {
                for (c in 0..2) rect(x + 8f + c * 23f, y, 16f, 14f, if ((b + c + y.toInt() / 28) % 3 == 0) Cine.AmberSoft.copy(alpha = 0.7f) else Color(0xFF2F4670), 2f)
                y += 28f
            }
        }
        floor(300f, Color(0xFF0A1222))
        person(300f, 330f, Skin1, Color(0xFF2E2E3A), 0.85f)
        hero(90f, 330f, 0.85f)
    }

    fun dinner() {
        sky(0xFF2A1A16, 0xFF3E2620)
        rect(120f, 40f, 150f, 110f, Color(0xFF101628), 6f)
        circle(230f, 70f, 10f, Cream.copy(alpha = 0.8f))
        line(195f, 40f, 195f, 150f, Color(0xFF3E2620), 4f); line(120f, 95f, 270f, 95f, Color(0xFF3E2620), 4f)
        d.drawOval(Color(0xFF6A4630), o(30f, 220f), s(330f, 80f))
        for (x in listOf(90f, 195f, 300f)) d.drawOval(Cream, o(x - 28f, 240f), s(56f, 22f))
        for (x in listOf(150f, 240f)) {
            rect(x - 4f, 200f, 8f, 40f, Cream, 3f)
            circle(x, 192f, 6f, Cine.AmberSoft)
            circle(x, 192f, 16f, Cine.Amber.copy(alpha = 0.2f))
        }
        person(80f, 220f, Skin1, Color(0xFF3C6E5A), 0.75f)
        person(310f, 220f, Color(0xFFE2BFA0), Color(0xFF8A3B48), 0.75f)
        floor(300f, Color(0xFF1A100D))
    }
}
