package com.rihla.english.ui.victory

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rihla.english.R
import com.rihla.english.ui.components.RihlaButton
import com.rihla.english.ui.lesson.VictorySummary
import com.rihla.english.ui.theme.Blue
import com.rihla.english.ui.theme.Gold
import com.rihla.english.ui.theme.Green
import com.rihla.english.ui.theme.Orange
import com.rihla.english.ui.theme.Purple
import com.rihla.english.ui.theme.Red
import com.rihla.english.ui.theme.RihlaTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin
import kotlin.random.Random

/** Celebration: confetti, XP / accuracy / streak cards, a reward chest to open, and new badges. */
@Composable
fun LessonVictoryScreen(summary: VictorySummary, onContinue: () -> Unit) {
    val colors = RihlaTheme.colors
    val trophyScale = remember { Animatable(0f) }
    var showStats by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        trophyScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
        showStats = true
    }

    Box(Modifier.fillMaxSize()) {
        Confetti(Modifier.fillMaxSize())
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Spacer(Modifier.height(24.dp))
                Text(
                    if (summary.isPractice) "💪" else "🏆",
                    fontSize = 96.sp,
                    modifier = Modifier.graphicsLayer { scaleX = trophyScale.value; scaleY = trophyScale.value },
                )
                Text(
                    stringResource(if (summary.isPractice) R.string.victory_practice_title else R.string.victory_title),
                    style = MaterialTheme.typography.headlineLarge,
                    color = Gold,
                    textAlign = TextAlign.Center,
                )
                if (summary.isPerfect) {
                    Text(stringResource(R.string.victory_perfect), style = MaterialTheme.typography.titleMedium, color = Green)
                }

                AnimatedVisibility(showStats, enter = fadeIn() + scaleIn(initialScale = 0.8f)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StatCard(stringResource(R.string.victory_xp), "⚡ ${summary.xpEarned}", Gold, Modifier.weight(1f))
                        StatCard(stringResource(R.string.victory_accuracy), "🎯 ${summary.accuracyPercent}%", Green, Modifier.weight(1f))
                        StatCard(
                            stringResource(R.string.victory_streak),
                            "🔥 ${summary.streak}",
                            Orange,
                            Modifier.weight(1f),
                            footnote = if (summary.streakExtended) stringResource(R.string.victory_streak_extended) else null,
                        )
                    }
                }

                if (summary.gemsEarned > 0) {
                    TreasureChestReveal(gems = summary.gemsEarned)
                }

                summary.newLevel?.let { Highlight(stringResource(R.string.victory_level_up, it), Purple) }
                if (summary.dailyGoalReached) Highlight(stringResource(R.string.victory_goal), Orange)
                if (summary.freezeUsed) Highlight(stringResource(R.string.victory_freeze_used), Blue)
                if (summary.heartsGained > 0) Highlight(stringResource(R.string.victory_heart), Red)

                if (summary.badges.isNotEmpty()) {
                    Text(stringResource(R.string.victory_badges), style = MaterialTheme.typography.titleLarge, color = colors.textPrimary)
                    summary.badges.forEach { badge ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Gold.copy(alpha = 0.15f))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(badge.emoji, fontSize = 36.sp)
                            Spacer(Modifier.size(12.dp))
                            Column {
                                Text(badge.titleAr, style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                                Text(badge.descriptionAr, style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
            RihlaButton(
                text = stringResource(R.string.continue_label),
                onClick = onContinue,
                modifier = Modifier.padding(20.dp),
            )
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, color: Color, modifier: Modifier = Modifier, footnote: String? = null) {
    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .border(2.dp, color, RoundedCornerShape(16.dp)),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .background(color)
                .padding(vertical = 6.dp),
            contentAlignment = Alignment.Center,
        ) { Text(label, style = MaterialTheme.typography.labelMedium, color = Color.White) }
        Text(value, style = MaterialTheme.typography.titleLarge, color = color, modifier = Modifier.padding(vertical = 10.dp))
        footnote?.let {
            Text(it, style = MaterialTheme.typography.labelMedium, color = color, modifier = Modifier.padding(bottom = 8.dp))
        }
    }
}

@Composable
private fun Highlight(text: String, color: Color) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        color = color,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(10.dp),
    )
}

/**
 * Chest-opening animation: the closed chest wobbles; tap it (or wait) and it bursts open,
 * revealing the gems earned with a springy count-up.
 */
@Composable
fun TreasureChestReveal(gems: Int, modifier: Modifier = Modifier, autoOpenMillis: Long = 1_800) {
    var opened by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val burst = remember { Animatable(0f) }
    val count = remember { Animatable(0f) }
    val wobble = rememberInfiniteTransition(label = "wobble")
    val angle by wobble.animateFloat(-8f, 8f, infiniteRepeatable(tween(180, easing = LinearEasing), RepeatMode.Reverse), label = "angle")

    fun open() {
        if (opened) return
        opened = true
        scope.launch { burst.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy)) }
        scope.launch { count.animateTo(gems.toFloat(), tween(900)) }
    }
    LaunchedEffect(Unit) {
        delay(autoOpenMillis)
        open()
    }

    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Blue.copy(alpha = 0.1f))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { open() }
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (opened) {
                // Light rays behind the open chest.
                Canvas(
                    Modifier
                        .size(140.dp)
                        .graphicsLayer { scaleX = burst.value; scaleY = burst.value; alpha = burst.value },
                ) {
                    val c = center
                    repeat(12) { i ->
                        rotate(i * 30f, c) {
                            drawRect(Gold.copy(alpha = 0.45f), topLeft = Offset(c.x - 4f, 0f), size = Size(8f, size.height / 2))
                        }
                    }
                }
            }
            Text(
                if (opened) "💰" else "🎁",
                fontSize = 72.sp,
                modifier = Modifier.graphicsLayer {
                    rotationZ = if (opened) 0f else angle
                    val s = if (opened) 0.8f + 0.4f * burst.value else 1f
                    scaleX = s
                    scaleY = s
                },
            )
        }
        Text(
            if (opened) "+${count.value.toInt()} 💎" else stringResource(R.string.victory_chest_tap),
            style = MaterialTheme.typography.headlineMedium,
            color = Blue,
        )
    }
}

private data class Particle(val x: Float, val speed: Float, val size: Float, val phase: Float, val color: Color, val spin: Float)

/** Lightweight Canvas confetti. */
@Composable
private fun Confetti(modifier: Modifier = Modifier, count: Int = 70) {
    val palette = listOf(Gold, Green, Blue, Red, Purple, Orange)
    val particles = remember {
        val r = Random(42)
        List(count) {
            Particle(
                x = r.nextFloat(),
                speed = 0.25f + r.nextFloat() * 0.5f,
                size = 6f + r.nextFloat() * 10f,
                phase = r.nextFloat(),
                color = palette[r.nextInt(palette.size)],
                spin = r.nextFloat() * 360f,
            )
        }
    }
    val time = rememberInfiniteTransition(label = "confetti")
    val t by time.animateFloat(0f, 1f, infiniteRepeatable(tween(4_000, easing = LinearEasing)), label = "t")
    Canvas(modifier) {
        particles.forEach { p ->
            val progress = (p.phase + t * p.speed * 2f) % 1f
            val y = progress * (size.height + 40f) - 20f
            val x = p.x * size.width + sin((progress * 6f + p.phase * 10f).toDouble()).toFloat() * 24f
            rotate(p.spin + progress * 720f, Offset(x, y)) {
                drawRect(p.color, topLeft = Offset(x - p.size / 2, y - p.size / 4), size = Size(p.size, p.size / 2))
            }
        }
    }
}
