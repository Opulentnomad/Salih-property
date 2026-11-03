package com.rihla.english.ui.lesson

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rihla.english.R
import com.rihla.english.ui.components.ArabicText
import com.rihla.english.ui.components.EnglishText
import com.rihla.english.ui.components.MiniSpeaker
import com.rihla.english.ui.components.RihlaButton
import com.rihla.english.ui.theme.EnglishTextStyles
import com.rihla.english.ui.theme.Gold
import com.rihla.english.ui.theme.Green
import com.rihla.english.ui.theme.GreenDark
import com.rihla.english.ui.theme.Red
import com.rihla.english.ui.theme.RedDark
import com.rihla.english.ui.theme.RihlaTheme

/** Bottom sheet shown after "Check": green celebration or red correction with Arabic explanation. */
@Composable
fun FeedbackSheet(feedback: AnswerFeedback, onContinue: () -> Unit, modifier: Modifier = Modifier) {
    val colors = RihlaTheme.colors
    val accent = if (feedback.correct) colors.correctText else colors.wrongText
    Column(
        modifier
            .fillMaxWidth()
            .background(if (feedback.correct) colors.correctBackground else colors.wrongBackground)
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center,
            ) { Text(if (feedback.correct) "✓" else "✗", color = accent, fontSize = 26.sp) }
            Spacer(Modifier.width(12.dp))
            Text(
                stringResource(if (feedback.correct) R.string.correct_title else R.string.incorrect_title),
                style = MaterialTheme.typography.headlineMedium,
                color = accent,
                modifier = Modifier.weight(1f),
            )
            if (feedback.correct && feedback.xpGained > 0) {
                Text(
                    stringResource(R.string.xp_gained, feedback.xpGained),
                    style = MaterialTheme.typography.titleLarge,
                    color = Gold,
                )
            }
        }

        if (feedback.correct && feedback.comboBonusXp > 0) {
            Text(
                stringResource(R.string.correct_combo, feedback.combo, feedback.comboBonusXp),
                style = MaterialTheme.typography.titleMedium,
                color = accent,
            )
        }

        if (!feedback.correct && feedback.correctAnswer != null) {
            Text(stringResource(R.string.correct_answer_label), style = MaterialTheme.typography.titleMedium, color = accent)
            Row(verticalAlignment = Alignment.CenterVertically) {
                EnglishText(feedback.correctAnswer, style = EnglishTextStyles.medium, color = accent, modifier = Modifier.weight(1f))
                MiniSpeaker(feedback.correctAnswer)
            }
        }
        val confused = feedback.confusedWith
        if (confused != null && feedback.correctAnswer != null) {
            Text(
                stringResource(R.string.speak_confused, confused, feedback.correctAnswer),
                style = MaterialTheme.typography.titleMedium,
                color = accent,
            )
        } else {
            feedback.heardText?.let {
                Text(stringResource(R.string.speak_heard, it), style = MaterialTheme.typography.bodyMedium, color = accent)
            }
        }
        if (!feedback.correct && feedback.explanationAr != null) {
            ArabicText(feedback.explanationAr, style = MaterialTheme.typography.bodyLarge, color = colors.textPrimary)
        }

        RihlaButton(
            text = stringResource(R.string.continue_label),
            onClick = onContinue,
            color = if (feedback.correct) Green else Red,
            shadowColor = if (feedback.correct) GreenDark else RedDark,
        )
    }
}
