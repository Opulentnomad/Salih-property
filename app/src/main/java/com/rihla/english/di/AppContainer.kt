package com.rihla.english.di

import android.content.Context
import com.rihla.english.audio.SpeechRecognizerManager
import com.rihla.english.audio.TtsManager
import com.rihla.english.data.JourneyStore
import com.rihla.english.data.journeyDataStore
import com.rihla.english.util.AppClock
import com.rihla.english.util.SystemAppClock

/** Manual dependency injection: one container per process, created by the Application. */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val clock: AppClock = SystemAppClock()
    val journeyStore: JourneyStore by lazy { JourneyStore(appContext.journeyDataStore) }
    val ttsManager: TtsManager by lazy { TtsManager(appContext) }
    val speechRecognizer: SpeechRecognizerManager by lazy { SpeechRecognizerManager(appContext) }
}
