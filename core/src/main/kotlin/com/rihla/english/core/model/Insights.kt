package com.rihla.english.core.model

import kotlinx.serialization.Serializable

/** How the learner is doing on one mistake pattern / sound. */
@Serializable
data class PatternStat(
    val attempts: Int = 0,
    val misses: Int = 0,
    val lastPracticedDay: Long = 0,
) {
    val accuracyPercent: Int get() = if (attempts == 0) 0 else ((attempts - misses) * 100) / attempts
}

/** What happened on one calendar day (for the weekly chart, calendar and daily plan). */
@Serializable
data class DayActivity(
    val xp: Int = 0,
    val lessons: Int = 0,
    val practices: Int = 0,
    val soundDrills: Int = 0,
    val stories: Int = 0,
)

@Serializable
data class StoryProgress(val bestStars: Int = 0, val timesCompleted: Int = 0)

/** Long-term learning analytics (stored as JSON in DataStore by the app). */
@Serializable
data class LearningInsights(
    val patterns: Map<String, PatternStat> = emptyMap(),
    val days: Map<Long, DayActivity> = emptyMap(),
    val stories: Map<String, StoryProgress> = emptyMap(),
) {
    fun day(epochDay: Long): DayActivity = days[epochDay] ?: DayActivity()
    fun pattern(p: ErrorPattern): PatternStat = patterns[p.name] ?: PatternStat()

    /** Weakest patterns first (needs a few attempts before judging). */
    fun weakestPatterns(category: PatternCategory, limit: Int = 3): List<Pair<ErrorPattern, PatternStat>> =
        ErrorPattern.entries.filter { it.category == category }
            .map { it to pattern(it) }
            .filter { (_, s) -> s.attempts >= 3 && s.misses > 0 }
            .sortedBy { (_, s) -> s.accuracyPercent }
            .take(limit)

    /** XP for the last [count] days, oldest first. */
    fun xpHistory(todayEpochDay: Long, count: Int = 7): List<Pair<Long, Int>> =
        ((todayEpochDay - count + 1)..todayEpochDay).map { it to day(it).xp }

    companion object {
        const val DAYS_TO_KEEP = 120
    }
}

enum class LearningGoal(val emoji: String, val titleAr: String) {
    TRAVEL("✈️", "السفر والسياحة"),
    WORK("💼", "العمل والوظيفة"),
    STUDY("🎓", "الدراسة والابتعاث"),
    DAILY_LIFE("🏠", "الحياة اليومية"),
    KIDS("👨‍👩‍👧", "أساعد أطفالي في دراستهم"),
    FUN("🎯", "تحدٍّ شخصي ومتعة"),
}

enum class DailyGoalLevel(val emoji: String, val titleAr: String, val minutesAr: String, val xp: Int) {
    CASUAL("🌱", "خفيف", "٥ دقائق يوميًا", 10),
    REGULAR("🚶", "منتظم", "١٠ دقائق يوميًا", 30),
    SERIOUS("🏃", "جاد", "١٥ دقيقة يوميًا", 50),
    INTENSE("🚀", "مكثّف", "٢٠ دقيقة يوميًا", 80),
}

@Serializable
data class UserProfile(
    val name: String = "",
    val goal: LearningGoal? = null,
    val onboarded: Boolean = false,
    val remindersEnabled: Boolean = true,
    val reminderHour: Int = 20,
    val reminderMinute: Int = 0,
)

enum class DailyTaskKind(val emoji: String, val titleAr: String) {
    LESSON("📘", "أكمل درسًا جديدًا"),
    PRACTICE("🎯", "تدرّب على أخطائك أو نطقك"),
    STORY("💬", "عِش موقفًا من حياتك"),
}

data class DailyTask(val kind: DailyTaskKind, val done: Boolean)

/** "خطة اليوم": three small tasks; finishing all of them unlocks a daily bonus chest. */
object DailyPlan {
    const val BONUS_GEMS = 15

    fun tasksFor(insights: LearningInsights, todayEpochDay: Long): List<DailyTask> {
        val d = insights.day(todayEpochDay)
        return listOf(
            DailyTask(DailyTaskKind.LESSON, d.lessons > 0),
            DailyTask(DailyTaskKind.PRACTICE, d.practices + d.soundDrills > 0),
            DailyTask(DailyTaskKind.STORY, d.stories > 0),
        )
    }
}
