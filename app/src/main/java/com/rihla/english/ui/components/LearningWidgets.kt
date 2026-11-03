package com.rihla.english.ui.components

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rihla.english.R
import com.rihla.english.core.model.GrammarBridge
import com.rihla.english.ui.theme.EnglishTextStyles
import com.rihla.english.ui.theme.Gold
import com.rihla.english.ui.theme.Purple
import com.rihla.english.ui.theme.RihlaTheme

/** Tappable English word chip used by word-bank and listening-assemble exercises. */
@Composable
fun WordChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: Boolean = false,
    enabled: Boolean = true,
) {
    val colors = RihlaTheme.colors
    if (placeholder) {
        // Keeps the chip's footprint in the bank after it moves to the answer line.
        Box(
            modifier
                .clip(RoundedCornerShape(12.dp))
                .background(colors.border)
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            EnglishText(text, style = EnglishTextStyles.chip, color = Color.Transparent)
        }
        return
    }
    PressableSurface(
        onClick = onClick,
        modifier = modifier.widthIn(min = 48.dp),
        color = colors.cardBackground,
        shadowColor = colors.border,
        border = BorderStroke(2.dp, colors.border),
        shape = RoundedCornerShape(12.dp),
        depth = 3.dp,
        enabled = enabled,
    ) {
        EnglishText(
            text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            style = EnglishTextStyles.chip,
            color = colors.textPrimary,
        )
    }
}

/** Mascot 🐪 with a speech bubble — used to present the Arabic prompt. */
@Composable
fun MascotBubble(modifier: Modifier = Modifier, mascot: String = "🐪", content: @Composable () -> Unit) {
    val colors = RihlaTheme.colors
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(mascot, fontSize = 56.sp)
        Spacer(Modifier.width(8.dp))
        Box(
            Modifier
                .border(BorderStroke(2.dp, colors.border), RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) { content() }
    }
}

/**
 * Visual Grammar Bridge: the Arabic nominal sentence on top (RTL), its English equivalent below
 * (LTR). English words with no Arabic counterpart — the copula am/is/are and articles — are
 * highlighted and labelled "∅"; every other word carries the number of the Arabic word it translates.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GrammarBridgeCard(bridge: GrammarBridge, modifier: Modifier = Modifier) {
    val colors = RihlaTheme.colors
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Purple.copy(alpha = 0.12f))
            .border(2.dp, Purple.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(stringResource(R.string.grammar_bridge_title), style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)

        // Arabic row (RTL): one box per Arabic token.
        Rtl {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                bridge.arabicTokens.forEachIndexed { i, token ->
                    BridgeBox(
                        text = token,
                        index = i + 1,
                        background = colors.cardBackground,
                        english = false,
                    )
                }
            }
        }

        Text("⬇", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, fontSize = 20.sp)

        // English row (LTR): words mapped to Arabic indices; hidden words highlighted.
        Ltr {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                bridge.englishTokens.forEach { token ->
                    val hidden = token.arabicIndex == null
                    BridgeBox(
                        text = token.text,
                        index = token.arabicIndex?.plus(1),
                        background = if (hidden) Gold.copy(alpha = 0.35f) else colors.cardBackground,
                        english = true,
                        dashed = hidden,
                    )
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .width(18.dp)
                    .height(18.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Gold.copy(alpha = 0.35f)),
            )
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.grammar_bridge_hidden), style = MaterialTheme.typography.labelMedium, color = colors.textSecondary)
        }
        ArabicText(bridge.explanationAr, style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
    }
}

@Composable
private fun BridgeBox(text: String, index: Int?, background: Color, english: Boolean, dashed: Boolean = false) {
    val colors = RihlaTheme.colors
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(background)
                .border(2.dp, if (dashed) Gold else colors.border, RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
        ) {
            if (english) {
                EnglishText(text, style = EnglishTextStyles.chip, color = colors.textPrimary)
            } else {
                Text(text, style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
            }
        }
        Text(
            text = index?.toString() ?: "∅",
            style = MaterialTheme.typography.labelMedium,
            color = colors.textSecondary,
            modifier = Modifier.alpha(0.8f),
        )
    }
}
