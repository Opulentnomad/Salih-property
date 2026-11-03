package com.rihla.english.core.engine

import com.rihla.english.core.model.Badge
import com.rihla.english.core.model.LeagueOutcome
import com.rihla.english.core.model.LeagueTier
import com.rihla.english.core.model.ShopItem
import com.rihla.english.core.model.UserStats
import com.rihla.english.core.model.UserStats.Companion.NO_DAY
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

/** Something noteworthy that happened while applying a gamification rule (drives UI celebrations). */
sealed interface GamificationEvent {
    data class XpGained(val amount: Int, val completionBonus: Int) : GamificationEvent
    data class GemsGained(val amount: Int) : GamificationEvent
    data class DailyGoalReached(val goalXp: Int) : GamificationEvent
    data class StreakExtended(val newStreak: Int) : GamificationEvent
    data class StreakFreezeUsed(val freezesUsed: Int) : GamificationEvent
    data class StreakLost(val previousStreak: Int) : GamificationEvent
    data class LevelUp(val newLevel: Int) : GamificationEvent
    data class BadgeUnlocked(val badge: Badge) : GamificationEvent
    data class HeartsGained(val amount: Int) : GamificationEvent
    data class LeagueResolved(val outcome: LeagueOutcome, val newTier: LeagueTier) : GamificationEvent
    data class ChestOpened(val gems: Int, val bonusFreeze: Boolean) : GamificationEvent
}

/** Result of applying a rule: the new stats plus the events to celebrate. */
data class Outcome(val stats: UserStats, val events: List<GamificationEvent> = emptyList())

/** Data collected by a finished lesson session. */
data class LessonResult(
    /** XP already earned for answers, including combo bonuses and the Double XP boost. */
    val answerXp: Int,
    val correctAnswers: Int,
    val totalAnswers: Int,
    val mistakes: Int,
    val bestCombo: Int,
    val speakingCorrect: Int,
    val isPractice: Boolean,
    val doubleXpActive: Boolean,
) {
    val isPerfect: Boolean get() = mistakes == 0 && totalAnswers > 0
    val accuracyPercent: Int
        get() = if (totalAnswers == 0) 100 else (correctAnswers * 100f / totalAnswers).toInt()
}

sealed interface PurchaseResult {
    data class Success(val outcome: Outcome) : PurchaseResult
    data class NotEnoughGems(val missing: Int) : PurchaseResult
    data class NotNeeded(val reasonAr: String) : PurchaseResult
}

/**
 * All XP, combo, streak, freeze, heart, gem, chest, shop, league and badge rules.
 * Pure and deterministic: every function takes the current [UserStats] and returns a new one,
 * so it can be applied atomically inside a DataStore transaction and unit-tested on the JVM.
 */
class GamificationEngine {

    // ---------------------------------------------------------------- XP & combos

    /** +10 XP per correct answer, +5 combo bonus once the learner is on 3+ correct in a row. */
    fun xpForCorrectAnswer(comboAfterAnswer: Int, doubleXp: Boolean): Int {
        val base = XP_PER_CORRECT + if (comboAfterAnswer >= COMBO_THRESHOLD) COMBO_BONUS_XP else 0
        return if (doubleXp) base * 2 else base
    }

    fun completionBonus(result: LessonResult): Int {
        val base = LESSON_COMPLETION_BONUS + if (result.isPerfect) PERFECT_LESSON_BONUS else 0
        return if (result.doubleXpActive) base * 2 else base
    }

    fun levelForXp(totalXp: Int): Int {
        var level = 1
        while (level < MAX_LEVEL && totalXp >= xpRequiredForLevel(level + 1)) level++
        return level
    }

    /** Total XP needed to reach [level]: 0, 100, 250, 450, 700, ... */
    fun xpRequiredForLevel(level: Int): Int = 25 * (level - 1) * (level + 2)

    // ---------------------------------------------------------------- Daily refresh

    /**
     * Run whenever the app comes to the foreground or a new day may have started:
     * resets daily XP, consumes Streak Freezes for missed days (or breaks the streak),
     * regenerates hearts and resolves the weekly league.
     */
    fun refresh(stats: UserStats, todayEpochDay: Long, nowMillis: Long): Outcome {
        val events = mutableListOf<GamificationEvent>()
        var s = stats

        if (s.dailyXpEpochDay != todayEpochDay) {
            s = s.copy(dailyXp = 0, dailyXpEpochDay = todayEpochDay)
        }

        if (s.currentStreak > 0 && s.lastGoalEpochDay != NO_DAY) {
            val gap = todayEpochDay - s.lastGoalEpochDay
            if (gap >= 2) {
                val missedDays = (gap - 1).toInt()
                s = if (s.streakFreezes >= missedDays) {
                    events += GamificationEvent.StreakFreezeUsed(missedDays)
                    s.copy(
                        streakFreezes = s.streakFreezes - missedDays,
                        lastGoalEpochDay = todayEpochDay - 1,
                    )
                } else {
                    events += GamificationEvent.StreakLost(s.currentStreak)
                    s.copy(currentStreak = 0)
                }
            }
        }

        val regen = regenerateHearts(s, nowMillis)
        s = regen.stats
        events += regen.events

        val league = resolveLeagueWeek(s, todayEpochDay)
        s = league.stats
        events += league.events

        return withBadges(Outcome(s, events), completedUnitIds = emptySet())
    }

    // ---------------------------------------------------------------- Hearts

    fun regenerateHearts(stats: UserStats, nowMillis: Long): Outcome {
        if (stats.hearts >= UserStats.MAX_HEARTS) {
            return Outcome(stats.copy(hearts = UserStats.MAX_HEARTS, heartRegenStartMillis = 0L))
        }
        if (stats.heartRegenStartMillis == 0L) {
            return Outcome(stats.copy(heartRegenStartMillis = nowMillis))
        }
        val elapsed = nowMillis - stats.heartRegenStartMillis
        if (elapsed < HEART_REGEN_MILLIS) return Outcome(stats)
        val gained = min((elapsed / HEART_REGEN_MILLIS).toInt(), UserStats.MAX_HEARTS - stats.hearts)
        val hearts = stats.hearts + gained
        val newStart = if (hearts >= UserStats.MAX_HEARTS) 0L
        else stats.heartRegenStartMillis + gained * HEART_REGEN_MILLIS
        return Outcome(
            stats.copy(hearts = hearts, heartRegenStartMillis = newStart),
            listOf(GamificationEvent.HeartsGained(gained)),
        )
    }

    /** Milliseconds until the next heart regenerates, or null if hearts are full. */
    fun millisUntilNextHeart(stats: UserStats, nowMillis: Long): Long? {
        if (stats.hearts >= UserStats.MAX_HEARTS || stats.heartRegenStartMillis == 0L) return null
        val elapsed = (nowMillis - stats.heartRegenStartMillis).coerceAtLeast(0)
        return HEART_REGEN_MILLIS - (elapsed % HEART_REGEN_MILLIS)
    }

    fun loseHeart(stats: UserStats, nowMillis: Long): UserStats {
        val s = regenerateHearts(stats, nowMillis).stats
        val hearts = max(0, s.hearts - 1)
        val start = if (s.heartRegenStartMillis == 0L) nowMillis else s.heartRegenStartMillis
        return s.copy(hearts = hearts, heartRegenStartMillis = start)
    }

    // ---------------------------------------------------------------- Lesson completion

    fun completeLesson(
        stats: UserStats,
        result: LessonResult,
        todayEpochDay: Long,
        nowMillis: Long,
        completedUnitIds: Set<String>,
    ): Outcome {
        val refreshed = refresh(stats, todayEpochDay, nowMillis)
        val events = refreshed.events.toMutableList()

        val bonus = completionBonus(result)
        val xp = result.answerXp + bonus
        events += GamificationEvent.XpGained(xp, bonus)
        val xpOutcome = addXp(refreshed.stats, xp, todayEpochDay)
        events += xpOutcome.events
        var s = xpOutcome.stats

        val gems = LESSON_GEMS + if (result.isPerfect) PERFECT_LESSON_GEMS else 0
        events += GamificationEvent.GemsGained(gems)
        s = s.copy(
            gems = s.gems + gems,
            lastCompletedEpochDay = todayEpochDay,
            lessonsCompleted = s.lessonsCompleted + if (result.isPractice) 0 else 1,
            perfectLessons = s.perfectLessons + if (result.isPerfect && !result.isPractice) 1 else 0,
            practiceSessions = s.practiceSessions + if (result.isPractice) 1 else 0,
            speakingCorrect = s.speakingCorrect + result.speakingCorrect,
            bestCombo = max(s.bestCombo, result.bestCombo),
        )

        if (result.isPractice && s.hearts < UserStats.MAX_HEARTS) {
            val hearts = s.hearts + 1
            s = s.copy(
                hearts = hearts,
                heartRegenStartMillis = if (hearts >= UserStats.MAX_HEARTS) 0L else s.heartRegenStartMillis,
            )
            events += GamificationEvent.HeartsGained(1)
        }

        return withBadges(Outcome(s, events), completedUnitIds)
    }

    /** Adds XP to total/daily/weekly counters, levels up and extends the streak when the goal is reached. */
    fun addXp(stats: UserStats, amount: Int, todayEpochDay: Long): Outcome {
        val events = mutableListOf<GamificationEvent>()
        val dailyBefore = if (stats.dailyXpEpochDay == todayEpochDay) stats.dailyXp else 0
        val dailyAfter = dailyBefore + amount
        val total = stats.totalXp + amount
        val newLevel = levelForXp(total)
        if (newLevel > stats.level) events += GamificationEvent.LevelUp(newLevel)

        var s = stats.copy(
            totalXp = total,
            level = newLevel,
            dailyXp = dailyAfter,
            dailyXpEpochDay = todayEpochDay,
            weeklyXp = stats.weeklyXp + amount,
        )

        val goalJustReached = dailyBefore < s.dailyGoalXp && dailyAfter >= s.dailyGoalXp
        if (goalJustReached && s.lastGoalEpochDay != todayEpochDay) {
            events += GamificationEvent.DailyGoalReached(s.dailyGoalXp)
            val continues = s.lastGoalEpochDay == todayEpochDay - 1 && s.currentStreak > 0
            val streak = if (continues) s.currentStreak + 1 else 1
            s = s.copy(
                currentStreak = streak,
                bestStreak = max(s.bestStreak, streak),
                lastGoalEpochDay = todayEpochDay,
            )
            events += GamificationEvent.StreakExtended(streak)
        }
        return Outcome(s, events)
    }

    // ---------------------------------------------------------------- Chests & shop

    /** Opens a treasure chest. Rewards are deterministic per chest id so re-renders can't reroll. */
    fun openChest(stats: UserStats, chestId: String): Outcome {
        val random = Random(chestId.hashCode())
        val gems = CHEST_MIN_GEMS + random.nextInt(CHEST_MAX_GEMS - CHEST_MIN_GEMS + 1)
        val freeze = random.nextInt(100) < CHEST_FREEZE_CHANCE_PERCENT &&
            stats.streakFreezes < UserStats.MAX_STREAK_FREEZES
        val s = stats.copy(
            gems = stats.gems + gems,
            chestsOpened = stats.chestsOpened + 1,
            streakFreezes = stats.streakFreezes + if (freeze) 1 else 0,
        )
        return withBadges(
            Outcome(s, listOf(GamificationEvent.ChestOpened(gems, freeze), GamificationEvent.GemsGained(gems))),
            completedUnitIds = emptySet(),
        )
    }

    fun purchase(stats: UserStats, item: ShopItem, nowMillis: Long): PurchaseResult {
        val current = regenerateHearts(stats, nowMillis).stats
        when (item) {
            ShopItem.STREAK_FREEZE -> if (current.streakFreezes >= UserStats.MAX_STREAK_FREEZES) {
                return PurchaseResult.NotNeeded("لديك الحد الأقصى من تجميد السلسلة (٢).")
            }
            ShopItem.HEART_REFILL -> if (current.hearts >= UserStats.MAX_HEARTS) {
                return PurchaseResult.NotNeeded("قلوبك ممتلئة بالفعل!")
            }
            ShopItem.DOUBLE_XP_BOOST -> Unit
        }
        if (current.gems < item.priceGems) {
            return PurchaseResult.NotEnoughGems(item.priceGems - current.gems)
        }
        val paid = current.copy(gems = current.gems - item.priceGems)
        val updated = when (item) {
            ShopItem.STREAK_FREEZE -> paid.copy(streakFreezes = paid.streakFreezes + 1)
            ShopItem.HEART_REFILL -> paid.copy(hearts = UserStats.MAX_HEARTS, heartRegenStartMillis = 0L)
            ShopItem.DOUBLE_XP_BOOST -> paid.copy(
                doubleXpUntilMillis = max(nowMillis, paid.doubleXpUntilMillis) + DOUBLE_XP_DURATION_MILLIS,
            )
        }
        return PurchaseResult.Success(Outcome(updated))
    }

    // ---------------------------------------------------------------- Leagues

    fun resolveLeagueWeek(stats: UserStats, todayEpochDay: Long): Outcome {
        val currentWeek = LeagueEngine.weekStart(todayEpochDay)
        if (stats.weekStartEpochDay == NO_DAY) {
            return Outcome(stats.copy(weekStartEpochDay = currentWeek, weeklyXp = 0))
        }
        if (stats.weekStartEpochDay >= currentWeek) return Outcome(stats)

        val (outcome, tier) = LeagueEngine.resolve(stats.league, stats.weekStartEpochDay, stats.weeklyXp)
        val s = stats.copy(
            league = tier,
            lastLeagueOutcome = outcome,
            weeklyXp = 0,
            weekStartEpochDay = currentWeek,
        )
        return Outcome(s, listOf(GamificationEvent.LeagueResolved(outcome, tier)))
    }

    // ---------------------------------------------------------------- Badges

    fun earnedBadges(stats: UserStats, completedUnitIds: Set<String>): Set<Badge> = buildSet {
        if (stats.lessonsCompleted >= 1) add(Badge.FIRST_LESSON)
        if (stats.bestStreak >= 3) add(Badge.STREAK_3)
        if (stats.bestStreak >= 7) add(Badge.STREAK_7)
        if (stats.perfectLessons >= 1) add(Badge.PERFECT_LESSON)
        if (stats.bestCombo >= 10) add(Badge.COMBO_10)
        if (stats.totalXp >= 500) add(Badge.XP_500)
        if (stats.chestsOpened >= 3) add(Badge.CHEST_HUNTER)
        if (FIRST_UNIT_ID in completedUnitIds) add(Badge.ALPHABET_MASTER)
        if (stats.practiceSessions >= 3) add(Badge.PERSISTENT)
        if (stats.speakingCorrect >= 10) add(Badge.BRAVE_SPEAKER)
        if (stats.league >= LeagueTier.GOLD) add(Badge.GOLD_LEAGUE)
    }

    /** Unlocks newly earned badges (each grants [BADGE_GEMS]). Badges are never revoked. */
    private fun withBadges(outcome: Outcome, completedUnitIds: Set<String>): Outcome {
        val s = outcome.stats
        val newBadges = earnedBadges(s, completedUnitIds).filter { it.name !in s.unlockedBadges }
        if (newBadges.isEmpty()) return outcome
        val gems = newBadges.size * BADGE_GEMS
        return Outcome(
            s.copy(
                unlockedBadges = s.unlockedBadges + newBadges.map { it.name },
                gems = s.gems + gems,
            ),
            outcome.events + newBadges.map { GamificationEvent.BadgeUnlocked(it) } +
                GamificationEvent.GemsGained(gems),
        )
    }

    companion object {
        const val XP_PER_CORRECT = 10
        const val COMBO_THRESHOLD = 3
        const val COMBO_BONUS_XP = 5
        const val LESSON_COMPLETION_BONUS = 15
        const val PERFECT_LESSON_BONUS = 5
        const val MAX_LEVEL = 50

        const val LESSON_GEMS = 5
        const val PERFECT_LESSON_GEMS = 5
        const val BADGE_GEMS = 20
        const val CHEST_MIN_GEMS = 15
        const val CHEST_MAX_GEMS = 40
        const val CHEST_FREEZE_CHANCE_PERCENT = 25

        const val HEART_REGEN_MILLIS = 30L * 60 * 1000
        const val DOUBLE_XP_DURATION_MILLIS = 15L * 60 * 1000

        const val FIRST_UNIT_ID = "u1"
    }
}
