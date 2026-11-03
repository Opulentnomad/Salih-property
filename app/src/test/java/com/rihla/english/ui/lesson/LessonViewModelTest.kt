package com.rihla.english.ui.lesson

import androidx.lifecycle.SavedStateHandle
import com.rihla.english.core.curriculum.SeedCurriculumData
import com.rihla.english.core.engine.GamificationEngine
import com.rihla.english.core.engine.UserAnswer
import com.rihla.english.core.model.CourseUnit
import com.rihla.english.core.model.Exercise
import com.rihla.english.core.model.FlashcardAudio
import com.rihla.english.core.model.ListeningChallenge
import com.rihla.english.core.model.ListeningMode
import com.rihla.english.core.model.MatchPairs
import com.rihla.english.core.model.MultipleChoice
import com.rihla.english.core.model.PathNode
import com.rihla.english.core.model.SpeakingChallenge
import com.rihla.english.core.model.UserStats
import com.rihla.english.core.model.WordBankSentence
import com.rihla.english.core.repository.CurriculumRepository
import com.rihla.english.core.repository.ExerciseOutcome
import com.rihla.english.core.repository.UserStatsRepository
import com.rihla.english.util.AppClock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LessonViewModelTest {

    private lateinit var curriculum: FakeCurriculumRepository
    private lateinit var stats: FakeStatsRepository
    private val engine = GamificationEngine()

    @Before fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        curriculum = FakeCurriculumRepository()
        stats = FakeStatsRepository(UserStats())
    }

    @After fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(nodeId: String = "u1_l1") = LessonViewModel(
        SavedStateHandle(mapOf(LessonViewModel.ARG_NODE_ID to nodeId)), curriculum, stats, engine, FixedClock,
    )

    private fun correctAnswer(e: Exercise): UserAnswer = when (e) {
        is FlashcardAudio -> UserAnswer.Acknowledged
        is MultipleChoice -> UserAnswer.Choice(e.correctIndex)
        is WordBankSentence -> UserAnswer.Tokens(e.answerTokens)
        is MatchPairs -> UserAnswer.MatchCompleted(0)
        is ListeningChallenge -> if (e.mode == ListeningMode.SELECT) UserAnswer.Choice(e.correctIndex) else UserAnswer.Tokens(e.answerTokens)
        is SpeakingChallenge -> UserAnswer.Speech(listOf(e.targetText))
    }

    private fun wrongAnswer(e: Exercise): UserAnswer = when (e) {
        is MultipleChoice -> UserAnswer.Choice((e.correctIndex + 1) % e.options.size)
        is MatchPairs -> UserAnswer.MatchCompleted(2)
        else -> UserAnswer.Tokens(listOf("definitely", "wrong"))
    }

    /** Answers until the lesson leaves the PLAYING phase; returns the number of answers given. */
    private fun LessonViewModel.playToEnd(answer: (Exercise) -> UserAnswer = ::correctAnswer): Int {
        var steps = 0
        while (state.value.phase == LessonPhase.PLAYING && steps < 500) {
            val e = state.value.exercise!!
            submit(answer(e))
            if (state.value.feedback != null) continueAfterFeedback()
            steps++
        }
        return steps
    }

    @Test fun `perfect lesson finishes with combo xp, completion bonus and rewards`() = runTest {
        val vm = viewModel()
        assertEquals(LessonPhase.INTRO, vm.state.value.phase) // u1_l1 has an intro tip
        vm.startFromIntro()
        vm.playToEnd()

        val s = vm.state.value
        assertEquals(LessonPhase.FINISHED, s.phase)
        val victory = assertNotNull(s.victory).let { s.victory!! }
        assertTrue(victory.isPerfect)
        assertEquals(100, victory.accuracyPercent)
        // 6 graded exercises: 10 + 10 + 15 + 15 + 15 + 15 = 80, plus 15 completion + 5 perfect.
        assertEquals(100, victory.xpEarned)
        assertTrue("u1_l1" in curriculum.completed.value)
        assertEquals(1, stats.state.value.lessonsCompleted)
        assertEquals(1, stats.state.value.currentStreak) // 100 XP > 30 XP daily goal
        assertTrue(victory.streakExtended)
        assertEquals(UserStats.MAX_HEARTS, stats.state.value.hearts)
    }

    @Test fun `missed exercise costs a heart and is re-queued until mastered`() = runTest {
        val vm = viewModel()
        vm.startFromIntro()
        // Skip past flashcards to the first graded exercise.
        while (vm.state.value.exercise is FlashcardAudio) vm.submit(UserAnswer.Acknowledged)
        val missed = vm.state.value.exercise!!
        vm.submit(wrongAnswer(missed))
        assertEquals(false, vm.state.value.feedback?.correct)
        assertEquals(UserStats.MAX_HEARTS - 1, stats.state.value.hearts)
        vm.continueAfterFeedback()

        var sawRetry = false
        while (vm.state.value.phase == LessonPhase.PLAYING) {
            val e = vm.state.value.exercise!!
            if (e.id == missed.id) {
                assertTrue(vm.state.value.isRetry)
                sawRetry = true
            }
            vm.submit(correctAnswer(e))
            if (vm.state.value.feedback != null) vm.continueAfterFeedback()
        }
        assertTrue(sawRetry)
        assertEquals(LessonPhase.FINISHED, vm.state.value.phase)
        assertEquals(false, vm.state.value.victory!!.isPerfect)
        assertTrue(curriculum.recorded.any { it.exercise.id == missed.id && !it.firstTryCorrect && it.eventuallyCorrect })
    }

    @Test fun `running out of hearts pauses the lesson until refilled with gems`() = runTest {
        stats.state.value = UserStats(hearts = 1, gems = 100)
        val vm = viewModel("u1_l2") // no intro
        assertEquals(LessonPhase.PLAYING, vm.state.value.phase)
        while (vm.state.value.exercise is FlashcardAudio) vm.submit(UserAnswer.Acknowledged)
        vm.submit(wrongAnswer(vm.state.value.exercise!!))
        vm.continueAfterFeedback()
        assertEquals(LessonPhase.OUT_OF_HEARTS, vm.state.value.phase)

        vm.refillHeartsWithGems()
        assertEquals(LessonPhase.PLAYING, vm.state.value.phase)
        assertEquals(UserStats.MAX_HEARTS, stats.state.value.hearts)
        assertEquals(60, stats.state.value.gems)
        vm.playToEnd()
        assertEquals(LessonPhase.FINISHED, vm.state.value.phase)
    }

    // Not runTest: the out-of-hearts countdown ticks forever in virtual time while hearts are 0.
    @Test fun `cannot start a lesson with zero hearts`() {
        stats.state.value = UserStats(hearts = 0, heartRegenStartMillis = FixedClock.nowMillis())
        val vm = viewModel("u1_l2")
        assertEquals(LessonPhase.OUT_OF_HEARTS, vm.state.value.phase)
        assertNotNull(vm.state.value.nextHeartMillis)
    }

    @Test fun `skipping speaking removes remaining speaking challenges without penalty`() = runTest {
        val vm = viewModel("u1_l5")
        vm.startFromIntro()
        var skipped = false
        var steps = 0
        while (vm.state.value.phase == LessonPhase.PLAYING && steps++ < 200) {
            val e = vm.state.value.exercise!!
            if (e is SpeakingChallenge) {
                assertTrue("speaking shown again after skip", !skipped)
                vm.skipSpeaking()
                skipped = true
                continue
            }
            vm.submit(correctAnswer(e))
            if (vm.state.value.feedback != null) vm.continueAfterFeedback()
        }
        assertTrue(skipped)
        assertEquals(LessonPhase.FINISHED, vm.state.value.phase)
        assertTrue(vm.state.value.victory!!.isPerfect)
    }

    @Test fun `practice session never costs hearts and restores one`() = runTest {
        stats.state.value = UserStats(hearts = 2, heartRegenStartMillis = FixedClock.nowMillis())
        val vm = viewModel(LessonViewModel.PRACTICE_ID)
        vm.startFromIntro()
        val first = vm.state.value.exercise!!
        vm.submit(wrongAnswer(first))
        vm.continueAfterFeedback()
        assertEquals(2, stats.state.value.hearts)
        vm.playToEnd()
        assertEquals(LessonPhase.FINISHED, vm.state.value.phase)
        assertEquals(3, stats.state.value.hearts)
        assertEquals(1, stats.state.value.practiceSessions)
    }
}

private object FixedClock : AppClock {
    override fun nowMillis(): Long = 1_750_000_000_000L
    override fun todayEpochDay(): Long = 20_250L
    override fun fractionOfDay(): Double = 0.5
}

private class FakeStatsRepository(initial: UserStats) : UserStatsRepository {
    val state = MutableStateFlow(initial)
    override val stats: Flow<UserStats> = state
    override suspend fun <R> updateAndGet(transform: (UserStats) -> Pair<UserStats, R>): R {
        val (updated, result) = transform(state.value)
        state.value = updated
        return result
    }
}

private class FakeCurriculumRepository : CurriculumRepository {
    val completed = MutableStateFlow<Set<String>>(emptySet())
    val recorded = mutableListOf<ExerciseOutcome>()
    private val nodes: List<PathNode> = SeedCurriculumData.units.flatMap { it.nodes }

    override suspend fun ensureSeeded() = Unit
    override fun observeUnits(): Flow<List<CourseUnit>> = flowOf(SeedCurriculumData.units)
    override fun observeCompletedNodeIds(): Flow<Set<String>> = completed
    override fun observeWordsLearned(): Flow<Int> = flowOf(0)
    override fun observeMistakeCount(): Flow<Int> = flowOf(0)
    override suspend fun getNode(nodeId: String): PathNode? = nodes.firstOrNull { it.id == nodeId }
    override suspend fun markNodeCompleted(nodeId: String, accuracyPercent: Int, nowMillis: Long) {
        completed.value = completed.value + nodeId
    }
    override suspend fun buildPracticeSession(todayEpochDay: Long, maxExercises: Int): List<Exercise> =
        nodes.first().exercises.filter { it.isGraded && it !is SpeakingChallenge }.take(maxExercises)
    override suspend fun recordOutcomes(outcomes: List<ExerciseOutcome>, isPractice: Boolean, todayEpochDay: Long, nowMillis: Long) {
        recorded += outcomes
    }
}
