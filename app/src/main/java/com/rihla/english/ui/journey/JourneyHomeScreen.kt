package com.rihla.english.ui.journey

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rihla.english.R
import com.rihla.english.core.journey.ChapterStatus
import com.rihla.english.core.journey.ChapterView
import com.rihla.english.core.journey.Mission
import com.rihla.english.ui.components.CineIcon
import com.rihla.english.ui.components.CineIconView
import com.rihla.english.ui.components.EnglishText
import com.rihla.english.ui.components.SceneBackdrop
import com.rihla.english.ui.theme.Cine
import com.rihla.english.ui.theme.EnglishTextStyles
import java.time.LocalTime

/** "الرحلة": the current chapter as a film still, the journey timeline and today's mission. */
@Composable
fun JourneyHomeScreen(
    viewModel: JourneyViewModel,
    onOpenScene: (String) -> Unit,
    onOpenMission: () -> Unit,
    onOpenReview: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LazyColumn(
        Modifier.fillMaxSize().background(Cine.Ink).statusBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item { Header(state) }
        item { HeroCard(state, onOpenScene) }
        if (state.season != null) {
            item {
                Text(
                    state.season!!.messageAr,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Cine.TextSoft,
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Cine.Panel).padding(14.dp),
                )
            }
        }
        if (state.dueCount > 0) {
            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Cine.BlueTint)
                        .clickable(role = Role.Button, onClick = onOpenReview)
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CineIconView(CineIcon.Book, Cine.BlueSoft, Modifier.size(22.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(stringResource(R.string.review_banner, state.dueCount), style = MaterialTheme.typography.titleMedium, color = Cine.BlueSoft)
                }
            }
        }
        item { Text(stringResource(R.string.journey_timeline), style = MaterialTheme.typography.titleLarge) }
        items(state.chapters, key = { it.chapter.id }) { ChapterRow(it) }
        state.activeMission?.let { mission -> item { MissionCard(mission, onOpenMission) } }
    }
}

@Composable
private fun Header(state: JourneyUiState) {
    val greeting = stringResource(if (LocalTime.now().hour < 12) R.string.greeting_morning else R.string.greeting_evening)
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineLarge)
            Text(
                if (state.profile.name.isBlank()) greeting else stringResource(R.string.greeting_line, greeting, state.profile.name),
                style = MaterialTheme.typography.bodyMedium,
                color = Cine.Muted,
            )
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                Modifier.border(1.dp, Cine.Line, RoundedCornerShape(999.dp)).padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CineIconView(CineIcon.Clock, Cine.Amber, Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.minutes_today, state.minutesToday, state.profile.dailyMinutes), style = MaterialTheme.typography.labelMedium)
            }
            if (state.commitmentDays > 0) {
                Text(stringResource(R.string.commitment_days, state.commitmentDays), style = MaterialTheme.typography.labelMedium, color = Cine.AmberSoft)
            }
        }
    }
}

@Composable
private fun HeroCard(state: JourneyUiState, onOpenScene: (String) -> Unit) {
    val chapter = state.nextChapter
    val scene = state.nextScene
    Box(
        Modifier
            .fillMaxWidth()
            .height(330.dp)
            .clip(RoundedCornerShape(28.dp))
            .clickable(enabled = scene != null, role = Role.Button) { scene?.let { onOpenScene(it.id) } },
    ) {
        SceneBackdrop(scene?.art ?: state.chapters.lastOrNull()?.chapter?.art ?: com.rihla.english.core.journey.SceneArt.DINNER, Modifier.fillMaxSize())
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(0.35f to Color.Transparent, 1f to Cine.Ink.copy(alpha = 0.94f))))
        Column(
            Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (chapter != null && scene != null) {
                Text(
                    stringResource(R.string.chapter_scene_progress, chapter.number, scene.index + 1, chapter.scenes.size),
                    style = MaterialTheme.typography.labelMedium,
                    color = Cine.Amber,
                )
                Text(chapter.titleAr, style = MaterialTheme.typography.headlineLarge)
                EnglishText("Chapter ${chapter.number} — ${chapter.titleEn}", style = EnglishTextStyles.small, color = Cine.TextSoft)
                Text(scene.titleAr, style = MaterialTheme.typography.bodyMedium, color = Cine.TextSoft)
                Row(Modifier.padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Row(
                        Modifier.clip(RoundedCornerShape(999.dp)).background(Cine.Amber).padding(horizontal = 18.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CineIconView(CineIcon.Play, Cine.Ink, Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            stringResource(if (scene.index == 0) R.string.start_scene else R.string.continue_scene),
                            style = MaterialTheme.typography.labelLarge,
                            color = Cine.Ink,
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    LinearProgressIndicator(
                        progress = { scene.index.toFloat() / chapter.scenes.size },
                        modifier = Modifier.weight(1f).height(4.dp).clip(RoundedCornerShape(2.dp)),
                        color = Cine.Amber,
                        trackColor = Cine.Text.copy(alpha = 0.2f),
                    )
                }
            } else {
                Text(stringResource(R.string.journey_complete), style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}

@Composable
private fun ChapterRow(view: ChapterView) {
    val ch = view.chapter
    val (tile, tileText, title, stateColor, label) = when (view.status) {
        ChapterStatus.DONE -> Quint(Cine.Amber, Cine.Ink, Cine.Text, Cine.BlueSoft, R.string.chapter_done)
        ChapterStatus.CURRENT -> Quint(Cine.Text, Cine.Ink, Cine.Text, Cine.Amber, R.string.chapter_now)
        ChapterStatus.LOCKED -> Quint(Cine.PanelBorder, Cine.Faint, Cine.Muted, Cine.Faint, R.string.chapter_locked)
    }
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Cine.Panel)
            .border(1.dp, Cine.PanelBorder, RoundedCornerShape(18.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(tile), contentAlignment = Alignment.Center) {
            EnglishText(ch.code, style = EnglishTextStyles.stamp.copy(fontSize = 12.sp), color = tileText)
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(stringResource(R.string.chapter_line, ch.number, ch.titleAr), style = MaterialTheme.typography.titleMedium, color = title)
            Text(ch.subtitleAr, style = MaterialTheme.typography.labelMedium, color = Cine.Muted)
        }
        if (view.status == ChapterStatus.LOCKED) CineIconView(CineIcon.Lock, Cine.Faint, Modifier.size(18.dp))
        else Text(stringResource(label), style = MaterialTheme.typography.labelMedium, color = stateColor)
    }
}

private data class Quint(val a: Color, val b: Color, val c: Color, val d: Color, val e: Int)

@Composable
private fun MissionCard(mission: Mission, onOpen: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, Cine.Amber, RoundedCornerShape(20.dp))
            .clickable(role = Role.Button, onClick = onOpen)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(42.dp).clip(CircleShape).background(Cine.AmberTint), contentAlignment = Alignment.Center) {
            CineIconView(CineIcon.Star, Cine.Amber, Modifier.size(22.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(stringResource(R.string.mission_today), style = MaterialTheme.typography.labelMedium, color = Cine.Amber)
            Text(mission.titleAr, style = MaterialTheme.typography.titleMedium)
        }
    }
}
