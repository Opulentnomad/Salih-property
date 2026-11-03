package com.rihla.english.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rihla.english.R
import com.rihla.english.core.model.DailyPlan
import com.rihla.english.core.model.DailyTask
import com.rihla.english.core.model.PathNode
import com.rihla.english.core.season.Season
import com.rihla.english.ui.components.CrescentRow
import com.rihla.english.ui.components.NightSkyCard
import com.rihla.english.ui.components.PressableSurface
import com.rihla.english.ui.components.ProgressRing
import com.rihla.english.ui.components.RihlaButton
import com.rihla.english.ui.components.RihlaCard
import com.rihla.english.ui.gamification.HomeUiState
import com.rihla.english.ui.theme.Ember
import com.rihla.english.ui.theme.Night
import com.rihla.english.ui.theme.Oasis
import com.rihla.english.ui.theme.OasisDark
import com.rihla.english.ui.theme.RihlaTheme
import com.rihla.english.ui.theme.Saffron
import com.rihla.english.ui.theme.SaffronDark
import java.time.LocalTime

/** Greeting + daily-goal ring + streak on a night-sky card. */
@Composable
fun HeroCard(state: HomeUiState, modifier: Modifier = Modifier) {
    val stats = state.stats
    val greeting = stringResource(if (LocalTime.now().hour < 12) R.string.greeting_morning else R.string.greeting_evening)
    val name = state.profile.name
    val progress = stats.dailyXp.toFloat() / stats.dailyGoalXp.coerceAtLeast(1)
    NightSkyCard(modifier.padding(horizontal = 16.dp)) {
        Row(
            Modifier.fillMaxWidth().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    if (name.isBlank()) stringResource(R.string.greeting_anonymous, greeting) else stringResource(R.string.greeting_line, greeting, name),
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                )
                state.profile.goal?.let {
                    Text("${it.emoji} ${it.titleAr}", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f))
                }
                Text(
                    if (stats.dailyGoalReached) stringResource(R.string.hero_goal_done)
                    else stringResource(R.string.hero_goal_progress, stats.dailyXp, stats.dailyGoalXp),
                    style = MaterialTheme.typography.titleMedium,
                    color = Saffron,
                )
                if (stats.currentStreak > 0) {
                    Text(stringResource(R.string.hero_streak, stats.currentStreak), style = MaterialTheme.typography.labelLarge, color = Ember)
                }
            }
            ProgressRing(progress = progress, size = 96.dp, color = if (stats.dailyGoalReached) Oasis else Saffron) {
                Text(if (stats.dailyGoalReached) "🏆" else "🐪", fontSize = 38.sp)
            }
        }
    }
}

/** Seasonal banner: Ramadan crescent challenge, Eid streak protection, National Day... */
@Composable
fun SeasonBanner(state: HomeUiState, modifier: Modifier = Modifier) {
    val season = state.season ?: return
    val color = Color(season.colorHex)
    Column(
        modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(listOf(color, Night)))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(season.emoji, fontSize = 30.sp)
            Spacer(Modifier.width(10.dp))
            Column {
                Text(season.titleAr, style = MaterialTheme.typography.titleLarge, color = Color.White)
                state.ramadanDay?.let {
                    Text(stringResource(R.string.season_ramadan_day, it), style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.8f))
                }
            }
        }
        Text(season.messageAr, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.92f))
        if (season == Season.RAMADAN) {
            val lit = state.stats.ramadanGoalDays
            CrescentRow(lit = lit)
            Text(stringResource(R.string.season_ramadan_progress, lit), style = MaterialTheme.typography.labelLarge, color = Saffron)
        }
    }
}

/** "خطة اليوم": three small tasks and a bonus chest when all are done. */
@Composable
fun DailyPlanCard(state: HomeUiState, onClaim: () -> Unit, modifier: Modifier = Modifier) {
    val colors = RihlaTheme.colors
    RihlaCard(modifier.padding(horizontal = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("🗺️", fontSize = 26.sp)
            Spacer(Modifier.width(10.dp))
            Text(stringResource(R.string.daily_plan_title), style = MaterialTheme.typography.titleLarge, color = colors.textPrimary, modifier = Modifier.weight(1f))
            Text("${state.dailyTasks.count { it.done }}/${state.dailyTasks.size}", style = MaterialTheme.typography.titleMedium, color = Oasis)
        }
        state.dailyTasks.forEach { TaskRow(it) }
        when {
            state.dailyBonusClaimed -> Text(stringResource(R.string.daily_plan_claimed), style = MaterialTheme.typography.labelLarge, color = Oasis)
            state.allDailyTasksDone -> RihlaButton(
                text = stringResource(R.string.daily_plan_bonus, DailyPlan.BONUS_GEMS),
                onClick = onClaim,
                color = Saffron,
                shadowColor = SaffronDark,
                textColor = Night,
            )
            else -> Text(stringResource(R.string.daily_plan_hint), style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
        }
    }
}

@Composable
private fun TaskRow(task: DailyTask) {
    val colors = RihlaTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(if (task.done) Oasis else Color.Transparent)
                .border(2.dp, if (task.done) Oasis else colors.border, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (task.done) Text("✓", color = Color.White, fontSize = 16.sp)
        }
        Spacer(Modifier.width(12.dp))
        Text(task.kind.emoji, fontSize = 20.sp)
        Spacer(Modifier.width(8.dp))
        Text(
            task.kind.titleAr,
            style = MaterialTheme.typography.titleMedium.copy(textDecoration = if (task.done) TextDecoration.LineThrough else null),
            color = if (task.done) colors.textSecondary else colors.textPrimary,
        )
    }
}

/** Big "continue your journey" card pointing at the next lesson. */
@Composable
fun ContinueCard(node: PathNode, unitColor: Color, onStart: () -> Unit, modifier: Modifier = Modifier) {
    PressableSurface(
        onClick = onStart,
        modifier = modifier.padding(horizontal = 16.dp),
        color = unitColor,
        shadowColor = OasisDark.copy(alpha = 0.9f),
        shape = RoundedCornerShape(24.dp),
        depth = 6.dp,
    ) {
        Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(64.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center,
            ) { Text(node.iconEmoji, fontSize = 32.sp) }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.continue_title), style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.85f))
                Text(node.titleAr, style = MaterialTheme.typography.titleLarge, color = Color.White)
            }
            Text(
                stringResource(R.string.continue_cta) + " ←",
                style = MaterialTheme.typography.labelLarge,
                color = Night,
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(Saffron)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            )
        }
    }
}
