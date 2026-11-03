package com.rihla.english.core.model

/**
 * Snapshot of everything the gamification layer tracks. Persisted in Jetpack
 * DataStore by the app module. Dates are stored as epoch days (LocalDate.toEpochDay)
 * and instants as epoch millis so the core stays free of platform types.
 */
data class UserStats(
    val totalXp: Int = 0,
    val level: Int = 1,
    val dailyXp: Int = 0,
    val dailyXpEpochDay: Long = NO_DAY,
    val dailyGoalXp: Int = DEFAULT_DAILY_GOAL,

    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    /** Last day on which the daily XP goal was reached (or bridged by a freeze). */
    val lastGoalEpochDay: Long = NO_DAY,
    val streakFreezes: Int = 0,

    val hearts: Int = MAX_HEARTS,
    /** When the current heart-regeneration timer started (0 = hearts are full). */
    val heartRegenStartMillis: Long = 0L,

    val gems: Int = STARTING_GEMS,
    /** Last day any lesson was completed. */
    val lastCompletedEpochDay: Long = NO_DAY,
    val doubleXpUntilMillis: Long = 0L,

    val weeklyXp: Int = 0,
    val weekStartEpochDay: Long = NO_DAY,
    val league: LeagueTier = LeagueTier.BRONZE,
    val lastLeagueOutcome: LeagueOutcome? = null,

    val lessonsCompleted: Int = 0,
    val perfectLessons: Int = 0,
    val chestsOpened: Int = 0,
    val practiceSessions: Int = 0,
    val speakingCorrect: Int = 0,
    val bestCombo: Int = 0,
    val unlockedBadges: Set<String> = emptySet(),

    val storiesCompleted: Int = 0,
    val perfectStories: Int = 0,
    val soundDrills: Int = 0,
    val errorCorrectionsCorrect: Int = 0,
    /** Ramadan challenge: days the goal was reached during Ramadan of [ramadanHijriYear]. */
    val ramadanGoalDays: Int = 0,
    val ramadanHijriYear: Int = 0,
    val celebratedNationalDay: Boolean = false,
    val dailyBonusClaimedDay: Long = NO_DAY,
) {
    fun isDoubleXpActive(nowMillis: Long) = doubleXpUntilMillis > nowMillis
    val dailyGoalReached: Boolean get() = dailyXp >= dailyGoalXp

    companion object {
        const val NO_DAY = Long.MIN_VALUE
        const val MAX_HEARTS = 5
        const val MAX_STREAK_FREEZES = 2
        const val DEFAULT_DAILY_GOAL = 30
        const val STARTING_GEMS = 100
    }
}

enum class LeagueTier(val titleAr: String, val emoji: String, val colorHex: Long) {
    BRONZE("الدوري البرونزي", "🥉", 0xFFCD7F32),
    SILVER("الدوري الفضي", "🥈", 0xFFA8B2BD),
    GOLD("الدوري الذهبي", "🥇", 0xFFFFC800),
    DIAMOND("الدوري الماسي", "💎", 0xFF5AC8FA);

    val next: LeagueTier get() = entries.getOrElse(ordinal + 1) { this }
    val previous: LeagueTier get() = entries.getOrElse(ordinal - 1) { this }
}

enum class LeagueOutcome { PROMOTED, STAYED, DEMOTED }

/** Items purchasable with gems. */
enum class ShopItem(
    val titleAr: String,
    val descriptionAr: String,
    val emoji: String,
    val priceGems: Int,
) {
    STREAK_FREEZE(
        titleAr = "تجميد السلسلة",
        descriptionAr = "يحمي سلسلتك تلقائيًا إذا فاتك يوم واحد. يمكنك امتلاك ٢ كحد أقصى.",
        emoji = "❄️",
        priceGems = 50,
    ),
    HEART_REFILL(
        titleAr = "إعادة ملء القلوب",
        descriptionAr = "استعد كل قلوبك الخمسة فورًا وواصل التعلم.",
        emoji = "❤️",
        priceGems = 40,
    ),
    DOUBLE_XP_BOOST(
        titleAr = "مضاعف النقاط ×٢",
        descriptionAr = "ضاعف نقاط الخبرة (XP) لمدة ١٥ دقيقة.",
        emoji = "⚡",
        priceGems = 60,
    ),
}

/** Unlockable achievement badge. Conditions are evaluated by the GamificationEngine. */
enum class Badge(val titleAr: String, val descriptionAr: String, val emoji: String) {
    FIRST_LESSON("أول درس", "أكمل درسك الأول", "🎓"),
    STREAK_3("سلسلة ٣ أيام", "حقق هدفك اليومي ٣ أيام متتالية", "✨"),
    STREAK_7("سلسلة 7 أيام 🔥", "حقق هدفك اليومي ٧ أيام متتالية", "🔥"),
    PERFECT_LESSON("قناص الدقة 100% 🎯", "أنهِ درسًا بدون أي خطأ", "🎯"),
    COMBO_10("سلسلة إجابات ١٠", "أجب ١٠ إجابات صحيحة متتالية", "⚡"),
    XP_500("جامع الخبرة", "اجمع ٥٠٠ نقطة خبرة", "⭐"),
    CHEST_HUNTER("صائد الكنوز", "افتح ٣ صناديق كنز", "🎁"),
    ALPHABET_MASTER("سيّد الحروف", "أكمل الوحدة الأولى كاملة", "🔤"),
    PERSISTENT("المثابر", "أكمل ٣ جلسات لمراجعة الأخطاء", "💪"),
    BRAVE_SPEAKER("المتحدث الجريء", "انطق ١٠ عبارات بشكل صحيح", "🎤"),
    GOLD_LEAGUE("بطل الذهب", "اصعد إلى الدوري الذهبي", "🏆"),
    RAMADAN_CRESCENT("هلال رمضان", "حقق هدفك ١٠ أيام في رمضان", "🌙"),
    NATIONAL_PRIDE("فخر الوطن", "تعلّم في أسبوع اليوم الوطني", "🇸🇦"),
    STORYTELLER("الراوي", "أكمل ٣ مواقف حوارية", "💬"),
    SMOOTH_TALKER("لبق الحديث", "أنهِ موقفًا حواريًا بثلاث نجوم", "🌟"),
    ERROR_HUNTER("صياد الأخطاء", "صحّح ١٥ خطأً شائعًا", "🔍"),
    SHARP_EAR("أذن ذهبية", "أكمل ٣ تمارين في مختبر النطق", "👂"),
}

/** A row in the weekly league leaderboard. */
data class LeagueEntry(
    val name: String,
    val avatarEmoji: String,
    val weeklyXp: Int,
    val isCurrentUser: Boolean,
)
