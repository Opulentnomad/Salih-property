package com.rihla.english.ui.lesson.exercises

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rihla.english.R
import com.rihla.english.core.engine.UserAnswer
import com.rihla.english.core.model.MatchMode
import com.rihla.english.core.model.MatchPairs
import com.rihla.english.ui.components.EnglishText
import com.rihla.english.ui.components.LocalSpeechPlayer
import com.rihla.english.ui.components.PressableSurface
import com.rihla.english.ui.theme.EnglishTextStyles
import com.rihla.english.ui.theme.RihlaTheme
import kotlinx.coroutines.delay
import kotlin.random.Random

private enum class TileState { IDLE, SELECTED, MATCHED, WRONG }

/**
 * MATCH_PAIRS: two columns, tap one tile on each side. In the RTL layout the Arabic column sits
 * on the right and the English (or audio) column on the left. Submits automatically when done.
 */
@Composable
fun MatchPairsView(
    exercise: MatchPairs,
    presentation: Int,
    enabled: Boolean,
    onSubmit: (UserAnswer) -> Unit,
    modifier: Modifier = Modifier,
) {
    val player = LocalSpeechPlayer.current
    val haptics = LocalHapticFeedback.current
    val seed = exercise.id.hashCode() + presentation
    val english = remember(exercise.id, presentation) { exercise.pairs.map { it.english }.shuffled(Random(seed)) }
    val others = remember(exercise.id, presentation) { exercise.pairs.map { it.other }.shuffled(Random(seed * 31)) }
    var selectedEnglish by remember { mutableStateOf<Int?>(null) }
    var selectedOther by remember { mutableStateOf<Int?>(null) }
    val matchedEnglish = remember { mutableStateListOf<Int>() }
    val matchedOther = remember { mutableStateListOf<Int>() }
    var wrongPair by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var mistakes by remember { mutableIntStateOf(0) }

    fun tryMatch() {
        val e = selectedEnglish ?: return
        val o = selectedOther ?: return
        val isPair = exercise.pairs.any { it.english == english[e] && it.other == others[o] }
        if (isPair) {
            matchedEnglish += e
            matchedOther += o
        } else {
            mistakes++
            wrongPair = e to o
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        }
        selectedEnglish = null
        selectedOther = null
    }

    LaunchedEffect(wrongPair) {
        if (wrongPair != null) {
            delay(650)
            wrongPair = null
        }
    }
    LaunchedEffect(matchedEnglish.size) {
        if (matchedEnglish.size == exercise.pairs.size) {
            delay(350)
            onSubmit(UserAnswer.MatchCompleted(mistakes))
        }
    }

    val title = when (exercise.mode) {
        MatchMode.ENGLISH_TO_ARABIC -> R.string.exercise_match
        MatchMode.AUDIO_TO_ARABIC -> R.string.exercise_match_audio
        MatchMode.UPPER_TO_LOWER -> R.string.exercise_match_letters
    }

    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        ExerciseTitle(stringResource(title))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            // First child = right side in RTL: Arabic (or lowercase letters).
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                others.forEachIndexed { i, text ->
                    val state = when {
                        i in matchedOther -> TileState.MATCHED
                        wrongPair?.second == i -> TileState.WRONG
                        selectedOther == i -> TileState.SELECTED
                        else -> TileState.IDLE
                    }
                    MatchTile(state = state, enabled = enabled && state != TileState.MATCHED, onClick = {
                        selectedOther = i
                        tryMatch()
                    }) {
                        if (exercise.mode == MatchMode.UPPER_TO_LOWER) {
                            EnglishText(text, style = EnglishTextStyles.large, textAlign = TextAlign.Center)
                        } else {
                            Text(text, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
                        }
                    }
                }
            }
            // Second child = left side in RTL: English words, letters, or audio clips.
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                english.forEachIndexed { i, text ->
                    val state = when {
                        i in matchedEnglish -> TileState.MATCHED
                        wrongPair?.first == i -> TileState.WRONG
                        selectedEnglish == i -> TileState.SELECTED
                        else -> TileState.IDLE
                    }
                    MatchTile(state = state, enabled = enabled && state != TileState.MATCHED, onClick = {
                        selectedEnglish = i
                        player.speak(text)
                        tryMatch()
                    }) {
                        if (exercise.mode == MatchMode.AUDIO_TO_ARABIC) {
                            Text("🔊 ${i + 1}", fontSize = 22.sp, textAlign = TextAlign.Center)
                        } else {
                            EnglishText(text, style = EnglishTextStyles.chip, textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MatchTile(
    state: TileState,
    enabled: Boolean,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    val colors = RihlaTheme.colors
    val face by animateColorAsState(
        when (state) {
            TileState.SELECTED -> colors.selectedBackground
            TileState.WRONG -> colors.wrongBackground
            TileState.MATCHED -> colors.correctBackground
            TileState.IDLE -> colors.cardBackground
        },
        label = "tile",
    )
    val edge = when (state) {
        TileState.SELECTED -> colors.selectedBorder
        TileState.WRONG -> colors.wrongText
        TileState.MATCHED -> colors.correctText
        TileState.IDLE -> colors.border
    }
    PressableSurface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (state == TileState.MATCHED) 0.55f else 1f),
        color = face,
        shadowColor = edge,
        border = BorderStroke(2.dp, edge),
        enabled = enabled,
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center,
        ) { content() }
    }
}
