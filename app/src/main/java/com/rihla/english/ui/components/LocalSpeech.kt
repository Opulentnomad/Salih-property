package com.rihla.english.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.rihla.english.R
import com.rihla.english.audio.SpeechPlayer
import com.rihla.english.audio.TtsState
import com.rihla.english.ui.theme.Cine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** Provided by MainActivity; a silent fallback keeps previews working. */
val LocalSpeechPlayer = staticCompositionLocalOf<SpeechPlayer> { SilentSpeechPlayer }

private object SilentSpeechPlayer : SpeechPlayer {
    override val state: StateFlow<TtsState> = MutableStateFlow(TtsState.UNAVAILABLE)
    override fun speak(text: String, slow: Boolean) = Unit
    override fun stop() = Unit
}

/** A pill-shaped secondary control used in the subtitle area (listen, slow, save). */
@Composable
fun RowScope.ControlPill(
    label: String,
    onClick: () -> Unit,
    icon: CineIcon,
    active: Boolean = false,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.weight(1f).heightIn(min = 46.dp),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, if (active) Cine.Amber else Cine.Line),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (active) Cine.AmberTint else Cine.Panel,
            contentColor = if (active) Cine.AmberSoft else Cine.Text,
        ),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            CineIconView(icon, tint = if (active) Cine.AmberSoft else Cine.Text, modifier = Modifier.size(18.dp))
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

/** Listen at normal speed and at 0.65× ("turtle") speed. */
@Composable
fun RowScope.AudioPills(text: String) {
    val player = LocalSpeechPlayer.current
    ControlPill(stringResource(R.string.listen), onClick = { player.speak(text) }, icon = CineIcon.Speaker)
    ControlPill(stringResource(R.string.listen_slow), onClick = { player.speak(text, slow = true) }, icon = CineIcon.Slow)
}

