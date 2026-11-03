package com.rihla.english.ui.journey

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.rihla.english.RihlaApplication
import com.rihla.english.core.journey.Chapter
import com.rihla.english.core.journey.ChapterView
import com.rihla.english.core.journey.Journey
import com.rihla.english.core.journey.JourneyProgress
import com.rihla.english.core.journey.Mission
import com.rihla.english.core.journey.Scene
import com.rihla.english.core.journey.Script
import com.rihla.english.core.journey.Stamp
import com.rihla.english.core.model.DailyGoalLevel
import com.rihla.english.core.model.LearningGoal
import com.rihla.english.core.model.UserProfile
import com.rihla.english.core.season.Season
import com.rihla.english.core.season.SeasonCalendar
import com.rihla.english.data.JourneyStore
import com.rihla.english.reminders.ReminderScheduler
import com.rihla.english.util.AppClock
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class JourneyUiState(
    val loaded: Boolean = false,
    val profile: UserProfile = UserProfile(),
    val progress: JourneyProgress = JourneyProgress(),
    val chapters: List<ChapterView> = emptyList(),
    val nextScene: Scene? = null,
    val nextChapter: Chapter? = null,
    val activeMission: Mission? = null,
    val stamps: List<Stamp> = emptyList(),
    val minutesToday: Int = 0,
    val commitmentDays: Int = 0,
    val dueCount: Int = 0,
    val today: Long = 0,
    val season: Season? = null,
)

/** Activity-scoped hub: journey map, missions, passport and profile/onboarding. */
class JourneyViewModel(
    private val store: JourneyStore,
    private val clock: AppClock,
    private val appContext: Context,
) : ViewModel() {

    /** Null until loaded, so the app can decide whether to show onboarding without flashing. */
    val profile: StateFlow<UserProfile?> = store.profile.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val state: StateFlow<JourneyUiState> = combine(store.progress, store.profile) { p, profile ->
        val today = clock.todayEpochDay()
        val next = Journey.nextScene(Script.chapters, p)
        JourneyUiState(
            loaded = true,
            profile = profile,
            progress = p,
            chapters = Journey.chapterViews(Script.chapters, p),
            nextScene = next,
            nextChapter = next?.let { Script.chapter(it.chapterId) },
            activeMission = Journey.activeMission(Script.chapters, p),
            stamps = Journey.stamps(Script.chapters, p),
            minutesToday = Journey.minutesToday(p, today),
            commitmentDays = Journey.commitmentDays(p, today),
            dueCount = Journey.dueEntries(p, today).size,
            today = today,
            season = SeasonCalendar.seasonOn(today),
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, JourneyUiState())

    fun acceptMission(mission: Mission) = viewModelScope.launch {
        store.update { Journey.acceptMission(it, mission, clock.todayEpochDay()) }
    }

    fun completeMission(mission: Mission, note: String) = viewModelScope.launch {
        store.update { Journey.completeMission(it, mission, clock.todayEpochDay(), note) }
    }

    fun completeOnboarding(name: String, goal: LearningGoal?, level: DailyGoalLevel, reminderHour: Int?) = viewModelScope.launch {
        store.updateProfile {
            it.copy(
                name = name.trim().take(24),
                goal = goal,
                dailyMinutes = level.minutes,
                onboarded = true,
                remindersEnabled = reminderHour != null,
                reminderHour = reminderHour ?: it.reminderHour,
                reminderMinute = 0,
            )
        }
        if (reminderHour != null) ReminderScheduler.schedule(appContext, reminderHour, 0) else ReminderScheduler.cancel(appContext)
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as RihlaApplication
                JourneyViewModel(app.container.journeyStore, app.container.clock, app)
            }
        }
    }
}
