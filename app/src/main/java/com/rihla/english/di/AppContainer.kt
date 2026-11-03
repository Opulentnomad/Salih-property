package com.rihla.english.di

import android.content.Context
import com.rihla.english.audio.SpeechRecognizerManager
import com.rihla.english.audio.TtsManager
import com.rihla.english.core.engine.GamificationEngine
import com.rihla.english.core.repository.CurriculumRepository
import com.rihla.english.core.repository.UserStatsRepository
import com.rihla.english.data.local.RihlaDatabase
import com.rihla.english.data.local.UserStatsDataStore
import com.rihla.english.data.local.userStatsDataStore
import com.rihla.english.data.repository.CurriculumRepositoryImpl
import com.rihla.english.util.AppClock
import com.rihla.english.util.SystemAppClock

/**
 * Manual dependency injection: one container per process, created by [com.rihla.english.RihlaApplication].
 * Everything is lazy so cold start stays fast.
 */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val clock: AppClock = SystemAppClock()
    val gamificationEngine = GamificationEngine()

    private val database by lazy { RihlaDatabase.create(appContext) }

    val curriculumRepository: CurriculumRepository by lazy {
        CurriculumRepositoryImpl(database.curriculumDao(), database.progressDao())
    }

    val userStatsRepository: UserStatsRepository by lazy {
        UserStatsDataStore(appContext.userStatsDataStore)
    }

    val ttsManager: TtsManager by lazy { TtsManager(appContext) }
    val speechRecognizer: SpeechRecognizerManager by lazy { SpeechRecognizerManager(appContext) }
}
