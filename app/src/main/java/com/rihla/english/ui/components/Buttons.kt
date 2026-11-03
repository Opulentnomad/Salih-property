package com.rihla.english.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import com.rihla.english.ui.theme.Green
import com.rihla.english.ui.theme.GreenDark
import com.rihla.english.ui.theme.RihlaTheme

/**
 * Chunky "3D" button: a darker base layer sits under the face, and the face sinks into it
 * while pressed — the tactile style that makes gamified apps feel responsive.
 */
@Composable
fun PressableSurface(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color,
    shadowColor: Color,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(16.dp),
    depth: Dp = 4.dp,
    border: BorderStroke? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val offset by animateDpAsState(if (pressed && enabled) depth else 0.dp, label = "press")
    Box(modifier) {
        Box(
            Modifier
                .matchParentSize()
                .padding(top = depth)
                .clip(shape)
                .background(shadowColor),
        )
        Box(
            Modifier
                .fillMaxWidth()
                .padding(bottom = depth)
                .offset(y = offset)
                .clip(shape)
                .background(color)
                .then(if (border != null) Modifier.border(border, shape) else Modifier)
                .clickable(
                    interactionSource = interaction,
                    indication = null,
                    enabled = enabled,
                    role = Role.Button,
                    onClick = onClick,
                ),
            contentAlignment = Alignment.Center,
            content = content,
        )
    }
}

@Composable
fun RihlaButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = Green,
    shadowColor: Color = GreenDark,
    textColor: Color = Color.White,
    enabled: Boolean = true,
) {
    val colors = RihlaTheme.colors
    PressableSurface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        color = if (enabled) color else colors.locked,
        shadowColor = if (enabled) shadowColor else colors.lockedShadow,
        enabled = enabled,
    ) {
        Box(Modifier.height(52.dp), contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = if (enabled) textColor else colors.textSecondary,
            )
        }
    }
}

/** Outlined secondary variant (white face, grey border). */
@Composable
fun RihlaOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    textColor: Color = com.rihla.english.ui.theme.Blue,
) {
    val colors = RihlaTheme.colors
    PressableSurface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        color = colors.cardBackground,
        shadowColor = colors.border,
        border = BorderStroke(2.dp, colors.border),
    ) {
        Box(Modifier.height(52.dp), contentAlignment = Alignment.Center) {
            Text(text = text, style = MaterialTheme.typography.labelLarge, color = textColor)
        }
    }
}
