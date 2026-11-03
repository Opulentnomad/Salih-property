package com.rihla.english.ui.lesson.exercises

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.rihla.english.R
import com.rihla.english.audio.RecognitionEvent
import com.rihla.english.core.engine.UserAnswer
import com.rihla.english.core.model.SpeakingChallenge
import com.rihla.english.ui.components.AudioButtons
import com.rihla.english.ui.components.EnglishText
import com.rihla.english.ui.components.LocalSpeechRecognizer
import com.rihla.english.ui.components.PressableSurface
import com.rihla.english.ui.theme.Blue
import com.rihla.english.ui.theme.BlueDark
import com.rihla.english.ui.theme.EnglishTextStyles
import com.rihla.english.ui.theme.RihlaTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private sealed interface MicState {
    data object Idle : MicState
    data class Listening(val partial: String) : MicState
    data class Problem(val messageRes: Int) : MicState
}

/**
 * SPEAKING_CHALLENGE: the learner says the phrase; Android SpeechRecognizer transcribes it and the
 * grader fuzzy-matches it. "Can't speak right now" skips speaking for the rest of the lesson.
 */
@Composable
fun SpeakingView(
    exercise: SpeakingChallenge,
    enabled: Boolean,
    onSubmit: (UserAnswer) -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val recognizer = LocalSpeechRecognizer.current
    val available = remember(recognizer) { recognizer?.isAvailable() == true }
    val scope = rememberCoroutineScope()
    var mic by remember { mutableStateOf<MicState>(MicState.Idle) }
    var level by remember { mutableFloatStateOf(0f) }
    var job by remember { mutableStateOf<Job?>(null) }
    val colors = RihlaTheme.colors

    DisposableEffect(Unit) { onDispose { job?.cancel() } }

    fun startListening() {
        val manager = recognizer ?: return
        job?.cancel()
        mic = MicState.Listening("")
        job = scope.launch {
            manager.listen().collect { event ->
                when (event) {
                    RecognitionEvent.Ready -> Unit
                    is RecognitionEvent.Volume -> level = ((event.rmsDb + 2f) / 12f).coerceIn(0f, 1f)
                    is RecognitionEvent.Partial -> mic = MicState.Listening(event.text)
                    is RecognitionEvent.Results -> {
                        mic = MicState.Idle
                        if (event.candidates.isEmpty()) mic = MicState.Problem(R.string.speak_error)
                        else onSubmit(UserAnswer.Speech(event.candidates))
                    }
                    is RecognitionEvent.Error -> mic = MicState.Problem(R.string.speak_error)
                }
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) startListening() else mic = MicState.Problem(R.string.speak_permission_denied)
    }

    fun onMicPressed() {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        if (granted) startListening() else permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    Column(
        modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ExerciseTitle(stringResource(R.string.exercise_speak))
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .border(2.dp, colors.border, RoundedCornerShape(24.dp))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            EnglishText(exercise.targetText, style = EnglishTextStyles.large, color = colors.textPrimary, textAlign = TextAlign.Center)
            Text(exercise.arabic, style = MaterialTheme.typography.titleLarge, color = colors.textPrimary)
            Text(stringResource(R.string.exercise_pronunciation, exercise.arabicPhonetic), style = MaterialTheme.typography.titleMedium, color = colors.textSecondary)
            AudioButtons(exercise.targetText, size = 52.dp)
        }
        TipBox(exercise.tipAr)

        if (!available) {
            Text(stringResource(R.string.speak_unavailable), style = MaterialTheme.typography.bodyLarge, color = colors.wrongText, textAlign = TextAlign.Center)
        } else {
            MicButton(listening = mic is MicState.Listening, level = level, enabled = enabled, onClick = ::onMicPressed)
            val status = when (val m = mic) {
                MicState.Idle -> stringResource(R.string.speak_tap_to_talk)
                is MicState.Listening -> if (m.partial.isBlank()) stringResource(R.string.speak_listening) else stringResource(R.string.speak_heard, m.partial)
                is MicState.Problem -> stringResource(m.messageRes)
            }
            Text(status, style = MaterialTheme.typography.titleMedium, color = if (mic is MicState.Problem) colors.wrongText else colors.textSecondary, textAlign = TextAlign.Center)
        }

        TextButton(onClick = { job?.cancel(); onSkip() }, enabled = enabled) {
            Text(stringResource(R.string.speak_skip), style = MaterialTheme.typography.labelLarge, color = Blue)
        }
    }
}

@Composable
private fun MicButton(listening: Boolean, level: Float, enabled: Boolean, onClick: () -> Unit) {
    val pulse = rememberInfiniteTransition(label = "mic")
    val ring by pulse.animateFloat(1f, 1.25f, infiniteRepeatable(tween(700), RepeatMode.Reverse), label = "ring")
    Box(contentAlignment = Alignment.Center) {
        if (listening) {
            Box(
                Modifier
                    .size(120.dp)
                    .graphicsLayer {
                        val s = ring + level * 0.3f
                        scaleX = s
                        scaleY = s
                        alpha = 0.35f
                    }
                    .clip(CircleShape)
                    .background(Blue),
            )
        }
        PressableSurface(
            onClick = onClick,
            modifier = Modifier.width(96.dp),
            color = Blue,
            shadowColor = BlueDark,
            shape = CircleShape,
            enabled = enabled && !listening,
        ) {
            Box(Modifier.size(92.dp), contentAlignment = Alignment.Center) { Text("🎤", fontSize = 40.sp) }
        }
    }
}
