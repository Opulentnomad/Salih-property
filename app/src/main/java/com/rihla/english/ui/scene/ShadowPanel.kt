package com.rihla.english.ui.scene

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.rihla.english.R
import com.rihla.english.audio.RecognitionEvent
import com.rihla.english.core.engine.AnswerMatcher
import com.rihla.english.core.journey.Beat
import com.rihla.english.ui.components.AudioPills
import com.rihla.english.ui.components.CineIcon
import com.rihla.english.ui.components.CineIconView
import com.rihla.english.ui.components.EnglishText
import com.rihla.english.ui.components.LocalSpeechRecognizer
import com.rihla.english.ui.components.Ltr
import com.rihla.english.ui.theme.Cine
import com.rihla.english.ui.theme.EnglishTextStyles
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.abs

private sealed interface MicState {
    data object Idle : MicState
    data object Listening : MicState
    data class Scored(val percent: Int, val heard: String) : MicState
    data class Problem(val res: Int) : MicState
}

/**
 * Shadowing ("كرّر بعدي"): the native line plays, the learner repeats it. While they speak, the
 * real microphone level is drawn as a waveform under the reference rhythm; the recogniser's
 * transcript is compared with the phrase for a pronunciation match score.
 */
@Composable
fun ShadowPanel(beat: Beat.Shadow, key: Int, onSkip: () -> Unit) {
    val context = LocalContext.current
    val recognizer = LocalSpeechRecognizer.current
    val available = remember(recognizer) { recognizer?.isAvailable() == true }
    val scope = rememberCoroutineScope()
    var mic by remember(key) { mutableStateOf<MicState>(MicState.Idle) }
    val levels = remember(key) { mutableStateListOf<Float>() }
    var job by remember { mutableStateOf<Job?>(null) }
    DisposableEffect(key) { onDispose { job?.cancel() } }

    fun listen() {
        val manager = recognizer ?: return
        job?.cancel()
        levels.clear()
        mic = MicState.Listening
        job = scope.launch {
            manager.listen().collect { e ->
                when (e) {
                    is RecognitionEvent.Volume -> if (levels.size < BARS) levels += ((e.rmsDb + 2f) / 12f).coerceIn(0.08f, 1f)
                    is RecognitionEvent.Results -> {
                        val score = AnswerMatcher.pronunciationScore(e.candidates, listOf(beat.english))
                        mic = if (e.candidates.isEmpty()) MicState.Problem(R.string.shadow_error)
                        else MicState.Scored((score * 100).toInt(), e.candidates.first())
                    }
                    is RecognitionEvent.Error -> mic = MicState.Problem(R.string.shadow_error)
                    else -> Unit
                }
            }
        }
    }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) listen() else mic = MicState.Problem(R.string.shadow_permission)
    }

    Text(stringResource(R.string.shadow_title), style = MaterialTheme.typography.titleLarge)
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Cine.Panel).border(1.dp, Cine.PanelBorder, RoundedCornerShape(22.dp)).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(stringResource(R.string.shadow_ready), style = MaterialTheme.typography.labelMedium, color = Cine.Amber)
        EnglishText(beat.english, style = EnglishTextStyles.hero, color = Cine.Text)
        Text(beat.arabic, style = MaterialTheme.typography.bodyLarge, color = Cine.Muted)
        Text(stringResource(R.string.shadow_pronunciation, beat.phonetic), style = MaterialTheme.typography.bodyLarge, color = Cine.TextSoft)
        Text(beat.tipAr, style = MaterialTheme.typography.bodyMedium, color = Cine.AmberSoft)
    }
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { AudioPills(beat.english) }

    Waveform(stringResource(R.string.shadow_native), referenceBars(beat.english), Cine.Amber)
    Waveform(stringResource(R.string.shadow_yours), levels.toList().ifEmpty { List(BARS) { 0.08f } }, if (levels.isEmpty()) Cine.Line else Cine.Blue)

    when (val m = mic) {
        is MicState.Scored -> Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Cine.BlueTint).padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(stringResource(R.string.shadow_match, m.percent), style = MaterialTheme.typography.titleMedium, color = Cine.BlueSoft)
            EnglishText("“${m.heard}”", style = EnglishTextStyles.small, color = Cine.TextSoft)
        }
        is MicState.Problem -> Text(stringResource(m.res), style = MaterialTheme.typography.bodyMedium, color = Cine.AmberSoft)
        else -> Unit
    }

    if (available) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val label = stringResource(R.string.shadow_record)
            OutlinedButton(
                onClick = {
                    val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                    if (granted) listen() else permission.launch(Manifest.permission.RECORD_AUDIO)
                },
                enabled = mic != MicState.Listening,
                modifier = Modifier.size(88.dp).semantics { contentDescription = label },
                shape = CircleShape,
                border = BorderStroke(5.dp, if (mic is MicState.Scored) Cine.Blue else Cine.AmberTint),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Cine.Amber, disabledContainerColor = Cine.AmberSoft),
            ) { CineIconView(CineIcon.Mic, Cine.Ink, Modifier.size(34.dp)) }
            Text(
                stringResource(
                    when (mic) {
                        MicState.Listening -> R.string.shadow_hint_listening
                        is MicState.Scored -> R.string.shadow_retry
                        else -> R.string.shadow_hint_idle
                    },
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = Cine.Muted,
                textAlign = TextAlign.Center,
            )
        }
    } else {
        Text(stringResource(R.string.shadow_unavailable), style = MaterialTheme.typography.bodyMedium, color = Cine.Muted)
    }
    TextButton(onClick = { job?.cancel(); onSkip() }, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.shadow_skip), style = MaterialTheme.typography.labelLarge, color = Cine.Muted)
    }
}

private const val BARS = 40

/** A deterministic reference "rhythm" shape from the phrase's syllable-ish structure. */
private fun referenceBars(text: String): List<Float> {
    val letters = text.filter { it.isLetter() || it == ' ' }.ifEmpty { "a" }
    return List(BARS) { i ->
        val c = letters[(i * letters.length) / BARS]
        val base = if (c == ' ') 0.15f else if (c.lowercaseChar() in "aeiouy") 0.95f else 0.55f
        (base - abs((i % 7) - 3) * 0.06f).coerceIn(0.1f, 1f)
    }
}

@Composable
private fun Waveform(label: String, bars: List<Float>, color: androidx.compose.ui.graphics.Color) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = Cine.Muted)
        Ltr {
            Row(Modifier.fillMaxWidth().height(52.dp), horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically) {
                bars.take(BARS).forEach { h ->
                    Box(Modifier.width(5.dp).height((52 * h).dp).clip(RoundedCornerShape(3.dp)).background(color))
                }
            }
        }
    }
}
