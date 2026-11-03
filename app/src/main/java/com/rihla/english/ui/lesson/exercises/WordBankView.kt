package com.rihla.english.ui.lesson.exercises

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.rihla.english.R
import com.rihla.english.core.engine.UserAnswer
import com.rihla.english.core.model.WordBankSentence
import com.rihla.english.ui.components.GrammarBridgeCard
import com.rihla.english.ui.components.MascotBubble
import com.rihla.english.ui.theme.Purple
import com.rihla.english.ui.theme.RihlaTheme
import kotlin.random.Random

/** WORD_BANK_SENTENCE: translate the Arabic sentence by tapping English chips in order. */
@Composable
fun WordBankView(
    exercise: WordBankSentence,
    presentation: Int,
    enabled: Boolean,
    onAnswerChanged: (UserAnswer?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val bank = remember(exercise.id, presentation) {
        (exercise.answerTokens + exercise.distractors).shuffled(Random(exercise.id.hashCode() + presentation))
    }
    var showBridge by remember { mutableStateOf(false) }

    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ExerciseTitle(stringResource(R.string.exercise_translate))
        MascotBubble {
            Text(exercise.arabicSentence, style = MaterialTheme.typography.headlineMedium, color = RihlaTheme.colors.textPrimary)
        }
        exercise.grammarBridge?.let { bridge ->
            TextButton(onClick = { showBridge = !showBridge }) {
                Text(stringResource(R.string.show_grammar_tip), color = Purple, style = MaterialTheme.typography.labelLarge)
            }
            AnimatedVisibility(showBridge) { GrammarBridgeCard(bridge) }
        }
        TokenAssembler(bank = bank, enabled = enabled, onAnswerChanged = onAnswerChanged)
    }
}
