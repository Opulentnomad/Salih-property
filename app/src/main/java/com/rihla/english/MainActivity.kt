package com.rihla.english

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.CompositionLocalProvider
import com.rihla.english.ui.components.LocalSpeechPlayer
import com.rihla.english.ui.components.LocalSpeechRecognizer
import com.rihla.english.ui.components.Rtl
import com.rihla.english.ui.journey.JourneyViewModel
import com.rihla.english.ui.navigation.RihlaApp
import com.rihla.english.ui.theme.RihlaTheme

class MainActivity : ComponentActivity() {

    private val journeyViewModel: JourneyViewModel by viewModels { JourneyViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val container = (application as RihlaApplication).container
        setContent {
            RihlaTheme {
                // The whole app chrome is Arabic (RTL); English content opts into LTR locally.
                Rtl {
                    CompositionLocalProvider(
                        LocalSpeechPlayer provides container.ttsManager,
                        LocalSpeechRecognizer provides container.speechRecognizer,
                    ) {
                        RihlaApp(journeyViewModel)
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
