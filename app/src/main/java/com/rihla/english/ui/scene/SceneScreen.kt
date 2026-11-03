package com.rihla.english.ui.scene

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rihla.english.R
import com.rihla.english.core.journey.Beat
import com.rihla.english.core.journey.Naturalness
import com.rihla.english.core.journey.ReplyOption
import com.rihla.english.ui.components.AudioPills
import com.rihla.english.ui.components.CineButton
import com.rihla.english.ui.components.CineIcon
import com.rihla.english.ui.components.CineIconView
import com.rihla.english.ui.components.ControlPill
import com.rihla.english.ui.components.EnglishText
import com.rihla.english.ui.components.LocalSpeechPlayer
import com.rihla.english.ui.components.Ltr
import com.rihla.english.ui.components.SceneBackdrop
import com.rihla.english.ui.theme.Cine
import com.rihla.english.ui.theme.EnglishTextStyles

/** The cinematic scene player: film still on top, subtitles and interaction below. */
@Composable
fun SceneScreen(
    viewModel: SceneViewModel,
    onExit: () -> Unit,
    onOpenScene: (String) -> Unit,
    chapterEnd: @Composable (onDone: () -> Unit) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val s = state ?: run {
        LaunchedEffect(Unit) { onExit() }
        return
    }
    BackHandler(onBack = onExit)

    val summary = s.summary
    if (summary != null) {
        if (s.chapterCompleted) chapterEnd(onExit)
        else SceneEnd(s, summary, onNext = { s.nextSceneId?.let(onOpenScene) ?: onExit() }, onExit = onExit)
        return
    }

    val play = s.play
    val player = LocalSpeechPlayer.current
    // Each new spoken line or shadow phrase is read aloud automatically.
    LaunchedEffect(play.beatIndex) {
        when (val b = play.beat) {
            is Beat.Line -> player.speak(b.english)
            is Beat.Shadow -> player.speak(b.english)
            else -> Unit
        }
    }

    Column(Modifier.fillMaxSize().background(Cine.Ink)) {
        FilmStill(s, onExit)
        AnimatedContent(
            targetState = play.beatIndex to (play.picked != null),
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            modifier = Modifier.weight(1f),
            label = "beat",
        ) { _ ->
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 22.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                when (val beat = play.beat) {
                    is Beat.Narration -> NarrationBeat(beat)
                    is Beat.Line -> LineBeat(beat, s.chapter.cast[beat.speaker].orEmpty(), saved = beat.chunk?.id in play.savedChunkIds, onSave = viewModel::saveChunk)
                    is Beat.Reply -> ReplyBeat(beat, play.picked, onChoose = viewModel::choose)
                    is Beat.Shadow -> ShadowPanel(beat, key = play.beatIndex, onSkip = viewModel::skipShadow)
                    null -> Unit
                }
            }
        }
        if (play.canContinue) {
            CineButton(
                text = stringResource(if (play.beat is Beat.Reply) R.string.continue_label else R.string.next),
                onClick = viewModel::next,
                modifier = Modifier.navigationBarsPadding().padding(horizontal = 22.dp, vertical = 14.dp),
            )
        }
    }
}

@Composable
private fun FilmStill(s: SceneUiState, onExit: () -> Unit) {
    Box(Modifier.fillMaxWidth().height(300.dp)) {
        SceneBackdrop(s.scene.art, Modifier.fillMaxSize())
        Box(Modifier.fillMaxWidth().height(26.dp).background(Cine.Letterbox))
        Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(16.dp).background(Cine.Letterbox))
        Row(
            Modifier.statusBarsPadding().padding(horizontal = 14.dp, vertical = 10.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val close = stringResource(R.string.close)
            IconButton(
                onClick = onExit,
                modifier = Modifier.clip(CircleShape).background(Cine.Ink.copy(alpha = 0.7f)).semantics { contentDescription = close },
            ) { CineIconView(CineIcon.Close, Cine.Text, Modifier.size(20.dp)) }
            Spacer(Modifier.weight(1f))
            Text(
                stringResource(R.string.chapter_scene_progress, s.chapter.number, s.scene.index + 1, s.chapter.scenes.size),
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(Cine.Ink.copy(alpha = 0.7f)).padding(horizontal = 12.dp, vertical = 6.dp),
            )
        }
        LinearProgressIndicator(
            progress = { s.play.progress },
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp).fillMaxWidth().height(3.dp),
            color = Cine.Amber,
            trackColor = Color.Transparent,
        )
    }
}

@Composable
private fun NarrationBeat(beat: Beat.Narration) {
    Text(stringResource(R.string.narrator), style = MaterialTheme.typography.labelMedium, color = Cine.Muted)
    Text(beat.arabic, style = MaterialTheme.typography.titleLarge.copy(lineHeight = MaterialTheme.typography.headlineMedium.lineHeight), color = Cine.Text)
}

@Composable
private fun SpeakerLabel(name: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(Cine.Amber))
        Spacer(Modifier.width(8.dp))
        Text(name, style = MaterialTheme.typography.labelMedium, color = Cine.Muted)
    }
}

@Composable
private fun LineBeat(beat: Beat.Line, speaker: String, saved: Boolean, onSave: () -> Unit) {
    SpeakerLabel(speaker)
    val chunk = beat.chunk
    val annotated = buildAnnotatedString {
        if (chunk == null) append(beat.english) else {
            val start = beat.english.indexOf(chunk.english)
            append(beat.english.substring(0, start))
            withStyle(SpanStyle(color = Cine.Amber, textDecoration = TextDecoration.Underline)) { append(chunk.english) }
            append(beat.english.substring(start + chunk.english.length))
        }
    }
    Ltr {
        Text(annotated, style = EnglishTextStyles.subtitle.copy(textDirection = TextDirection.Ltr, color = Cine.Text), modifier = Modifier.fillMaxWidth())
    }
    Text(beat.arabic, style = MaterialTheme.typography.bodyLarge, color = Cine.Muted)
    if (chunk != null) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            EnglishText(chunk.english, style = EnglishTextStyles.small, color = Cine.AmberSoft)
            Text("  =  ${chunk.arabic}", style = MaterialTheme.typography.bodyMedium, color = Cine.TextSoft)
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        AudioPills(beat.english)
        if (chunk != null) {
            ControlPill(stringResource(if (saved) R.string.saved else R.string.save), onClick = onSave, icon = CineIcon.Bookmark, active = saved)
        }
    }
}

@Composable
private fun ReplyBeat(beat: Beat.Reply, picked: ReplyOption?, onChoose: (Int) -> Unit) {
    Text(beat.promptAr, style = MaterialTheme.typography.bodyLarge, color = Cine.TextSoft)
    Text(stringResource(R.string.your_reply), style = MaterialTheme.typography.titleLarge)
    beat.options.forEachIndexed { i, option ->
        val selected = picked == option
        val good = option.naturalness != Naturalness.UNNATURAL
        OutlinedButton(
            onClick = { onChoose(i) },
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, if (!selected) Cine.Line else if (good) Cine.Blue else Cine.Amber),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = if (!selected) Cine.Panel else if (good) Cine.BlueTint else Cine.AmberTint,
                contentColor = Cine.Text,
            ),
        ) {
            EnglishText(option.english, style = EnglishTextStyles.option, color = Cine.Text, modifier = Modifier.fillMaxWidth())
        }
    }
    AnimatedVisibility(picked != null) {
        picked?.let { ReplyFeedback(it) }
    }
}

@Composable
private fun ReplyFeedback(option: ReplyOption) {
    val (title, color, bg) = when (option.naturalness) {
        Naturalness.NATURAL -> Triple(R.string.reply_natural, Cine.BlueSoft, Cine.BlueTint)
        Naturalness.OK -> Triple(R.string.reply_ok, Cine.BlueSoft, Cine.BlueTint)
        Naturalness.UNNATURAL -> Triple(R.string.reply_unnatural, Cine.AmberSoft, Cine.AmberTint)
    }
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(bg).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(stringResource(title), style = MaterialTheme.typography.titleMedium, color = color)
        Text(option.arabic, style = MaterialTheme.typography.bodyMedium, color = Cine.Muted)
        Text(option.noteAr, style = MaterialTheme.typography.bodyMedium, color = Cine.Text)
        option.pattern?.let {
            Text(stringResource(R.string.arab_trap, it.titleAr), style = MaterialTheme.typography.labelMedium, color = Cine.AmberSoft)
        }
        if (option.naturalness == Naturalness.UNNATURAL) {
            Text(stringResource(R.string.reply_try_again), style = MaterialTheme.typography.labelLarge, color = Cine.AmberSoft)
        }
    }
}

@Composable
private fun SceneEnd(s: SceneUiState, summary: com.rihla.english.core.journey.SceneSummary, onNext: () -> Unit, onExit: () -> Unit) {
    Column(
        Modifier.fillMaxSize().background(Cine.Ink).statusBarsPadding().navigationBarsPadding().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Spacer(Modifier.height(30.dp))
        Box(Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(24.dp))) {
            SceneBackdrop(s.scene.art, Modifier.fillMaxSize())
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Cine.Ink.copy(alpha = 0.85f)))))
            Text(s.scene.titleAr, style = MaterialTheme.typography.titleLarge, modifier = Modifier.align(Alignment.BottomStart).padding(16.dp))
        }
        Text(stringResource(R.string.scene_end_title), style = MaterialTheme.typography.headlineLarge, textAlign = TextAlign.Center)
        StatsRow(summary)
        Spacer(Modifier.weight(1f))
        if (s.nextSceneId != null) CineButton(stringResource(R.string.scene_end_next), onClick = onNext)
        com.rihla.english.ui.components.CineOutlinedButton(stringResource(R.string.back_to_journey), onClick = onExit)
    }
}

@Composable
fun StatsRow(summary: com.rihla.english.core.journey.SceneSummary) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        listOf(
            summary.newPhrases to R.string.stat_phrases,
            summary.naturalReplies to R.string.stat_natural,
            summary.shadows to R.string.stat_shadows,
        ).forEach { (n, label) ->
            Column(
                Modifier.weight(1f).clip(RoundedCornerShape(16.dp)).background(Cine.Panel).padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                EnglishText("$n", style = EnglishTextStyles.hero.copy(fontSize = MaterialTheme.typography.headlineMedium.fontSize), color = Cine.Text)
                Text(stringResource(label), style = MaterialTheme.typography.labelMedium, color = Cine.Muted)
            }
        }
    }
}

