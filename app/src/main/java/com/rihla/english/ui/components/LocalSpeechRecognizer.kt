package com.rihla.english.ui.components

import androidx.compose.runtime.staticCompositionLocalOf
import com.rihla.english.audio.SpeechRecognizerManager

/** Provided by MainActivity; null in previews (speaking challenges then offer the skip option). */
val LocalSpeechRecognizer = staticCompositionLocalOf<SpeechRecognizerManager?> { null }
