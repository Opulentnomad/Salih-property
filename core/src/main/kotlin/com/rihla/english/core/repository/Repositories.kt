package com.rihla.english.core.repository

import com.rihla.english.core.model.CourseUnit
import com.rihla.english.core.model.ErrorPattern
import com.rihla.english.core.model.LearningInsights
import com.rihla.english.core.model.SessionKind
import com.rihla.english.core.model.UserProfile
import com.rihla.english.core.model.Exercise
import com.rihla.english.core.model.PathNode
import com.rihla.english.core.model.UserStats
import kotlinx.coroutines.flow.Flow

/** How one exercise went during a session; feeds SM-2 and the mistakes store. */
data class ExerciseOutcome(
    val exercise: Exercise,
    val nodeId: String?,
    val firstTryCorrect: Boolean,
    val eventuallyCorrect: Boolean,
)

/** Curriculum content + learning progress (Room in the app module). */
interface CurriculumRepository {
    /** Inserts/updates the seed curriculum if the stored version is older. Keeps progress. */
    suspend fun ensureSeeded()

    /** Units with their nodes (exercises are not loaded here). */
    fun observeUnits(): Flow<List<CourseUnit>>

    fun observeCompletedNodeIds(): Flow<Set<String>>

    /** Number of vocabulary items the learner has started learning. */
    fun observeWordsLearned(): Flow<Int>

    /** Number of exercises waiting in the "practice mistakes" pool. */
    fun observeMistakeCount(): Flow<Int>

    /** A lesson node with its exercises, or null if unknown. */
    suspend fun getNode(nodeId: String): PathNode?

    suspend fun markNodeCompleted(nodeId: String, accuracyPercent: Int, nowMillis: Long)

    /** Builds a review session from past mistakes and SM-2 due vocabulary. */
    suspend fun buildPracticeSession(todayEpochDay: Long, maxExercises: Int = 10): List<Exercise>

    /** Exercises that train one mistake pattern, preferring lessons the learner has completed. */
    suspend fun buildPatternSession(pattern: ErrorPattern, maxExercises: Int = 10): List<Exercise>

    suspend fun recordOutcomes(outcomes: List<ExerciseOutcome>, isPractice: Boolean, todayEpochDay: Long, nowMillis: Long)
}

/** Gamification stats (Jetpack DataStore in the app module). */
interface UserStatsRepository {
    val stats: Flow<UserStats>

    /** Atomically transforms the stats and returns a value computed alongside. */
    suspend fun <R> updateAndGet(transform: (UserStats) -> Pair<UserStats, R>): R

    suspend fun update(transform: (UserStats) -> UserStats) {
        updateAndGet { transform(it) to Unit }
    }
}

/** Learning analytics: mistake profile, sound mastery, daily activity and story progress. */
interface InsightsRepository {
    val insights: Flow<LearningInsights>

    /**
     * Records a finished session: XP for the day, the session kind, and first-try results per
     * mistake pattern (true = correct on the first attempt).
     */
    suspend fun recordSession(
        todayEpochDay: Long,
        xp: Int,
        kind: SessionKind,
        patternResults: List<Pair<ErrorPattern, Boolean>>,
    )

    suspend fun recordStory(todayEpochDay: Long, storyId: String, stars: Int, xp: Int)
}

/** Learner profile from onboarding (name, goal, reminder time). */
interface ProfileRepository {
    val profile: Flow<UserProfile>
    suspend fun update(transform: (UserProfile) -> UserProfile)
}
