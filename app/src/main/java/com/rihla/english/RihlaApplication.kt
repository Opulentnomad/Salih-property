package com.rihla.english

import android.app.Application
import com.rihla.english.di.AppContainer

class RihlaApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
