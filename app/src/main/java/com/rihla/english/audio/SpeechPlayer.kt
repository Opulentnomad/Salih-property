package com.rihla.english.audio

import kotlinx.coroutines.flow.StateFlow

/** Plays English audio for exercise items. Normal speed and "turtle" slow speed (0.65x). */
interface SpeechPlayer {
    val state: StateFlow<TtsState>

    fun speak(text: String, slow: Boolean = false)
    fun stop()

    companion object {
        const val NORMAL_RATE = 1.0f
        const val SLOW_RATE = 0.65f
    }
}

enum class TtsState { INITIALIZING, READY, UNAVAILABLE }
