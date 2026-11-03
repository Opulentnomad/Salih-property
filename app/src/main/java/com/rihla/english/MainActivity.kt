package com.rihla.english

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.rihla.english.ui.components.LocalSpeechPlayer
import com.rihla.english.ui.components.LocalSpeechRecognizer
import com.rihla.english.ui.components.Rtl
import com.rihla.english.ui.gamification.GamificationViewModel
import com.rihla.english.ui.navigation.RihlaApp
import com.rihla.english.ui.theme.RihlaTheme

class MainActivity : ComponentActivity() {

    private val gamificationViewModel: GamificationViewModel by viewModels { GamificationViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val container = (application as RihlaApplication).container

        // Daily streak/freeze processing, heart regeneration and countdowns run while visible.
        lifecycle.addObserver(
            LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_START -> gamificationViewModel.onForeground()
                    Lifecycle.Event.ON_STOP -> gamificationViewModel.onBackground()
                    else -> Unit
                }
            },
        )

        setContent {
            RihlaTheme {
                // The whole app chrome is Arabic (RTL); English content opts into LTR locally.
                Rtl {
                    CompositionLocalProvider(
                        LocalSpeechPlayer provides container.ttsManager,
                        LocalSpeechRecognizer provides container.speechRecognizer,
                    ) {
                        Surface(color = MaterialTheme.colorScheme.background) {
                            RihlaApp(gamificationViewModel)
                        }
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        (application as RihlaApplication).container.ttsManager.stop()
    }
}
