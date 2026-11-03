package com.rihla.english.ui.notebook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.rihla.english.RihlaApplication
import com.rihla.english.core.journey.Journey
import com.rihla.english.core.journey.JourneyRepository
import com.rihla.english.core.journey.NotebookEntry
import com.rihla.english.util.AppClock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReviewUiState(
    val loading: Boolean = true,
    val queue: List<NotebookEntry> = emptyList(),
    val index: Int = 0,
    val revealed: Boolean = false,
) {
    val current: NotebookEntry? get() = queue.getOrNull(index)
    val finished: Boolean get() = !loading && index >= queue.size
}

/** Spaced-repetition review of notebook phrases: hear it in context, recall, self-grade. */
class ReviewViewModel(
    private val repository: JourneyRepository,
    private val clock: AppClock,
) : ViewModel() {

    private val startedAt = clock.nowMillis()
    private val _state = MutableStateFlow(ReviewUiState())
    val state: StateFlow<ReviewUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val due = Journey.dueEntries(repository.progress.first(), clock.todayEpochDay()).take(SESSION_SIZE)
            _state.value = ReviewUiState(loading = false, queue = due)
        }
    }

    fun reveal() = _state.update { it.copy(revealed = true) }

    /** 5 = knew it, 3 = almost, 1 = didn't. */
    fun grade(quality: Int) {
        val entry = _state.value.current ?: return
        val today = clock.todayEpochDay()
        val last = _state.value.index == _state.value.queue.lastIndex
        viewModelScope.launch {
            repository.update {
                val reviewed = Journey.review(it, entry.chunkId, quality, today)
                if (last) Journey.addTime(reviewed, today, ((clock.nowMillis() - startedAt) / 1000).toInt()) else reviewed
            }
        }
        _state.update { it.copy(index = it.index + 1, revealed = false) }
    }

    companion object {
        const val SESSION_SIZE = 8

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val container = (this[APPLICATION_KEY] as RihlaApplication).container
                ReviewViewModel(container.journeyStore, container.clock)
            }
        }
    }
}
