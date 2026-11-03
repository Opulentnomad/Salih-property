package com.rihla.english.core.season

import java.time.LocalDate
import java.time.Month
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField

/** Local seasons that change the app's look, content and rules. */
enum class Season(
    val titleAr: String,
    val emoji: String,
    val colorHex: Long,
    val messageAr: String,
    /** Missed days in this season never break the streak. */
    val protectsStreak: Boolean,
) {
    RAMADAN(
        "تحدّي رمضان", "🌙", 0xFF4C3F91,
        "رمضان كريم! حقق هدفك اليومي كل يوم لتضيء هلالًا جديدًا. درس قصير بعد الإفطار يكفي ✨",
        protectsStreak = false,
    ),
    EID_AL_FITR(
        "عيد الفطر", "🎉", 0xFF0F9D58,
        "عيد مبارك! سلسلتك محمية طوال أيام العيد، استمتع مع أهلك 🎈",
        protectsStreak = true,
    ),
    EID_AL_ADHA(
        "عيد الأضحى", "🐑", 0xFF0F9D58,
        "عيد أضحى مبارك! سلسلتك محمية طوال أيام العيد 🎈",
        protectsStreak = true,
    ),
    NATIONAL_DAY(
        "اليوم الوطني", "🇸🇦", 0xFF006C35,
        "كل عام والوطن بخير! أكمل درسًا هذا الأسبوع لتحصل على وسام «فخر الوطن» 💚",
        protectsStreak = false,
    ),
    FOUNDING_DAY(
        "يوم التأسيس", "🏰", 0xFF8B5E34,
        "يوم بدينا! احتفل بيوم التأسيس بدرس جديد اليوم 🌴",
        protectsStreak = false,
    ),
}

/**
 * Season detection. Hijri dates use the JDK's Umm al-Qura calendar (HijrahDate), which can
 * differ by a day from moon sighting; the Eid window is widened by a day to stay fair.
 */
object SeasonCalendar {

    fun seasonOn(date: LocalDate): Season? {
        hijri(date)?.let { (month, day) ->
            when {
                month == 9 -> return Season.RAMADAN
                month == 10 && day <= 4 -> return Season.EID_AL_FITR
                month == 12 && day in 9..13 -> return Season.EID_AL_ADHA
            }
        }
        if (date.month == Month.SEPTEMBER && date.dayOfMonth in 20..25) return Season.NATIONAL_DAY
        if (date.month == Month.FEBRUARY && date.dayOfMonth in 20..23) return Season.FOUNDING_DAY
        return null
    }

    fun seasonOn(epochDay: Long): Season? = seasonOn(LocalDate.ofEpochDay(epochDay))

    fun isStreakProtected(epochDay: Long): Boolean = seasonOn(epochDay)?.protectsStreak == true

    /** The Hijri year if [epochDay] is in Ramadan, else null. */
    fun ramadanYearOf(epochDay: Long): Int? {
        val date = LocalDate.ofEpochDay(epochDay)
        val h = runCatching { HijrahDate.from(date) }.getOrNull() ?: return null
        return if (h.get(ChronoField.MONTH_OF_YEAR) == 9) h.get(ChronoField.YEAR) else null
    }

    /** Day number (1–30) within Ramadan, or null outside Ramadan. */
    fun ramadanDay(epochDay: Long): Int? {
        val (month, day) = hijri(LocalDate.ofEpochDay(epochDay)) ?: return null
        return if (month == 9) day else null
    }

    private fun hijri(date: LocalDate): Pair<Int, Int>? = runCatching {
        val h = HijrahDate.from(date)
        h.get(ChronoField.MONTH_OF_YEAR) to h.get(ChronoField.DAY_OF_MONTH)
    }.getOrNull()
}
