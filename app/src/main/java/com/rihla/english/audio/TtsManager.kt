package com.rihla.english.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Android [TextToSpeech] wrapper. Prefers US English, falls back to UK English, and picks an
 * offline (embedded) voice when one is installed so audio works without network.
 */
class TtsManager(context: Context) : SpeechPlayer, TextToSpeech.OnInitListener {

    private val _state = MutableStateFlow(TtsState.INITIALIZING)
    override val state: StateFlow<TtsState> = _state.asStateFlow()

    private var pending: Pair<String, Boolean>? = null
    private val tts = TextToSpeech(context.applicationContext, this)

    override fun onInit(status: Int) {
        if (status != TextToSpeech.SUCCESS) {
            _state.value = TtsState.UNAVAILABLE
            return
        }
        val locale = listOf(Locale.US, Locale.UK, Locale.ENGLISH).firstOrNull { candidate ->
            tts.isLanguageAvailable(candidate) >= TextToSpeech.LANG_AVAILABLE
        }
        if (locale == null) {
            _state.value = TtsState.UNAVAILABLE
            return
        }
        tts.language = locale
        preferOfflineVoice(locale)
        _state.value = TtsState.READY
        pending?.let { (text, slow) -> speak(text, slow) }
        pending = null
    }

    private fun preferOfflineVoice(locale: Locale) {
        val voices: Set<Voice> = runCatching { tts.voices }.getOrNull().orEmpty()
        voices
            .filter { it.locale.language == locale.language && it.locale.country == locale.country }
            .filter { !it.isNetworkConnectionRequired && TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED !in it.features.orEmpty() }
            .maxByOrNull { it.quality }
            ?.let { tts.voice = it }
    }

    override fun speak(text: String, slow: Boolean) {
        when (_state.value) {
            TtsState.INITIALIZING -> pending = text to slow
            TtsState.UNAVAILABLE -> Unit
            TtsState.READY -> {
                tts.setSpeechRate(if (slow) SpeechPlayer.SLOW_RATE else SpeechPlayer.NORMAL_RATE)
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, UUID.randomUUID().toString())
            }
        }
    }

    override fun stop() {
        if (_state.value == TtsState.READY) tts.stop()
    }

    fun shutdown() {
        tts.stop()
        tts.shutdown()
    }
}
