package com.rihla.english.ui.lesson.exercises

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.rihla.english.R
import com.rihla.english.core.engine.UserAnswer
import com.rihla.english.core.model.ListeningChallenge
import com.rihla.english.core.model.ListeningMode
import com.rihla.english.ui.components.AudioButtons
import com.rihla.english.ui.components.EnglishText
import com.rihla.english.ui.components.LocalSpeechPlayer
import com.rihla.english.ui.theme.EnglishTextStyles
import com.rihla.english.ui.theme.RihlaTheme
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * LISTENING_CHALLENGE: auto-plays English TTS. SELECT mode doubles as the minimal-pair phonics
 * drill (pear/bear, fan/van); ASSEMBLE mode builds the heard sentence from chips.
 */
@Composable
fun ListeningView(
    exercise: ListeningChallenge,
    presentation: Int,
    enabled: Boolean,
    onAnswerChanged: (UserAnswer?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val player = LocalSpeechPlayer.current
    LaunchedEffect(exercise.id, presentation) {
        delay(350)
        player.speak(exercise.audioText)
    }

    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        ExerciseTitle(
            stringResource(
                if (exercise.mode == ListeningMode.SELECT) R.string.exercise_listen_select else R.string.exercise_listen_assemble,
            ),
        )
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            AudioButtons(exercise.audioText, size = 96.dp)
        }
        when (exercise.mode) {
            ListeningMode.SELECT -> {
                var selected by remember { mutableStateOf<Int?>(null) }
                exercise.options.forEachIndexed { index, option ->
                    ChoiceCard(
                        number = index + 1,
                        selected = selected == index,
                        enabled = enabled,
                        onClick = {
                            selected = index
                            onAnswerChanged(UserAnswer.Choice(index))
                        },
                    ) {
                        EnglishText(option, style = EnglishTextStyles.medium, color = RihlaTheme.colors.textPrimary)
                    }
                }
            }
            ListeningMode.ASSEMBLE -> {
                val bank = remember(exercise.id, presentation) {
                    (exercise.answerTokens + exercise.distractors).shuffled(Random(exercise.id.hashCode() + presentation))
                }
                // Chips are not read aloud here: that would give away the answer.
                TokenAssembler(bank = bank, enabled = enabled, onAnswerChanged = onAnswerChanged, speakOnTap = false)
            }
        }
    }
}
