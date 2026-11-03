package com.rihla.english.ui.lesson.exercises

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rihla.english.R
import com.rihla.english.core.model.FlashcardAudio
import com.rihla.english.ui.components.AudioButtons
import com.rihla.english.ui.components.EnglishText
import com.rihla.english.ui.components.LocalSpeechPlayer
import com.rihla.english.ui.theme.Blue
import com.rihla.english.ui.theme.EnglishTextStyles
import com.rihla.english.ui.theme.RihlaTheme

/** FLASHCARD_AUDIO: picture, English word (LTR), Arabic meaning, IPA + Arabic-letter phonetics, 🔊/🐢. */
@Composable
fun FlashcardView(card: FlashcardAudio, modifier: Modifier = Modifier) {
    val player = LocalSpeechPlayer.current
    val colors = RihlaTheme.colors
    val speakText = card.letterPair?.let { "${it.first()}. ${card.english}" } ?: card.english
    val pop = remember { Animatable(0.85f) }
    LaunchedEffect(card.id) {
        player.speak(speakText)
        pop.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
    }

    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ExerciseTitle(stringResource(R.string.exercise_new_word))
        Column(
            Modifier
                .fillMaxWidth()
                .graphicsLayer { scaleX = pop.value; scaleY = pop.value }
                .clip(RoundedCornerShape(24.dp))
                .background(colors.cardBackground)
                .border(2.dp, colors.border, RoundedCornerShape(24.dp))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(card.emoji, fontSize = 72.sp)
            card.letterPair?.let {
                EnglishText(it, style = EnglishTextStyles.hero.copy(fontSize = 56.sp), color = Blue, textAlign = TextAlign.Center)
            }
            EnglishText(card.english, style = EnglishTextStyles.hero, color = colors.textPrimary, textAlign = TextAlign.Center)
            EnglishText(card.ipa, style = EnglishTextStyles.chip, color = colors.textSecondary, textAlign = TextAlign.Center)
            Spacer(Modifier.height(4.dp))
            Text(card.arabic, style = MaterialTheme.typography.headlineMedium, color = colors.textPrimary, textAlign = TextAlign.Center)
            Text(
                stringResource(R.string.exercise_pronunciation, card.arabicPhonetic),
                style = MaterialTheme.typography.titleMedium,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            AudioButtons(speakText)
        }
        card.tipAr?.let { TipBox(it) }
    }
}
