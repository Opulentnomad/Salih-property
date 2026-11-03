package com.rihla.english.ui.lesson.exercises

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rihla.english.core.engine.UserAnswer
import com.rihla.english.core.model.MultipleChoice
import com.rihla.english.ui.components.AudioButtons
import com.rihla.english.ui.components.EnglishText
import com.rihla.english.ui.components.LocalSpeechPlayer
import com.rihla.english.ui.components.Ltr
import com.rihla.english.ui.theme.EnglishTextStyles
import com.rihla.english.ui.theme.RihlaTheme

/** MULTIPLE_CHOICE: Arabic question, optional English stem (LTR, playable) and 4 options. */
@Composable
fun MultipleChoiceView(
    exercise: MultipleChoice,
    enabled: Boolean,
    onAnswerChanged: (UserAnswer?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val player = LocalSpeechPlayer.current
    var selected by remember { mutableStateOf<Int?>(null) }
    val colors = RihlaTheme.colors

    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        ExerciseTitle(exercise.questionAr)
        exercise.englishPrompt?.let { prompt ->
            Ltr {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    exercise.audioText?.let {
                        AudioButtons(it, size = 52.dp)
                        Spacer(Modifier.width(16.dp))
                    }
                    EnglishText(prompt, style = EnglishTextStyles.large, color = colors.textPrimary)
                }
            }
        }
        exercise.options.forEachIndexed { index, option ->
            ChoiceCard(
                number = index + 1,
                selected = selected == index,
                enabled = enabled,
                onClick = {
                    selected = index
                    if (exercise.optionsAreEnglish) player.speak(option)
                    onAnswerChanged(UserAnswer.Choice(index))
                },
            ) {
                if (exercise.optionsAreEnglish) {
                    EnglishText(option, style = EnglishTextStyles.medium, color = colors.textPrimary)
                } else {
                    Text(option, style = MaterialTheme.typography.titleLarge, color = colors.textPrimary)
                }
            }
        }
    }
}
