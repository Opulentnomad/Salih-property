package com.rihla.english.ui.lesson

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.rihla.english.R
import com.rihla.english.RihlaApplication
import com.rihla.english.core.engine.ExerciseGrader
import com.rihla.english.core.engine.GamificationEngine
import com.rihla.english.core.engine.GamificationEvent
import com.rihla.english.core.engine.LessonQueue
import com.rihla.english.core.engine.LessonResult
import com.rihla.english.core.engine.Outcome
import com.rihla.english.core.engine.PurchaseResult
import com.rihla.english.core.engine.SkillPath
import com.rihla.english.core.engine.UserAnswer
import com.rihla.english.core.model.Badge
import com.rihla.english.core.model.ErrorCorrection
import com.rihla.english.core.model.ErrorPattern
import com.rihla.english.core.model.SessionId
import com.rihla.english.core.model.SessionKind
import com.rihla.english.core.model.Exercise
import com.rihla.english.core.model.GrammarBridge
import com.rihla.english.core.model.ShopItem
import com.rihla.english.core.model.SpeakingChallenge
import com.rihla.english.core.model.UserStats
import com.rihla.english.core.repository.CurriculumRepository
import com.rihla.english.core.repository.ExerciseOutcome
import com.rihla.english.core.repository.InsightsRepository
import com.rihla.english.core.sound.SoundLab
import com.rihla.english.core.repository.UserStatsRepository
import com.rihla.english.ui.UiMessage
import com.rihla.english.util.AppClock
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class LessonPhase { LOADING, INTRO, PLAYING, OUT_OF_HEARTS, SAVING, FINISHED, ERROR }

/** Shown in the bottom feedback sheet after "Check". */
data class AnswerFeedback(
    val correct: Boolean,
    val xpGained: Int,
    val comboBonusXp: Int,
    val combo: Int,
    /** English (LTR) correct answer, shown when wrong. */
    val correctAnswer: String?,
    val explanationAr: String?,
    /** What the speech recognizer heard (speaking challenges). */
    val heardText: String? = null,
    /** Speaking: the minimal-pair word that was heard instead of the target. */
    val confusedWith: String? = null,
)

/** Everything the victory screen celebrates. */
data class VictorySummary(
    val isPractice: Boolean,
    val xpEarned: Int,
    val accuracyPercent: Int,
    val isPerfect: Boolean,
    val streak: Int,
    val streakExtended: Boolean,
    val freezeUsed: Boolean,
    val gemsEarned: Int,
    val heartsGained: Int,
    val newLevel: Int?,
    val dailyGoalReached: Boolean,
    val badges: List<Badge>,
    val kind: SessionKind = if (isPractice) SessionKind.PRACTICE else SessionKind.LESSON,
    /** Number of crescents lit this Ramadan, if one was lit by this session. */
    val ramadanCrescents: Int? = null,
)

data class LessonUiState(
    val phase: LessonPhase = LessonPhase.LOADING,
    val isPractice: Boolean = false,
    val kind: SessionKind = SessionKind.LESSON,
    /** For pattern / sound sessions: the pattern being trained (intro shows its example). */
    val focusPattern: ErrorPattern? = null,
    val titleAr: String = "",
    val introTipAr: String? = null,
    val grammarBridge: GrammarBridge? = null,
    val exercise: Exercise? = null,
    /** Increments every time an exercise is presented, so per-exercise UI state resets on retries. */
    val presentation: Int = 0,
    val isRetry: Boolean = false,
    val progress: Float = 0f,
    val hearts: Int = UserStats.MAX_HEARTS,
    val gems: Int = 0,
    val combo: Int = 0,
    val sessionXp: Int = 0,
    val doubleXp: Boolean = false,
    val feedback: AnswerFeedback? = null,
    val nextHeartMillis: Long? = null,
    val victory: VictorySummary? = null,
    val message: UiMessage? = null,
)

/**
 * Runs a lesson (or a "Practice Mistakes" session): the mistake-recovery queue, grading,
 * XP + combo multiplier, heart loss, out-of-hearts recovery, and final rewards.
 */
class LessonViewModel(
    savedStateHandle: SavedStateHandle,
    private val curriculum: CurriculumRepository,
    private val statsRepository: UserStatsRepository,
    private val engine: GamificationEngine,
    private val clock: AppClock,
    private val insights: InsightsRepository,
) : ViewModel() {

    private val nodeId: String = checkNotNull(savedStateHandle[ARG_NODE_ID]) { "nodeId argument missing" }
    private val sessionId = SessionId.parse(nodeId)
    private val kind = sessionId.kind

    /** Every non-lesson session is risk-free practice: no hearts lost, no path progress. */
    private val isPractice = kind != SessionKind.LESSON

    private val _state = MutableStateFlow(LessonUiState(isPractice = isPractice, kind = kind))
    val state: StateFlow<LessonUiState> = _state.asStateFlow()

    private var queue = LessonQueue(emptyList())
    private val outcomes = LinkedHashMap<String, Pair<Boolean, Boolean>>() // id -> (firstTry, eventually)
    private val exercisesById = HashMap<String, Exercise>()
    private var lastAnswerCorrect = false
    private var speakingDisabled = false
    private var combo = 0
    private var bestCombo = 0
    private var correctAnswers = 0
    private var totalAnswers = 0
    private var mistakes = 0
    private var speakingCorrect = 0
    private var errorCorrectionsCorrect = 0
    private var sessionXp = 0
    private var heartsTicker: Job? = null
    private var doubleXpUntilMillis = 0L

    init {
        viewModelScope.launch {
            statsRepository.stats.collect { s ->
                doubleXpUntilMillis = s.doubleXpUntilMillis
                _state.update {
                    it.copy(hearts = s.hearts, gems = s.gems, doubleXp = s.isDoubleXpActive(clock.nowMillis()))
                }
            }
        }
        viewModelScope.launch { load() }
    }

    private suspend fun load() {
        val exercises: List<Exercise>
        var title = ""
        var intro: String? = null
        var bridge: GrammarBridge? = null
        var focus: ErrorPattern? = null
        when (val id = sessionId) {
            SessionId.Practice -> exercises = curriculum.buildPracticeSession(clock.todayEpochDay())
            is SessionId.Pattern -> {
                exercises = curriculum.buildPatternSession(id.pattern)
                title = id.pattern.titleAr
                intro = id.pattern.explanationAr
                focus = id.pattern
            }
            is SessionId.Sound -> {
                exercises = SoundLab.buildSession(id.pattern)
                title = id.pattern.titleAr
                intro = SoundLab.drills[id.pattern]?.mouthTipAr ?: id.pattern.explanationAr
                focus = id.pattern
            }
            is SessionId.Lesson -> {
                val node = curriculum.getNode(id.nodeId)
                if (node == null || node.exercises.isEmpty()) {
                    _state.update { it.copy(phase = LessonPhase.ERROR) }
                    return
                }
                exercises = node.exercises
                title = node.titleAr
                intro = node.introTipAr
                bridge = node.grammarBridge
            }
        }
        if (exercises.isEmpty()) {
            _state.update { it.copy(phase = LessonPhase.ERROR) }
            return
        }
        exercises.forEach { exercisesById[it.id] = it }
        queue = LessonQueue(exercises)

        val now = clock.nowMillis()
        val hearts = statsRepository.updateAndGet { s ->
            val regen = engine.regenerateHearts(s, now).stats
            regen to regen.hearts
        }
        val phase = when {
            !isPractice && hearts <= 0 -> LessonPhase.OUT_OF_HEARTS
            isPractice || intro != null || bridge != null -> LessonPhase.INTRO
            else -> LessonPhase.PLAYING
        }
        _state.update {
            it.copy(
                phase = phase,
                titleAr = title,
                introTipAr = intro,
                grammarBridge = bridge,
                focusPattern = focus,
                hearts = hearts,
                exercise = queue.current,
                presentation = 1,
                progress = 0f,
            )
        }
        if (phase == LessonPhase.OUT_OF_HEARTS) startHeartsTicker()
    }

    fun startFromIntro() {
        if (_state.value.phase == LessonPhase.INTRO) _state.update { it.copy(phase = LessonPhase.PLAYING) }
    }

    /** Grades the learner's answer and shows the feedback sheet. */
    fun submit(answer: UserAnswer) {
        val current = _state.value
        val exercise = queue.current ?: return
        if (current.phase != LessonPhase.PLAYING || current.feedback != null) return

        if (!exercise.isGraded) { // flashcards: teaching only, no XP and no hearts
            outcomes[exercise.id] = true to true
            lastAnswerCorrect = true
            queue = queue.answer(correct = true)
            showNext()
            return
        }

        val grade = ExerciseGrader.grade(exercise, answer)
        val firstTry = outcomes[exercise.id]?.first ?: grade.correct
        outcomes[exercise.id] = firstTry to grade.correct
        totalAnswers++
        lastAnswerCorrect = grade.correct
        val now = clock.nowMillis()
        val heard = (answer as? UserAnswer.Speech)?.candidates?.firstOrNull()

        if (grade.correct) {
            combo++
            bestCombo = maxOf(bestCombo, combo)
            correctAnswers++
            if (exercise is SpeakingChallenge) speakingCorrect++
            if (exercise is ErrorCorrection && firstTry) errorCorrectionsCorrect++
            val double = doubleXpUntilMillis > now // evaluated per answer: a boost can expire mid-lesson
            val xp = engine.xpForCorrectAnswer(combo, double)
            val bonus = xp - engine.xpForCorrectAnswer(comboAfterAnswer = 1, doubleXp = double)
            sessionXp += xp
            _state.update {
                it.copy(
                    combo = combo,
                    sessionXp = sessionXp,
                    feedback = AnswerFeedback(true, xp, bonus, combo, null, null, heard),
                )
            }
        } else {
            combo = 0
            mistakes++
            _state.update {
                it.copy(
                    combo = 0,
                    feedback = AnswerFeedback(false, 0, 0, 0, grade.correctAnswer, grade.explanationAr, heard, grade.confusedWith),
                )
            }
            if (!isPractice) {
                viewModelScope.launch {
                    val hearts = statsRepository.updateAndGet { s ->
                        val updated = engine.loseHeart(s, now)
                        updated to updated.hearts
                    }
                    _state.update { it.copy(hearts = hearts) }
                }
            }
        }
    }

    /** "Continue" on the feedback sheet: re-queues a miss (Mistake Recovery Queue) and advances. */
    fun continueAfterFeedback() {
        if (_state.value.feedback == null) return
        queue = queue.answer(lastAnswerCorrect)
        _state.update { it.copy(feedback = null) }
        if (!isPractice && !lastAnswerCorrect && _state.value.hearts <= 0) {
            _state.update { it.copy(phase = LessonPhase.OUT_OF_HEARTS, progress = queue.progress) }
            startHeartsTicker()
            return
        }
        showNext()
    }

    /** "Can't speak right now": skips this and the remaining speaking challenges, without penalty. */
    fun skipSpeaking() {
        if (queue.current !is SpeakingChallenge || _state.value.feedback != null) return
        speakingDisabled = true
        queue = queue.skip()
        showNext()
    }

    private fun showNext() {
        while (speakingDisabled && queue.current is SpeakingChallenge) queue = queue.skip()
        if (queue.isFinished) {
            finish()
            return
        }
        _state.update {
            it.copy(
                phase = LessonPhase.PLAYING,
                exercise = queue.current,
                presentation = it.presentation + 1,
                isRetry = queue.currentIsRetry,
                progress = queue.progress,
            )
        }
    }

    private fun finish() {
        _state.update { it.copy(phase = LessonPhase.SAVING, progress = 1f, exercise = null) }
        viewModelScope.launch {
            val now = clock.nowMillis()
            val today = clock.todayEpochDay()
            val result = LessonResult(
                answerXp = sessionXp,
                correctAnswers = correctAnswers,
                totalAnswers = totalAnswers,
                mistakes = mistakes,
                bestCombo = bestCombo,
                speakingCorrect = speakingCorrect,
                isPractice = isPractice,
                doubleXpActive = doubleXpUntilMillis > now,
                kind = kind,
                errorCorrectionsCorrect = errorCorrectionsCorrect,
            )
            if (kind == SessionKind.LESSON) curriculum.markNodeCompleted(nodeId, result.accuracyPercent, now)
            val completedUnits = SkillPath.completedUnitIds(
                curriculum.observeUnits().first(),
                curriculum.observeCompletedNodeIds().first(),
            )
            val outcome = statsRepository.updateAndGet { s ->
                val o = engine.completeLesson(s, result, today, now, completedUnits)
                o.stats to o
            }
            curriculum.recordOutcomes(
                outcomes = outcomes.mapNotNull { (id, r) ->
                    exercisesById[id]?.let { ExerciseOutcome(it, if (isPractice) null else nodeId, r.first, r.second) }
                },
                isPractice = kind == SessionKind.PRACTICE,
                todayEpochDay = today,
                nowMillis = now,
            )
            val summary = summarize(result, outcome)
            insights.recordSession(
                todayEpochDay = today,
                xp = summary.xpEarned,
                kind = kind,
                patternResults = outcomes.mapNotNull { (id, r) ->
                    exercisesById[id]?.takeIf { it.isGraded }?.pattern?.let { it to r.first }
                },
            )
            _state.update { it.copy(phase = LessonPhase.FINISHED, victory = summary) }
        }
    }

    private fun summarize(result: LessonResult, outcome: Outcome): VictorySummary {
        val events = outcome.events
        return VictorySummary(
            isPractice = isPractice,
            xpEarned = events.filterIsInstance<GamificationEvent.XpGained>().sumOf { it.amount },
            accuracyPercent = result.accuracyPercent,
            isPerfect = result.isPerfect,
            streak = outcome.stats.currentStreak,
            streakExtended = events.any { it is GamificationEvent.StreakExtended },
            freezeUsed = events.any { it is GamificationEvent.StreakFreezeUsed },
            gemsEarned = events.filterIsInstance<GamificationEvent.GemsGained>().sumOf { it.amount },
            heartsGained = events.filterIsInstance<GamificationEvent.HeartsGained>().sumOf { it.amount },
            newLevel = events.filterIsInstance<GamificationEvent.LevelUp>().lastOrNull()?.newLevel,
            dailyGoalReached = events.any { it is GamificationEvent.DailyGoalReached },
            badges = events.filterIsInstance<GamificationEvent.BadgeUnlocked>().map { it.badge },
            kind = kind,
            ramadanCrescents = events.filterIsInstance<GamificationEvent.RamadanCrescentLit>().lastOrNull()?.litCount,
        )
    }

    // ------------------------------------------------------------------ out of hearts

    fun refillHeartsWithGems() {
        viewModelScope.launch {
            val result = statsRepository.updateAndGet { s ->
                when (val r = engine.purchase(s, ShopItem.HEART_REFILL, clock.nowMillis())) {
                    is PurchaseResult.Success -> r.outcome.stats to r
                    else -> s to r
                }
            }
            when (result) {
                is PurchaseResult.Success -> resume(hearts = UserStats.MAX_HEARTS)
                is PurchaseResult.NotEnoughGems ->
                    _state.update { it.copy(message = UiMessage.Res(R.string.not_enough_gems, listOf(result.missing))) }
                is PurchaseResult.NotNeeded -> resume(hearts = _state.value.hearts)
            }
        }
    }

    /** Continue once at least one heart has regenerated. */
    fun resumeWithRegeneratedHearts() {
        if (_state.value.hearts > 0) resume(_state.value.hearts)
    }

    private fun resume(hearts: Int) {
        heartsTicker?.cancel()
        _state.update { it.copy(hearts = hearts, nextHeartMillis = null) }
        showNext()
    }

    fun messageShown() = _state.update { it.copy(message = null) }

    private fun startHeartsTicker() {
        heartsTicker?.cancel()
        heartsTicker = viewModelScope.launch {
            while (isActive && _state.value.phase == LessonPhase.OUT_OF_HEARTS) {
                val now = clock.nowMillis()
                val stats = statsRepository.updateAndGet { s ->
                    val regen = engine.regenerateHearts(s, now).stats
                    regen to regen
                }
                _state.update { it.copy(hearts = stats.hearts, nextHeartMillis = engine.millisUntilNextHeart(stats, now)) }
                delay(1_000)
            }
        }
    }

    companion object {
        const val ARG_NODE_ID = "nodeId"
        const val PRACTICE_ID = "practice"

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val container = (this[APPLICATION_KEY] as RihlaApplication).container
                LessonViewModel(
                    savedStateHandle = createSavedStateHandle(),
                    curriculum = container.curriculumRepository,
                    statsRepository = container.userStatsRepository,
                    engine = container.gamificationEngine,
                    clock = container.clock,
                    insights = container.insightsRepository,
                )
            }
        }
    }
}
