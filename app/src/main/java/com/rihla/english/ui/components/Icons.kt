package com.rihla.english.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke

/** Thin-stroke line icons drawn on a 24-unit grid (no emoji in the cinematic UI). */
enum class CineIcon { Map, Book, Passport, Play, Speaker, Slow, Bookmark, Mic, Close, Back, Star, Clock, Check, Lock }

@Composable
fun CineIconView(icon: CineIcon, tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val u = size.minDimension / 24f
        val stroke = Stroke(width = 1.8f * u, cap = StrokeCap.Round, join = StrokeJoin.Round)
        fun p(x: Float, y: Float) = Offset(x * u, y * u)
        fun path(block: Path.() -> Unit) = drawPath(Path().apply(block), tint, style = stroke)
        when (icon) {
            CineIcon.Map -> path {
                moveTo(9 * u, 4 * u); lineTo(3 * u, 7 * u); lineTo(3 * u, 20 * u); lineTo(9 * u, 17 * u); lineTo(15 * u, 20 * u)
                lineTo(21 * u, 17 * u); lineTo(21 * u, 4 * u); lineTo(15 * u, 7 * u); close()
                moveTo(9 * u, 4 * u); lineTo(9 * u, 17 * u); moveTo(15 * u, 7 * u); lineTo(15 * u, 20 * u)
            }
            CineIcon.Book -> path {
                moveTo(5 * u, 4 * u); lineTo(16 * u, 4 * u); quadraticTo(19 * u, 4 * u, 19 * u, 7 * u); lineTo(19 * u, 20 * u)
                lineTo(8 * u, 20 * u); quadraticTo(5 * u, 20 * u, 5 * u, 17 * u); close()
                moveTo(5 * u, 17 * u); quadraticTo(5 * u, 14 * u, 8 * u, 14 * u); lineTo(19 * u, 14 * u)
            }
            CineIcon.Passport -> {
                drawRoundRect(tint, p(5f, 3f), Size(14 * u, 18 * u), CornerRadius(2 * u), style = stroke)
                drawCircle(tint, 3 * u, p(12f, 10f), style = stroke)
                drawLine(tint, p(9f, 16f), p(15f, 16f), stroke.width, StrokeCap.Round)
            }
            CineIcon.Play -> drawPath(Path().apply { moveTo(8 * u, 5 * u); lineTo(19 * u, 12 * u); lineTo(8 * u, 19 * u); close() }, tint)
            CineIcon.Speaker -> path {
                moveTo(4 * u, 9 * u); lineTo(8 * u, 9 * u); lineTo(13 * u, 5 * u); lineTo(13 * u, 19 * u); lineTo(8 * u, 15 * u); lineTo(4 * u, 15 * u); close()
                moveTo(16 * u, 9 * u); quadraticTo(19 * u, 12 * u, 16 * u, 15 * u)
            }
            CineIcon.Slow -> {
                drawArc(tint, 180f, 180f, false, p(4f, 8f), Size(14 * u, 12 * u), style = stroke)
                drawLine(tint, p(3f, 14f), p(21f, 14f), stroke.width, StrokeCap.Round)
                drawCircle(tint, 1.8f * u, p(20f, 11f), style = stroke)
            }
            CineIcon.Bookmark -> path { moveTo(6 * u, 3 * u); lineTo(18 * u, 3 * u); lineTo(18 * u, 21 * u); lineTo(12 * u, 17 * u); lineTo(6 * u, 21 * u); close() }
            CineIcon.Mic -> {
                drawRoundRect(tint, p(9f, 3f), Size(6 * u, 11 * u), CornerRadius(3 * u), style = stroke)
                drawArc(tint, 0f, 180f, false, p(5f, 4f), Size(14 * u, 14 * u), style = stroke)
                drawLine(tint, p(12f, 18f), p(12f, 21f), stroke.width, StrokeCap.Round)
            }
            CineIcon.Close -> {
                drawLine(tint, p(6f, 6f), p(18f, 18f), stroke.width, StrokeCap.Round)
                drawLine(tint, p(18f, 6f), p(6f, 18f), stroke.width, StrokeCap.Round)
            }
            CineIcon.Back -> path { moveTo(9 * u, 6 * u); lineTo(15 * u, 12 * u); lineTo(9 * u, 18 * u) }
            CineIcon.Star -> path {
                moveTo(12 * u, 2 * u); lineTo(14.6f * u, 8.4f * u); lineTo(21 * u, 9 * u); lineTo(16 * u, 13.4f * u); lineTo(17.5f * u, 20 * u)
                lineTo(12 * u, 16.6f * u); lineTo(6.5f * u, 20 * u); lineTo(8 * u, 13.4f * u); lineTo(3 * u, 9 * u); lineTo(9.4f * u, 8.4f * u); close()
            }
            CineIcon.Clock -> {
                drawCircle(tint, 9 * u, p(12f, 12f), style = stroke)
                path { moveTo(12 * u, 7 * u); lineTo(12 * u, 12 * u); lineTo(15 * u, 14 * u) }
            }
            CineIcon.Check -> path { moveTo(5 * u, 12.5f * u); lineTo(10 * u, 17 * u); lineTo(19 * u, 7 * u) }
            CineIcon.Lock -> {
                drawRoundRect(tint, p(5f, 11f), Size(14 * u, 10 * u), CornerRadius(2 * u), style = stroke)
                drawArc(tint, 180f, 180f, false, p(8f, 4f), Size(8 * u, 12 * u), style = stroke)
            }
        }
    }
}

