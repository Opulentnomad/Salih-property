package com.rihla.english.core.engine

import kotlin.math.max
import kotlin.math.min

/** Normalisation and fuzzy matching for typed/assembled sentences and speech recognition. */
object AnswerMatcher {

    private val contractions = mapOf(
        "i'm" to "i am", "you're" to "you are", "he's" to "he is", "she's" to "she is",
        "it's" to "it is", "we're" to "we are", "they're" to "they are",
        "isn't" to "is not", "aren't" to "are not", "don't" to "do not",
        "doesn't" to "does not", "can't" to "can not", "cannot" to "can not",
        "what's" to "what is", "that's" to "that is",
    )

    private val digitWords = mapOf(
        "0" to "zero", "1" to "one", "2" to "two", "3" to "three", "4" to "four", "5" to "five",
        "6" to "six", "7" to "seven", "8" to "eight", "9" to "nine", "10" to "ten",
    )

    /** Lower-cases, strips punctuation, expands contractions and spells out digits. */
    fun normalize(text: String): String {
        val cleaned = text.lowercase()
            .replace('’', '\'')
            .replace('‘', '\'')
            .replace(Regex("[^a-z0-9' ]"), " ")
        return cleaned.split(' ')
            .filter { it.isNotBlank() }
            .map { word -> contractions[word] ?: digitWords[word] ?: word }
            .joinToString(" ")
            .trim()
    }

    fun sentenceMatches(given: String, accepted: List<String>): Boolean {
        val n = normalize(given)
        return accepted.any { normalize(it) == n }
    }

    fun levenshtein(a: String, b: String): Int {
        if (a == b) return 0
        if (a.isEmpty()) return b.length
        if (b.isEmpty()) return a.length
        var prev = IntArray(b.length + 1) { it }
        var curr = IntArray(b.length + 1)
        for (i in 1..a.length) {
            curr[0] = i
            for (j in 1..b.length) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                curr[j] = min(min(curr[j - 1] + 1, prev[j] + 1), prev[j - 1] + cost)
            }
            val tmp = prev; prev = curr; curr = tmp
        }
        return prev[b.length]
    }

    /** Similarity in 0..1 between two normalised strings. */
    fun similarity(a: String, b: String): Double {
        val x = normalize(a)
        val y = normalize(b)
        val longest = max(x.length, y.length)
        if (longest == 0) return 1.0
        return 1.0 - levenshtein(x, y).toDouble() / longest
    }

    /** Best similarity of any recogniser hypothesis against any accepted target. */
    fun pronunciationScore(candidates: List<String>, targets: List<String>): Double =
        candidates.maxOfOrNull { c -> targets.maxOf { t -> similarity(c, t) } } ?: 0.0

    const val PRONUNCIATION_THRESHOLD = 0.75

    fun pronunciationAccepted(candidates: List<String>, targets: List<String>): Boolean =
        pronunciationScore(candidates, targets) >= PRONUNCIATION_THRESHOLD
}
