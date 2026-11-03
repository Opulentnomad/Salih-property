package com.rihla.english.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.rihla.english.core.model.LeagueOutcome
import com.rihla.english.core.model.LeagueTier
import com.rihla.english.core.model.UserStats
import com.rihla.english.core.repository.UserStatsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

val Context.userStatsDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_stats")

/** Jetpack DataStore implementation of [UserStatsRepository]. Updates are atomic transactions. */
class UserStatsDataStore(private val dataStore: DataStore<Preferences>) : UserStatsRepository {

    private object Keys {
        val totalXp = intPreferencesKey("total_xp")
        val level = intPreferencesKey("level")
        val dailyXp = intPreferencesKey("daily_xp")
        val dailyXpDay = longPreferencesKey("daily_xp_day")
        val dailyGoal = intPreferencesKey("daily_goal_xp")
        val currentStreak = intPreferencesKey("current_streak")
        val bestStreak = intPreferencesKey("best_streak")
        val lastGoalDay = longPreferencesKey("last_goal_day")
        val streakFreezes = intPreferencesKey("streak_freezes")
        val hearts = intPreferencesKey("hearts")
        val heartRegenStart = longPreferencesKey("heart_regen_start")
        val gems = intPreferencesKey("gems")
        val lastCompletedDay = longPreferencesKey("last_completed_day")
        val doubleXpUntil = longPreferencesKey("double_xp_until")
        val weeklyXp = intPreferencesKey("weekly_xp")
        val weekStart = longPreferencesKey("week_start_day")
        val league = stringPreferencesKey("league")
        val lastLeagueOutcome = stringPreferencesKey("last_league_outcome")
        val lessonsCompleted = intPreferencesKey("lessons_completed")
        val perfectLessons = intPreferencesKey("perfect_lessons")
        val chestsOpened = intPreferencesKey("chests_opened")
        val practiceSessions = intPreferencesKey("practice_sessions")
        val speakingCorrect = intPreferencesKey("speaking_correct")
        val bestCombo = intPreferencesKey("best_combo")
        val badges = stringSetPreferencesKey("badges")
        val storiesCompleted = intPreferencesKey("stories_completed")
        val perfectStories = intPreferencesKey("perfect_stories")
        val soundDrills = intPreferencesKey("sound_drills")
        val errorCorrections = intPreferencesKey("error_corrections")
        val ramadanGoalDays = intPreferencesKey("ramadan_goal_days")
        val ramadanHijriYear = intPreferencesKey("ramadan_hijri_year")
        val nationalDay = intPreferencesKey("celebrated_national_day")
        val dailyBonusDay = longPreferencesKey("daily_bonus_day")
    }

    override val stats: Flow<UserStats> = dataStore.data.map { it.toStats() }.distinctUntilChanged()

    override suspend fun <R> updateAndGet(transform: (UserStats) -> Pair<UserStats, R>): R {
        var result: R? = null
        dataStore.edit { prefs ->
            val (updated, r) = transform(prefs.toStats())
            prefs.write(updated)
            result = r
        }
        @Suppress("UNCHECKED_CAST")
        return result as R
    }

    private fun Preferences.toStats(): UserStats {
        val d = UserStats()
        return UserStats(
            totalXp = this[Keys.totalXp] ?: d.totalXp,
            level = this[Keys.level] ?: d.level,
            dailyXp = this[Keys.dailyXp] ?: d.dailyXp,
            dailyXpEpochDay = this[Keys.dailyXpDay] ?: d.dailyXpEpochDay,
            dailyGoalXp = this[Keys.dailyGoal] ?: d.dailyGoalXp,
            currentStreak = this[Keys.currentStreak] ?: d.currentStreak,
            bestStreak = this[Keys.bestStreak] ?: d.bestStreak,
            lastGoalEpochDay = this[Keys.lastGoalDay] ?: d.lastGoalEpochDay,
            streakFreezes = this[Keys.streakFreezes] ?: d.streakFreezes,
            hearts = this[Keys.hearts] ?: d.hearts,
            heartRegenStartMillis = this[Keys.heartRegenStart] ?: d.heartRegenStartMillis,
            gems = this[Keys.gems] ?: d.gems,
            lastCompletedEpochDay = this[Keys.lastCompletedDay] ?: d.lastCompletedEpochDay,
            doubleXpUntilMillis = this[Keys.doubleXpUntil] ?: d.doubleXpUntilMillis,
            weeklyXp = this[Keys.weeklyXp] ?: d.weeklyXp,
            weekStartEpochDay = this[Keys.weekStart] ?: d.weekStartEpochDay,
            league = this[Keys.league]?.let { runCatching { LeagueTier.valueOf(it) }.getOrNull() } ?: d.league,
            lastLeagueOutcome = this[Keys.lastLeagueOutcome]?.let { runCatching { LeagueOutcome.valueOf(it) }.getOrNull() },
            lessonsCompleted = this[Keys.lessonsCompleted] ?: d.lessonsCompleted,
            perfectLessons = this[Keys.perfectLessons] ?: d.perfectLessons,
            chestsOpened = this[Keys.chestsOpened] ?: d.chestsOpened,
            practiceSessions = this[Keys.practiceSessions] ?: d.practiceSessions,
            speakingCorrect = this[Keys.speakingCorrect] ?: d.speakingCorrect,
            bestCombo = this[Keys.bestCombo] ?: d.bestCombo,
            unlockedBadges = this[Keys.badges] ?: d.unlockedBadges,
            storiesCompleted = this[Keys.storiesCompleted] ?: d.storiesCompleted,
            perfectStories = this[Keys.perfectStories] ?: d.perfectStories,
            soundDrills = this[Keys.soundDrills] ?: d.soundDrills,
            errorCorrectionsCorrect = this[Keys.errorCorrections] ?: d.errorCorrectionsCorrect,
            ramadanGoalDays = this[Keys.ramadanGoalDays] ?: d.ramadanGoalDays,
            ramadanHijriYear = this[Keys.ramadanHijriYear] ?: d.ramadanHijriYear,
            celebratedNationalDay = (this[Keys.nationalDay] ?: 0) == 1,
            dailyBonusClaimedDay = this[Keys.dailyBonusDay] ?: d.dailyBonusClaimedDay,
        )
    }

    private fun MutablePreferences.write(s: UserStats) {
        this[Keys.totalXp] = s.totalXp
        this[Keys.level] = s.level
        this[Keys.dailyXp] = s.dailyXp
        this[Keys.dailyXpDay] = s.dailyXpEpochDay
        this[Keys.dailyGoal] = s.dailyGoalXp
        this[Keys.currentStreak] = s.currentStreak
        this[Keys.bestStreak] = s.bestStreak
        this[Keys.lastGoalDay] = s.lastGoalEpochDay
        this[Keys.streakFreezes] = s.streakFreezes
        this[Keys.hearts] = s.hearts
        this[Keys.heartRegenStart] = s.heartRegenStartMillis
        this[Keys.gems] = s.gems
        this[Keys.lastCompletedDay] = s.lastCompletedEpochDay
        this[Keys.doubleXpUntil] = s.doubleXpUntilMillis
        this[Keys.weeklyXp] = s.weeklyXp
        this[Keys.weekStart] = s.weekStartEpochDay
        this[Keys.league] = s.league.name
        if (s.lastLeagueOutcome != null) this[Keys.lastLeagueOutcome] = s.lastLeagueOutcome!!.name
        else remove(Keys.lastLeagueOutcome)
        this[Keys.lessonsCompleted] = s.lessonsCompleted
        this[Keys.perfectLessons] = s.perfectLessons
        this[Keys.chestsOpened] = s.chestsOpened
        this[Keys.practiceSessions] = s.practiceSessions
        this[Keys.speakingCorrect] = s.speakingCorrect
        this[Keys.bestCombo] = s.bestCombo
        this[Keys.badges] = s.unlockedBadges
        this[Keys.storiesCompleted] = s.storiesCompleted
        this[Keys.perfectStories] = s.perfectStories
        this[Keys.soundDrills] = s.soundDrills
        this[Keys.errorCorrections] = s.errorCorrectionsCorrect
        this[Keys.ramadanGoalDays] = s.ramadanGoalDays
        this[Keys.ramadanHijriYear] = s.ramadanHijriYear
        this[Keys.nationalDay] = if (s.celebratedNationalDay) 1 else 0
        this[Keys.dailyBonusDay] = s.dailyBonusClaimedDay
    }
}
