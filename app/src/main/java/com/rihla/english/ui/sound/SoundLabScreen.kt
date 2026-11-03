package com.rihla.english.ui.sound

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rihla.english.R
import com.rihla.english.core.model.ErrorPattern
import com.rihla.english.core.model.PatternStat
import com.rihla.english.core.sound.SoundLab
import com.rihla.english.ui.components.EnglishText
import com.rihla.english.ui.components.Ltr
import com.rihla.english.ui.components.MiniSpeaker
import com.rihla.english.ui.components.ProgressRing
import com.rihla.english.ui.components.RihlaButton
import com.rihla.english.ui.components.RihlaCard
import com.rihla.english.ui.components.ScreenHeader
import com.rihla.english.ui.gamification.GamificationViewModel
import com.rihla.english.ui.theme.Coral
import com.rihla.english.ui.theme.EnglishTextStyles
import com.rihla.english.ui.theme.Oasis
import com.rihla.english.ui.theme.OasisDark
import com.rihla.english.ui.theme.RihlaTheme
import com.rihla.english.ui.theme.Saffron

/** "مختبر النطق": one card per difficult sound with a mastery ring and a drill button. */
@Composable
fun SoundLabScreen(viewModel: GamificationViewModel, onStartDrill: (ErrorPattern) -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(containerColor = MaterialTheme.colorScheme.background, contentWindowInsets = WindowInsets(0, 0, 0, 0)) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding).statusBarsPadding(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item { ScreenHeader(stringResource(R.string.sounds_title), stringResource(R.string.sounds_subtitle)) }
            items(ErrorPattern.sounds, key = { it.name }) { pattern ->
                SoundCard(pattern, state.insights.pattern(pattern), onStart = { onStartDrill(pattern) })
            }
        }
    }
}

/** Mastery colour: coral → saffron → oasis as accuracy grows. */
fun masteryColor(stat: PatternStat) = when {
    stat.attempts == 0 -> Saffron
    stat.accuracyPercent >= 80 -> Oasis
    stat.accuracyPercent >= 50 -> Saffron
    else -> Coral
}

@Composable
private fun SoundCard(pattern: ErrorPattern, stat: PatternStat, onStart: () -> Unit) {
    val colors = RihlaTheme.colors
    val pair = SoundLab.drills[pattern]?.pairs?.first()
    RihlaCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ProgressRing(
                progress = stat.accuracyPercent / 100f,
                size = 72.dp,
                stroke = 8.dp,
                color = masteryColor(stat),
                track = colors.border,
            ) { Text(pattern.emoji, fontSize = 28.sp) }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(pattern.titleAr, style = MaterialTheme.typography.titleLarge, color = colors.textPrimary)
                Text(
                    if (stat.attempts == 0) stringResource(R.string.sound_not_started) else stringResource(R.string.sound_mastery, stat.accuracyPercent),
                    style = MaterialTheme.typography.labelLarge,
                    color = masteryColor(stat),
                )
            }
        }
        Text(pattern.explanationAr, style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
        if (pair != null) {
            Ltr {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MiniSpeaker(pair.target)
                    EnglishText(pair.target, style = EnglishTextStyles.medium, color = Oasis)
                    Text("↔", fontSize = 20.sp, color = colors.textSecondary)
                    MiniSpeaker(pair.other)
                    EnglishText(pair.other, style = EnglishTextStyles.medium, color = colors.textSecondary)
                }
            }
        }
        RihlaButton(text = stringResource(R.string.sound_start) + " 🎧", onClick = onStart, color = Oasis, shadowColor = OasisDark)
    }
}
