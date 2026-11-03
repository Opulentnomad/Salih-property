package com.rihla.english.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rihla.english.R
import com.rihla.english.audio.SpeechPlayer
import com.rihla.english.audio.TtsState
import com.rihla.english.ui.theme.Blue
import com.rihla.english.ui.theme.BlueDark
import com.rihla.english.ui.theme.RihlaTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** Provided by MainActivity; a silent fallback keeps previews working. */
val LocalSpeechPlayer = staticCompositionLocalOf<SpeechPlayer> { SilentSpeechPlayer }

private object SilentSpeechPlayer : SpeechPlayer {
    override val state: StateFlow<TtsState> = MutableStateFlow(TtsState.UNAVAILABLE)
    override fun speak(text: String, slow: Boolean) = Unit
    override fun stop() = Unit
}

/** The two playback modes required on every item: 🔊 normal and 🐢 slow (0.65x). */
@Composable
fun AudioButtons(
    text: String,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
) {
    val player = LocalSpeechPlayer.current
    val normalLabel = stringResource(R.string.audio_normal)
    val slowLabel = stringResource(R.string.audio_slow)
    Ltr {
        Row(modifier, horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            PressableSurface(
                onClick = { player.speak(text, slow = false) },
                modifier = Modifier.width(size).semantics { contentDescription = normalLabel },
                color = Blue,
                shadowColor = BlueDark,
            ) {
                Box(Modifier.height(size - 4.dp), contentAlignment = Alignment.Center) {
                    Text("🔊", fontSize = (size.value * 0.42f).sp)
                }
            }
            PressableSurface(
                onClick = { player.speak(text, slow = true) },
                modifier = Modifier.width(size * 0.8f).semantics { contentDescription = slowLabel },
                color = RihlaTheme.colors.cardBackground,
                shadowColor = RihlaTheme.colors.border,
                border = BorderStroke(2.dp, RihlaTheme.colors.border),
            ) {
                Box(Modifier.height(size * 0.8f - 4.dp), contentAlignment = Alignment.Center) {
                    Text("🐢", fontSize = (size.value * 0.34f).sp)
                }
            }
        }
    }
}

/** Small inline speaker button for list items. */
@Composable
fun MiniSpeaker(text: String, modifier: Modifier = Modifier) {
    val player = LocalSpeechPlayer.current
    PressableSurface(
        onClick = { player.speak(text) },
        modifier = modifier.width(40.dp),
        color = Blue,
        shadowColor = BlueDark,
        shape = RoundedCornerShape(12.dp),
        depth = 3.dp,
    ) { Box(Modifier.height(37.dp), contentAlignment = Alignment.Center) { Text("🔊", fontSize = 16.sp) } }
}
