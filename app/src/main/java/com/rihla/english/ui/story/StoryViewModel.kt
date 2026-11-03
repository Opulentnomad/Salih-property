package com.rihla.english.ui.story

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.rihla.english.RihlaApplication
import com.rihla.english.core.engine.GamificationEngine
import com.rihla.english.core.engine.GamificationEvent
import com.rihla.english.core.model.Badge
import com.rihla.english.core.repository.InsightsRepository
import com.rihla.english.core.repository.UserStatsRepository
import com.rihla.english.core.story.SeedStories
import com.rihla.english.core.story.StoryEngine
import com.rihla.english.core.story.StoryState
import com.rihla.english.util.AppClock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StoryReward(val stars: Int, val xp: Int, val gems: Int, val badges: List<Badge>, val streakExtended: Boolean)

data class StoryUiState(
    val play: StoryState? = null,
    val reward: StoryReward? = null,
    val saving: Boolean = false,
)

/** Plays one interactive dialogue story and grants its rewards when finished. */
class StoryViewModel(
    savedStateHandle: SavedStateHandle,
    private val statsRepository: UserStatsRepository,
    private val insights: InsightsRepository,
    private val engine: GamificationEngine,
    private val clock: AppClock,
) : ViewModel() {

    private val storyId: String = checkNotNull(savedStateHandle[ARG_STORY_ID])

    private val _state = MutableStateFlow(StoryUiState(play = SeedStories.byId(storyId)?.let(StoryEngine::start)))
    val state: StateFlow<StoryUiState> = _state.asStateFlow()

    /** Reveals the next spoken line (called by the UI's typing timer or a tap). */
    fun advance() = transition { StoryEngine.advance(it) }

    fun choose(optionIndex: Int) = transition { StoryEngine.choose(it, optionIndex) }

    fun restart() {
        val story = SeedStories.byId(storyId) ?: return
        _state.value = StoryUiState(play = StoryEngine.start(story))
    }

    private fun transition(step: (StoryState) -> StoryState) {
        val current = _state.value
        val play = current.play ?: return
        if (current.reward != null || current.saving) return
        val next = step(play)
        _state.update { it.copy(play = next) }
        if (next.isFinished) finish(next)
    }

    private fun finish(play: StoryState) {
        _state.update { it.copy(saving = true) }
        viewModelScope.launch {
            val today = clock.todayEpochDay()
            val now = clock.nowMillis()
            val firstTime = (insights.insights.first().stories[storyId]?.timesCompleted ?: 0) == 0
            val outcome = statsRepository.updateAndGet { s ->
                val o = engine.completeStory(s, play.stars, today, now, firstTime)
                o.stats to o
            }
            val xp = outcome.events.filterIsInstance<GamificationEvent.XpGained>().sumOf { it.amount }
            insights.recordStory(today, storyId, play.stars, xp)
            _state.update {
                it.copy(
                    saving = false,
                    reward = StoryReward(
                        stars = play.stars,
                        xp = xp,
                        gems = outcome.events.filterIsInstance<GamificationEvent.GemsGained>().sumOf { g -> g.amount },
                        badges = outcome.events.filterIsInstance<GamificationEvent.BadgeUnlocked>().map { b -> b.badge },
                        streakExtended = outcome.events.any { e -> e is GamificationEvent.StreakExtended },
                    ),
                )
            }
        }
    }

    companion object {
        const val ARG_STORY_ID = "storyId"

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val container = (this[APPLICATION_KEY] as RihlaApplication).container
                StoryViewModel(
                    savedStateHandle = createSavedStateHandle(),
                    statsRepository = container.userStatsRepository,
                    insights = container.insightsRepository,
                    engine = container.gamificationEngine,
                    clock = container.clock,
                )
            }
        }
    }
}
