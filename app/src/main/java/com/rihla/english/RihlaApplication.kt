package com.rihla.english

import android.app.Application
import com.rihla.english.di.AppContainer
import com.rihla.english.reminders.ReminderScheduler

class RihlaApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        ReminderScheduler.ensureChannel(this)
    }
}
