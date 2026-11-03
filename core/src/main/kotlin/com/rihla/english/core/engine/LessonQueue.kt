package com.rihla.english.core.engine

import com.rihla.english.core.model.Exercise

/**
 * Mistake Recovery Queue: an immutable queue of exercises. A missed exercise is moved to the
 * back so the lesson only ends once every item has been answered correctly.
 */
data class LessonQueue(
    val pending: List<Exercise>,
    val masteredCount: Int = 0,
    /** Ids of exercises that were missed at least once. */
    val missedIds: Set<String> = emptySet(),
) {
    val current: Exercise? get() = pending.firstOrNull()
    val isFinished: Boolean get() = pending.isEmpty()
    val totalUnique: Int get() = masteredCount + pending.size

    /** 0..1, only grows when an item is mastered. */
    val progress: Float get() = if (totalUnique == 0) 1f else masteredCount.toFloat() / totalUnique

    /** True when the current exercise is a recovery round of an earlier mistake. */
    val currentIsRetry: Boolean get() = current?.id in missedIds

    fun answer(correct: Boolean): LessonQueue {
        val head = current ?: return this
        val rest = pending.drop(1)
        return if (correct) {
            copy(pending = rest, masteredCount = masteredCount + 1)
        } else {
            copy(pending = rest + head, missedIds = missedIds + head.id)
        }
    }

    /** Removes the current exercise without grading it (e.g. "Can't speak right now"). */
    fun skip(): LessonQueue = if (pending.isEmpty()) this else copy(pending = pending.drop(1), masteredCount = masteredCount + 1)
}
