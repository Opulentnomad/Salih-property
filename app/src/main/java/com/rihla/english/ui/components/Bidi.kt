package com.rihla.english.ui.components

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.LayoutDirection
import com.rihla.english.ui.theme.EnglishTextStyles

/*
 * Bidirectional UI helpers. The app chrome is RTL (Arabic); every English letter, word or
 * sentence used as learning content is forced LTR so word order and punctuation never flip.
 */

/** Lays out [content] left-to-right (e.g. English word chips in sentence order). */
@Composable
fun Ltr(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr, content = content)
}

/** Lays out [content] right-to-left (Arabic). */
@Composable
fun Rtl(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl, content = content)
}

/** English learning text: forced LTR, large and high-contrast by default. */
@Composable
fun EnglishText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = EnglishTextStyles.large,
    color: Color = Color.Unspecified,
    textAlign: TextAlign = TextAlign.Start,
) {
    Ltr {
        Text(
            text = text,
            modifier = modifier,
            style = style.copy(textDirection = TextDirection.Ltr),
            color = if (color == Color.Unspecified) LocalContentColor.current else color,
            textAlign = textAlign,
        )
    }
}

/** Arabic text: forced RTL direction so mixed Arabic/Latin explanations render correctly. */
@Composable
fun ArabicText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle,
    color: Color = Color.Unspecified,
    textAlign: TextAlign = TextAlign.Start,
) {
    Rtl {
        Text(
            text = text,
            modifier = modifier,
            style = style.copy(textDirection = TextDirection.Rtl),
            color = if (color == Color.Unspecified) LocalContentColor.current else color,
            textAlign = textAlign,
        )
    }
}
