package com.rihla.english.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rihla.english.R
import com.rihla.english.core.model.UserStats
import com.rihla.english.ui.theme.Blue
import com.rihla.english.ui.theme.Gold
import com.rihla.english.ui.theme.Orange
import com.rihla.english.ui.theme.Red
import com.rihla.english.ui.theme.RihlaTheme
import com.rihla.english.util.formatCountdown

/** Streak 🔥, Gems 💎 and Hearts ❤️ — plus an ⚡ indicator while a Double XP boost is active. */
@Composable
fun TopGamificationBar(
    stats: UserStats,
    doubleXpRemainingMillis: Long?,
    onStreakClick: () -> Unit,
    onGemsClick: () -> Unit,
    onHeartsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = RihlaTheme.colors
    Column(
        modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            // Course flag: English for Arabic speakers.
            Text("🇬🇧", fontSize = 26.sp, modifier = Modifier.padding(horizontal = 4.dp))

            StatChip(
                emoji = "🔥",
                value = stats.currentStreak,
                color = if (stats.dailyGoalReached) Orange else colors.textSecondary,
                onClick = onStreakClick,
            )
            StatChip(emoji = "💎", value = stats.gems, color = Blue, onClick = onGemsClick)
            StatChip(
                emoji = if (stats.hearts > 0) "❤️" else "💔",
                value = stats.hearts,
                color = if (stats.hearts > 0) Red else colors.textSecondary,
                onClick = onHeartsClick,
            )
        }
        AnimatedVisibility(visible = doubleXpRemainingMillis != null) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 2.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Gold.copy(alpha = 0.2f))
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    stringResource(R.string.double_xp_active, formatCountdown(doubleXpRemainingMillis ?: 0L)),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFFB98900),
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        HorizontalDivider(thickness = 2.dp, color = colors.border)
    }
}

@Composable
private fun StatChip(emoji: String, value: Int, color: Color, onClick: () -> Unit) {
    val animated by animateIntAsState(value, label = "stat")
    Row(
        Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(emoji, fontSize = 22.sp)
        Spacer(Modifier.width(6.dp))
        Text(animated.toString(), style = MaterialTheme.typography.titleMedium, color = color)
    }
}
