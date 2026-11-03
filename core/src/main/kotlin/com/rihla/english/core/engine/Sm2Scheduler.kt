package com.rihla.english.core.engine

import kotlin.math.max
import kotlin.math.roundToInt

/** Spaced-repetition state of one vocabulary item. */
data class Sm2State(
    val easiness: Double = 2.5,
    val intervalDays: Int = 0,
    val repetitions: Int = 0,
    val dueEpochDay: Long = 0,
    val lastReviewedEpochDay: Long = 0,
    val timesCorrect: Int = 0,
    val timesWrong: Int = 0,
) {
    /** 0..5 strength used for the "word strength" UI. */
    val strength: Int get() = repetitions.coerceIn(0, 5)
}

/** Classic SuperMemo-2 algorithm. */
object Sm2Scheduler {
    const val MIN_EASINESS = 1.3

    /**
     * Maps how an item was answered in a lesson to an SM-2 quality grade (0..5):
     * right first time = 5, right after a retry = 3, never right = 1.
     */
    fun qualityFor(firstTryCorrect: Boolean, eventuallyCorrect: Boolean): Int = when {
        firstTryCorrect -> 5
        eventuallyCorrect -> 3
        else -> 1
    }

    fun review(state: Sm2State, quality: Int, todayEpochDay: Long): Sm2State {
        val q = quality.coerceIn(0, 5)
        val easiness = max(
            MIN_EASINESS,
            state.easiness + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02)),
        )
        val (repetitions, interval) = if (q < 3) {
            0 to 1
        } else {
            val reps = state.repetitions + 1
            reps to when (reps) {
                1 -> 1
                2 -> 6
                else -> (state.intervalDays * state.easiness).roundToInt().coerceAtLeast(1)
            }
        }
        return state.copy(
            easiness = easiness,
            intervalDays = interval,
            repetitions = repetitions,
            dueEpochDay = todayEpochDay + interval,
            lastReviewedEpochDay = todayEpochDay,
            timesCorrect = state.timesCorrect + if (q >= 3) 1 else 0,
            timesWrong = state.timesWrong + if (q < 3) 1 else 0,
        )
    }
}
