package com.rihla.english.ui.lesson.exercises

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.rihla.english.R
import com.rihla.english.core.engine.UserAnswer
import com.rihla.english.core.model.ErrorCorrection
import com.rihla.english.ui.components.EnglishText
import com.rihla.english.ui.components.Ltr
import com.rihla.english.ui.components.MascotBubble
import com.rihla.english.ui.components.PressableSurface
import com.rihla.english.ui.theme.EnglishTextStyles
import com.rihla.english.ui.theme.RihlaTheme

/**
 * ERROR_CORRECTION — "Spot the mistake": the sentence is shown as LTR word chips; the learner taps
 * the word that's wrong, then picks the correction (or "delete this word").
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ErrorCorrectionView(
    exercise: ErrorCorrection,
    enabled: Boolean,
    onAnswerChanged: (UserAnswer?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = RihlaTheme.colors
    var tapped by remember { mutableStateOf<Int?>(null) }
    var chosen by remember { mutableStateOf<Int?>(null) }

    fun publish() {
        val t = tapped
        val c = chosen
        onAnswerChanged(if (t != null && c != null) UserAnswer.Correction(t, c) else null)
    }

    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ExerciseTitle(stringResource(R.string.exercise_fix))
        MascotBubble(mascot = "🕵️") {
            Text(
                stringResource(R.string.exercise_fix_meaning, exercise.translationAr),
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary,
            )
        }
        Text(stringResource(R.string.exercise_fix_tap), style = MaterialTheme.typography.labelLarge, color = colors.textSecondary)
        Ltr {
            FlowRow(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                exercise.tokens.forEachIndexed { index, token ->
                    val selected = tapped == index
                    PressableSurface(
                        onClick = {
                            tapped = index
                            chosen = null
                            publish()
                        },
                        color = if (selected) colors.wrongBackground else colors.cardBackground,
                        shadowColor = if (selected) colors.wrongText else colors.border,
                        border = BorderStroke(2.dp, if (selected) colors.wrongText else colors.border),
                        shape = RoundedCornerShape(12.dp),
                        depth = 3.dp,
                        enabled = enabled,
                    ) {
                        EnglishText(
                            token,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            style = EnglishTextStyles.chip.copy(textDecoration = if (selected) TextDecoration.LineThrough else null),
                            color = if (selected) colors.wrongText else colors.textPrimary,
                        )
                    }
                }
            }
        }
        AnimatedVisibility(tapped != null) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp), horizontalAlignment = Alignment.Start) {
                Text(stringResource(R.string.exercise_fix_choose), style = MaterialTheme.typography.labelLarge, color = colors.textSecondary)
                exercise.options.forEachIndexed { i, option ->
                    ChoiceCard(
                        number = i + 1,
                        selected = chosen == i,
                        enabled = enabled,
                        onClick = {
                            chosen = i
                            publish()
                        },
                    ) {
                        if (option.isEmpty()) {
                            Text(stringResource(R.string.exercise_fix_delete), style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                        } else {
                            EnglishText(option, style = EnglishTextStyles.medium, color = colors.textPrimary)
                        }
                    }
                }
            }
        }
    }
}
