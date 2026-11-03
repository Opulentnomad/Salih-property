package com.rihla.english.core.model

import kotlinx.serialization.Serializable

enum class LearningGoal(val titleAr: String) {
    TRAVEL("السفر والسياحة"),
    WORK("العمل والوظيفة"),
    STUDY("الدراسة والابتعاث"),
    DAILY_LIFE("الحياة اليومية"),
    KIDS("أساعد أطفالي في دراستهم"),
    FUN("تحدٍّ شخصي ومتعة"),
}

/** Daily time the learner commits to (no points, just minutes). */
enum class DailyGoalLevel(val titleAr: String, val minutes: Int) {
    CASUAL("خفيف", 5),
    REGULAR("منتظم", 10),
    SERIOUS("جاد", 15),
    INTENSE("مكثّف", 20),
}

@Serializable
data class UserProfile(
    val name: String = "",
    val goal: LearningGoal? = null,
    val dailyMinutes: Int = DailyGoalLevel.REGULAR.minutes,
    val onboarded: Boolean = false,
    val remindersEnabled: Boolean = true,
    val reminderHour: Int = 20,
    val reminderMinute: Int = 0,
)
