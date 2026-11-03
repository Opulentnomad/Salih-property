package com.rihla.english.core.model

/** What kind of learning session is running. Only LESSON costs hearts and advances the path. */
enum class SessionKind { LESSON, PRACTICE, PATTERN, SOUND }

/** Route-friendly identifier of a session: "u1_l1", "practice", "pattern_MISSING_BE", "sound_SOUND_P_B". */
sealed interface SessionId {
    val raw: String
    val kind: SessionKind

    data class Lesson(val nodeId: String) : SessionId {
        override val raw get() = nodeId
        override val kind get() = SessionKind.LESSON
    }

    data object Practice : SessionId {
        override val raw get() = "practice"
        override val kind get() = SessionKind.PRACTICE
    }

    data class Pattern(val pattern: ErrorPattern) : SessionId {
        override val raw get() = "pattern_${pattern.name}"
        override val kind get() = SessionKind.PATTERN
    }

    data class Sound(val pattern: ErrorPattern) : SessionId {
        override val raw get() = "sound_${pattern.name}"
        override val kind get() = SessionKind.SOUND
    }

    companion object {
        fun parse(raw: String): SessionId = when {
            raw == "practice" -> Practice
            raw.startsWith("pattern_") -> ErrorPattern.entries.firstOrNull { it.name == raw.removePrefix("pattern_") }
                ?.let { Pattern(it) } ?: Practice
            raw.startsWith("sound_") -> ErrorPattern.entries.firstOrNull { it.name == raw.removePrefix("sound_") }
                ?.let { Sound(it) } ?: Practice
            else -> Lesson(raw)
        }
    }
}
