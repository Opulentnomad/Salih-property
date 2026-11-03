package com.rihla.english.ui.passport

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rihla.english.R
import com.rihla.english.core.journey.Chapter
import com.rihla.english.core.journey.Mission
import com.rihla.english.core.journey.MissionState
import com.rihla.english.core.journey.Script
import com.rihla.english.core.journey.Stamp
import com.rihla.english.core.journey.StampKind
import com.rihla.english.ui.components.CineButton
import com.rihla.english.ui.components.CineIcon
import com.rihla.english.ui.components.CineIconView
import com.rihla.english.ui.components.CineOutlinedButton
import com.rihla.english.ui.components.EnglishText
import com.rihla.english.ui.journey.JourneyViewModel
import com.rihla.english.ui.theme.Cine
import com.rihla.english.ui.theme.EnglishTextStyles

private val stampColors = listOf(Cine.Blue, Cine.Amber, Color(0xFFC9A27A), Color(0xFFB98ADB), Color(0xFF7CC6A4))

/** A round passport stamp. Unearned stamps are drawn dashed and dim. */
@Composable
fun PassportStamp(code: String, en: String, earned: Boolean, colorIndex: Int, size: Dp = 96.dp, tilt: Float = 0f) {
    val color = if (earned) stampColors[colorIndex % stampColors.size] else Cine.PanelBorder
    Box(Modifier.size(size).rotate(if (earned) tilt else 0f), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val w = this.size.minDimension
            drawCircle(color, w / 2 - 3.dp.toPx(), style = Stroke(3.dp.toPx(), pathEffect = if (earned) null else PathEffect.dashPathEffect(floatArrayOf(10f, 8f))))
            if (earned) drawCircle(color, w / 2 - 10.dp.toPx(), style = Stroke(1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            EnglishText(code, style = EnglishTextStyles.stamp.copy(fontSize = (size.value * 0.22f).sp), color = if (earned) color else Cine.Faint)
            EnglishText(en, style = EnglishTextStyles.stamp.copy(fontSize = (size.value * 0.075f).sp), color = if (earned) color else Cine.Faint)
        }
    }
}

/** "جوازي": profile card and every stamp of the journey; mission stamps open the mission. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassportScreen(viewModel: JourneyViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var openMission by rememberSaveable { mutableStateOf<String?>(null) }
    val earned = state.stamps.count { it.earned }
    val unlockedMissions = state.chapters.filter { it.scenesDone == it.chapter.scenes.size }.map { it.chapter.mission.stampCode }.toSet()

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize().background(Cine.Ink).statusBarsPadding(),
        contentPadding = PaddingValues(20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(R.string.passport_title), style = MaterialTheme.typography.headlineLarge)
                Text(stringResource(R.string.passport_sub), style = MaterialTheme.typography.bodyMedium, color = Cine.Muted)
            }
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Color(0xFF13233B)).border(1.dp, Color(0xFF23395C), RoundedCornerShape(22.dp)).padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(width = 60.dp, height = 72.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFF1E3352)), contentAlignment = Alignment.Center) {
                    CineIconView(CineIcon.Passport, Cine.BlueSoft, Modifier.size(32.dp))
                }
                Spacer(Modifier.width(14.dp))
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(state.profile.name.ifBlank { stringResource(R.string.you) }, style = MaterialTheme.typography.titleMedium)
                    EnglishText(stringResource(R.string.traveller), style = EnglishTextStyles.small, color = Cine.BlueSoft)
                    Text(
                        stringResource(R.string.passport_stats, earned, state.progress.notebook.size, state.commitmentDays),
                        style = MaterialTheme.typography.labelMedium,
                        color = Cine.Muted,
                    )
                }
            }
        }
        items(state.stamps.size) { i ->
            val stamp = state.stamps[i]
            val clickable = stamp.kind == StampKind.MISSION && stamp.code in unlockedMissions
            StampTile(stamp, i, clickable) { openMission = stamp.code }
        }
    }

    val mission = Script.chapters.map { it.mission }.firstOrNull { it.stampCode == openMission }
    if (mission != null) {
        ModalBottomSheet(onDismissRequest = { openMission = null }, containerColor = Cine.Panel) {
            MissionSheetContent(
                mission = mission,
                missionState = state.progress.missions[mission.id],
                onAccept = { viewModel.acceptMission(mission) },
                onComplete = { note -> viewModel.completeMission(mission, note); openMission = null },
            )
        }
    }
}

@Composable
private fun StampTile(stamp: Stamp, index: Int, clickable: Boolean, onClick: () -> Unit) {
    Column(
        Modifier
            .height(160.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Cine.Panel)
            .border(1.dp, if (clickable && !stamp.earned) Cine.Amber else Cine.PanelBorder, RoundedCornerShape(18.dp))
            .clickable(enabled = clickable, role = Role.Button, onClick = onClick)
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        PassportStamp(stamp.code, stamp.en, stamp.earned, index, tilt = if (index % 2 == 0) -6f else 5f)
        Spacer(Modifier.height(8.dp))
        Text(
            stamp.labelAr,
            style = MaterialTheme.typography.labelMedium,
            color = if (stamp.earned) Cine.Text else Cine.Faint,
            textAlign = TextAlign.Center,
            maxLines = 2,
        )
    }
}

/** Mission details: what to do, phrases to use, accept → done with an optional note. */
@Composable
fun MissionSheetContent(mission: Mission, missionState: MissionState?, onAccept: () -> Unit, onComplete: (String) -> Unit) {
    var note by rememberSaveable { mutableStateOf("") }
    Column(
        Modifier.fillMaxWidth().navigationBarsPadding().verticalScroll(rememberScrollState()).padding(horizontal = 22.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(stringResource(R.string.mission_real_life), style = MaterialTheme.typography.labelMedium, color = Cine.Amber)
        Text(mission.titleAr, style = MaterialTheme.typography.headlineMedium)
        Text(mission.descriptionAr, style = MaterialTheme.typography.bodyLarge, color = Cine.TextSoft)
        Text(stringResource(R.string.mission_use), style = MaterialTheme.typography.labelMedium, color = Cine.Muted)
        mission.phrases.forEach {
            EnglishText(it, style = EnglishTextStyles.option, color = Cine.Text, modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Cine.Ink).padding(horizontal = 12.dp, vertical = 9.dp))
        }
        when {
            missionState?.completedDay != null -> Row(verticalAlignment = Alignment.CenterVertically) {
                CineIconView(CineIcon.Check, Cine.BlueSoft, Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.mission_done), style = MaterialTheme.typography.titleMedium, color = Cine.BlueSoft)
            }
            missionState?.acceptedDay != null -> {
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it.take(280) },
                    placeholder = { Text(stringResource(R.string.mission_note_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Cine.Amber, unfocusedBorderColor = Cine.Line, cursorColor = Cine.Amber),
                )
                CineButton(stringResource(R.string.mission_complete), onClick = { onComplete(note) }, color = Cine.Blue)
            }
            else -> CineButton(stringResource(R.string.mission_accept), onClick = onAccept)
        }
        Spacer(Modifier.height(12.dp))
    }
}

/** End of a chapter: the stamp lands in the passport and the real-life mission is offered. */
@Composable
fun ChapterEndScreen(chapter: Chapter, missionState: MissionState?, onAccept: () -> Unit, onDone: () -> Unit) {
    Column(
        Modifier.fillMaxSize().background(Cine.Ink).statusBarsPadding().navigationBarsPadding().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(Modifier.height(16.dp))
        Text(stringResource(R.string.chapter_end_kicker, chapter.number), style = MaterialTheme.typography.labelMedium, color = Cine.Muted)
        Text(chapter.titleAr, style = MaterialTheme.typography.displayMedium, textAlign = TextAlign.Center)
        PassportStamp(chapter.code, chapter.stampEn, earned = true, colorIndex = chapter.number, size = 180.dp, tilt = -8f)
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).border(1.dp, Cine.Amber, RoundedCornerShape(22.dp)).padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(stringResource(R.string.mission_real_life), style = MaterialTheme.typography.labelMedium, color = Cine.Amber)
            Text(chapter.mission.titleAr, style = MaterialTheme.typography.titleLarge)
            Text(chapter.mission.descriptionAr, style = MaterialTheme.typography.bodyMedium, color = Cine.Muted)
            chapter.mission.phrases.forEach {
                EnglishText(it, style = EnglishTextStyles.option, color = Cine.Text, modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Cine.Panel).padding(horizontal = 12.dp, vertical = 8.dp))
            }
        }
        if (missionState?.acceptedDay != null) {
            Text(stringResource(R.string.mission_accepted), style = MaterialTheme.typography.bodyMedium, color = Cine.BlueSoft, textAlign = TextAlign.Center)
        }
        Spacer(Modifier.height(8.dp))
        if (missionState?.acceptedDay == null) CineButton(stringResource(R.string.mission_accept), onClick = onAccept)
        CineOutlinedButton(stringResource(R.string.back_to_journey), onClick = onDone)
    }
}
