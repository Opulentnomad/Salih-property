package com.rihla.english.ui.scene

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.rihla.english.RihlaApplication
import com.rihla.english.core.journey.Beat
import com.rihla.english.core.journey.Chapter
import com.rihla.english.core.journey.Journey
import com.rihla.english.core.journey.JourneyRepository
import com.rihla.english.core.journey.Scene
import com.rihla.english.core.journey.ScenePlayer
import com.rihla.english.core.journey.SceneState
import com.rihla.english.core.journey.SceneSummary
import com.rihla.english.core.journey.Script
import com.rihla.english.util.AppClock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SceneUiState(
    val chapter: Chapter,
    val scene: Scene,
    val play: SceneState,
    /** Set once the scene is finished and saved. */
    val summary: SceneSummary? = null,
    val chapterCompleted: Boolean = false,
    /** The scene to offer next (same chapter), if any. */
    val nextSceneId: String? = null,
)

/** Plays one scene of the journey and records it when finished. */
class SceneViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: JourneyRepository,
    private val clock: AppClock,
) : ViewModel() {

    private val startedAt = clock.nowMillis()
    private val found: Pair<Chapter, Scene>? = Script.scene(checkNotNull(savedStateHandle[ARG_SCENE_ID]))

    private val _state = MutableStateFlow(found?.let { (ch, sc) -> SceneUiState(ch, sc, ScenePlayer.start(sc)) })
    val state: StateFlow<SceneUiState?> = _state.asStateFlow()

    fun next() = transition { ScenePlayer.next(it) }
    fun choose(index: Int) = transition { ScenePlayer.choose(it, index) }
    fun skipShadow() = transition { ScenePlayer.skipShadow(it) }

    /** Saves the highlighted phrase of the current line to the notebook right away. */
    fun saveChunk() {
        val s = _state.value ?: return
        val line = s.play.beat as? Beat.Line ?: return
        val chunk = line.chunk ?: return
        if (chunk.id in s.play.savedChunkIds) return
        _state.update { it?.copy(play = ScenePlayer.saveChunk(it.play, chunk.id)) }
        viewModelScope.launch {
            repository.update { Journey.saveChunk(it, chunk, line.english, "${s.chapter.titleAr} · ${s.scene.titleAr}", clock.todayEpochDay()) }
        }
    }

    private fun transition(step: (SceneState) -> SceneState) {
        val s = _state.value ?: return
        if (s.summary != null) return
        val next = step(s.play)
        _state.update { it?.copy(play = next) }
        if (next.isFinished) finish(s.chapter, next)
    }

    private fun finish(chapter: Chapter, play: SceneState) {
        viewModelScope.launch {
            val seconds = ((clock.nowMillis() - startedAt) / 1000).toInt()
            var summary: SceneSummary? = null
            var completed = false
            repository.update { p ->
                val (updated, sum) = Journey.completeScene(p, chapter, play, clock.todayEpochDay(), seconds)
                summary = sum
                completed = Journey.isChapterComplete(chapter, updated)
                updated
            }
            val nextInChapter = chapter.scenes.getOrNull(play.scene.index + 1)?.id
            _state.update { it?.copy(summary = summary, chapterCompleted = completed, nextSceneId = nextInChapter) }
        }
    }

    companion object {
        const val ARG_SCENE_ID = "sceneId"

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val container = (this[APPLICATION_KEY] as RihlaApplication).container
                SceneViewModel(createSavedStateHandle(), container.journeyStore, container.clock)
            }
        }
    }
}
