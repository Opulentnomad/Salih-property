package com.rihla.english.ui.lesson.exercises

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.rihla.english.core.engine.UserAnswer
import com.rihla.english.ui.components.ArabicText
import com.rihla.english.ui.components.LocalSpeechPlayer
import com.rihla.english.ui.components.Ltr
import com.rihla.english.ui.components.PressableSurface
import com.rihla.english.ui.components.WordChip
import com.rihla.english.ui.theme.Gold
import com.rihla.english.ui.theme.RihlaTheme

/** Arabic instruction heading at the top of every exercise. */
@Composable
fun ExerciseTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineMedium,
        color = RihlaTheme.colors.textPrimary,
        modifier = modifier.fillMaxWidth(),
    )
}

/** Yellow 💡 tip box with Arabic text. */
@Composable
fun TipBox(text: String, modifier: Modifier = Modifier) {
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Gold.copy(alpha = 0.16f))
            .border(2.dp, Gold.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text("💡", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.width(8.dp))
        ArabicText(text, style = MaterialTheme.typography.bodyLarge, color = RihlaTheme.colors.textPrimary)
    }
}

/** Selectable answer card with a number badge, Duolingo-style. */
@Composable
fun ChoiceCard(
    number: Int,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val colors = RihlaTheme.colors
    PressableSurface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        color = if (selected) colors.selectedBackground else colors.cardBackground,
        shadowColor = if (selected) colors.selectedBorder else colors.border,
        border = BorderStroke(2.dp, if (selected) colors.selectedBorder else colors.border),
        enabled = enabled,
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 60.dp)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(30.dp)
                    .border(2.dp, if (selected) colors.selectedBorder else colors.border, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(number.toString(), style = MaterialTheme.typography.labelMedium, color = colors.textSecondary)
            }
            Spacer(Modifier.width(14.dp))
            Box(Modifier.weight(1f)) { content() }
        }
    }
}

/**
 * Chip-based sentence builder shared by WORD_BANK_SENTENCE and LISTENING_CHALLENGE (assemble).
 * The answer line and the bank are both LTR so English word order reads naturally.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TokenAssembler(
    bank: List<String>,
    enabled: Boolean,
    onAnswerChanged: (UserAnswer?) -> Unit,
    modifier: Modifier = Modifier,
    speakOnTap: Boolean = true,
) {
    val player = LocalSpeechPlayer.current
    val selected = remember(bank) { mutableStateListOf<Int>() }
    val colors = RihlaTheme.colors

    fun publish() {
        onAnswerChanged(if (selected.isEmpty()) null else UserAnswer.Tokens(selected.map { bank[it] }))
    }

    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        // Answer line
        Ltr {
            Column {
                FlowRow(
                    Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 60.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    selected.forEach { index ->
                        WordChip(
                            text = bank[index],
                            enabled = enabled,
                            onClick = {
                                selected.remove(index)
                                publish()
                            },
                        )
                    }
                }
                HorizontalDivider(thickness = 2.dp, color = colors.border)
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(thickness = 2.dp, color = colors.border)
            }
        }
        // Word bank
        Ltr {
            FlowRow(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                bank.forEachIndexed { index, word ->
                    WordChip(
                        text = word,
                        placeholder = index in selected,
                        enabled = enabled,
                        onClick = {
                            if (index !in selected) {
                                selected.add(index)
                                if (speakOnTap) player.speak(word)
                                publish()
                            }
                        },
                    )
                }
            }
        }
    }
}
