package com.rihla.english.ui.progress

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rihla.english.R
import com.rihla.english.core.model.DailyGoalLevel
import com.rihla.english.core.model.ErrorPattern
import com.rihla.english.core.model.PatternCategory
import com.rihla.english.core.season.Season
import com.rihla.english.ui.components.CrescentRow
import com.rihla.english.ui.components.EnglishText
import com.rihla.english.ui.components.RihlaCard
import com.rihla.english.ui.components.ScreenHeader
import com.rihla.english.ui.gamification.GamificationViewModel
import com.rihla.english.ui.gamification.HomeUiState
import com.rihla.english.ui.sound.masteryColor
import com.rihla.english.ui.theme.Coral
import com.rihla.english.ui.theme.EnglishTextStyles
import com.rihla.english.ui.theme.Ember
import com.rihla.english.ui.theme.Oasis
import com.rihla.english.ui.theme.Plum
import com.rihla.english.ui.theme.RihlaTheme
import com.rihla.english.ui.theme.Saffron
import com.rihla.english.ui.theme.Sky
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/** "تقدّمي": weekly XP chart, commitment calendar, personal mistake profile, sound mastery, settings. */
@Composable
fun ProgressScreen(
    viewModel: GamificationViewModel,
    onPracticePattern: (ErrorPattern) -> Unit,
    onOpenSoundLab: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(containerColor = MaterialTheme.colorScheme.background, contentWindowInsets = WindowInsets(0, 0, 0, 0)) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding).statusBarsPadding(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                ScreenHeader(
                    stringResource(R.string.progress_title),
                    stringResource(R.string.level_label, state.stats.level) + "  •  ⚡ ${state.stats.totalXp} XP  •  " +
                        stringResource(R.string.words_learned, state.wordsLearned),
                )
            }
            item { WeeklyChart(state) }
            item { CommitmentCalendar(state) }
            if (state.season == Season.RAMADAN || state.stats.ramadanGoalDays > 0) {
                item {
                    RihlaCard {
                        Text(stringResource(R.string.progress_ramadan), style = MaterialTheme.typography.titleLarge, color = RihlaTheme.colors.textPrimary)
                        CrescentRow(lit = state.stats.ramadanGoalDays)
                        Text(stringResource(R.string.season_ramadan_progress, state.stats.ramadanGoalDays), color = Plum, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
            item { MistakeProfile(state, onPracticePattern) }
            item { SoundSummary(state, onOpenSoundLab) }
            item { Settings(state, viewModel) }
        }
    }
}

@Composable
private fun WeeklyChart(state: HomeUiState) {
    val colors = RihlaTheme.colors
    val history = state.insights.xpHistory(state.todayEpochDay, 7)
    val max = (history.maxOfOrNull { it.second } ?: 0).coerceAtLeast(state.stats.dailyGoalXp)
    val goalFraction = state.stats.dailyGoalXp.toFloat() / max
    RihlaCard {
        Text(stringResource(R.string.progress_week), style = MaterialTheme.typography.titleLarge, color = colors.textPrimary)
        Canvas(Modifier.fillMaxWidth().height(140.dp)) {
            val gap = size.width / (history.size * 3f)
            val barWidth = gap * 2
            history.forEachIndexed { i, (_, xp) ->
                // Index 0 is the oldest day; draw it on the right so time flows right-to-left (RTL).
                val x = size.width - (i + 1) * (barWidth + gap) + gap / 2
                val h = (xp.toFloat() / max) * size.height
                drawRoundRect(colors.border, Offset(x, 0f), Size(barWidth, size.height), CornerRadius(12f, 12f))
                if (h > 0) {
                    drawRoundRect(
                        if (xp >= state.stats.dailyGoalXp) Oasis else Saffron,
                        Offset(x, size.height - h), Size(barWidth, h), CornerRadius(12f, 12f),
                    )
                }
            }
            val y = size.height * (1 - goalFraction)
            drawLine(Ember, Offset(0f, y), Offset(size.width, y), strokeWidth = 4f, pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(14f, 10f)))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            history.reversed().forEach { (day, xp) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(dayLabel(day), style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp), color = colors.textSecondary)
                    Text("$xp", style = MaterialTheme.typography.labelMedium, color = colors.textPrimary)
                }
            }
        }
    }
}

private fun dayLabel(epochDay: Long): String =
    LocalDate.ofEpochDay(epochDay).dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("ar"))

/** Five weeks of activity: filled squares for days with XP, glowing ones for goal days. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CommitmentCalendar(state: HomeUiState) {
    val colors = RihlaTheme.colors
    val today = state.todayEpochDay
    val start = today - 34 // five rows of seven days, today last
    RihlaCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.progress_calendar), style = MaterialTheme.typography.titleLarge, color = colors.textPrimary, modifier = Modifier.weight(1f))
            Text("🔥 ${state.stats.currentStreak}", style = MaterialTheme.typography.titleMedium, color = Ember)
        }
        FlowRow(maxItemsInEachRow = 7, horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            (start..today).forEach { day ->
                val xp = state.insights.day(day).xp
                val color = when {
                    xp >= state.stats.dailyGoalXp -> Oasis
                    xp > 0 -> Saffron.copy(alpha = 0.7f)
                    else -> colors.border
                }
                Box(
                    Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(color),
                    contentAlignment = Alignment.Center,
                ) {
                    if (day == today) Text("•", color = Color.White, fontSize = 18.sp)
                }
            }
        }
    }
}

@Composable
private fun MistakeProfile(state: HomeUiState, onPractice: (ErrorPattern) -> Unit) {
    val colors = RihlaTheme.colors
    val stats = ErrorPattern.grammar.map { it to state.insights.pattern(it) }.filter { it.second.attempts > 0 }
        .sortedBy { it.second.accuracyPercent }
    RihlaCard {
        Text(stringResource(R.string.progress_mistakes), style = MaterialTheme.typography.titleLarge, color = colors.textPrimary)
        if (stats.isEmpty()) {
            Text(stringResource(R.string.progress_mistakes_empty), style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
        }
        stats.forEach { (pattern, stat) ->
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(pattern.emoji, fontSize = 22.sp)
                    Spacer(Modifier.width(8.dp))
                    Column(Modifier.weight(1f)) {
                        Text(pattern.titleAr, style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                        Text(stringResource(R.string.progress_accuracy, stat.accuracyPercent), style = MaterialTheme.typography.labelMedium, color = masteryColor(stat))
                    }
                    TextButton(onClick = { onPractice(pattern) }) {
                        Text(stringResource(R.string.progress_practice_this), color = Sky, style = MaterialTheme.typography.labelLarge)
                    }
                }
                LinearProgressIndicator(
                    progress = { stat.accuracyPercent / 100f },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = masteryColor(stat),
                    trackColor = colors.border,
                )
                EnglishText("❌ ${pattern.exampleWrong}   ✅ ${pattern.exampleRight}", style = EnglishTextStyles.chip.copy(fontSize = 14.sp), color = colors.textSecondary)
            }
        }
    }
}

@Composable
private fun SoundSummary(state: HomeUiState, onOpen: () -> Unit) {
    val colors = RihlaTheme.colors
    RihlaCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.progress_sounds), style = MaterialTheme.typography.titleLarge, color = colors.textPrimary, modifier = Modifier.weight(1f))
            TextButton(onClick = onOpen) { Text(stringResource(R.string.sound_start), color = Sky) }
        }
        ErrorPattern.entries.filter { it.category == PatternCategory.SOUND }.forEach { pattern ->
            val stat = state.insights.pattern(pattern)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(pattern.emoji, fontSize = 18.sp)
                Spacer(Modifier.width(8.dp))
                Text(pattern.titleAr, style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary, modifier = Modifier.width(150.dp))
                LinearProgressIndicator(
                    progress = { stat.accuracyPercent / 100f },
                    modifier = Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = masteryColor(stat),
                    trackColor = colors.border,
                )
                Spacer(Modifier.width(8.dp))
                Text(if (stat.attempts == 0) "—" else "${stat.accuracyPercent}%", style = MaterialTheme.typography.labelMedium, color = colors.textSecondary)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Settings(state: HomeUiState, viewModel: GamificationViewModel) {
    val colors = RihlaTheme.colors
    RihlaCard {
        Text(stringResource(R.string.progress_settings), style = MaterialTheme.typography.titleLarge, color = colors.textPrimary)
        Text(stringResource(R.string.settings_daily_goal), style = MaterialTheme.typography.titleMedium, color = colors.textSecondary)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DailyGoalLevel.entries.forEach { level ->
                FilterChip(
                    selected = state.stats.dailyGoalXp == level.xp,
                    onClick = { viewModel.setDailyGoal(level) },
                    label = { Text("${level.emoji} ${level.titleAr} (${level.xp} XP)") },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Oasis.copy(alpha = 0.2f)),
                )
            }
        }
        Text(stringResource(R.string.settings_reminder), style = MaterialTheme.typography.titleMedium, color = colors.textSecondary)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val profile = state.profile
            ReminderOptions.forEach { (hour, label) ->
                FilterChip(
                    selected = if (hour == null) !profile.remindersEnabled else profile.remindersEnabled && profile.reminderHour == hour,
                    onClick = { viewModel.setReminder(hour) },
                    label = { Text(if (label == 0) stringResource(R.string.settings_reminder_off) else stringResource(label)) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Coral.copy(alpha = 0.18f)),
                )
            }
        }
    }
}

/** Reminder hour → label resource (0 = off). */
val ReminderOptions: List<Pair<Int?, Int>> = listOf(
    8 to R.string.time_8am,
    13 to R.string.time_1pm,
    18 to R.string.time_6pm,
    21 to R.string.time_9pm,
    null to 0,
)

