package com.rihla.english.core

import com.rihla.english.core.engine.GamificationEngine
import com.rihla.english.core.engine.GamificationEvent
import com.rihla.english.core.engine.LessonResult
import com.rihla.english.core.engine.PurchaseResult
import com.rihla.english.core.model.Badge
import com.rihla.english.core.model.ShopItem
import com.rihla.english.core.model.UserStats
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GamificationEngineTest {
    private val engine = GamificationEngine()
    private val today = 20_000L
    private val now = 1_700_000_000_000L

    private fun result(xp: Int = 50, mistakes: Int = 0, practice: Boolean = false) = LessonResult(
        answerXp = xp, correctAnswers = 5, totalAnswers = 5 + mistakes, mistakes = mistakes,
        bestCombo = 5, speakingCorrect = 0, isPractice = practice, doubleXpActive = false,
    )

    @Test fun `correct answer gives 10 xp and combo bonus from third in a row`() {
        assertEquals(10, engine.xpForCorrectAnswer(1, doubleXp = false))
        assertEquals(10, engine.xpForCorrectAnswer(2, doubleXp = false))
        assertEquals(15, engine.xpForCorrectAnswer(3, doubleXp = false))
        assertEquals(30, engine.xpForCorrectAnswer(7, doubleXp = true))
    }

    @Test fun `reaching daily goal starts and extends streak`() {
        var s = UserStats()
        s = engine.completeLesson(s, result(xp = 40), today, now, emptySet()).stats
        assertEquals(1, s.currentStreak)
        assertEquals(today, s.lastGoalEpochDay)

        val next = engine.completeLesson(s, result(xp = 40), today + 1, now, emptySet())
        assertEquals(2, next.stats.currentStreak)
        assertTrue(next.events.any { it is GamificationEvent.StreakExtended })
    }

    @Test fun `streak only extends once per day`() {
        var s = engine.completeLesson(UserStats(), result(xp = 40), today, now, emptySet()).stats
        s = engine.completeLesson(s, result(xp = 40), today, now, emptySet()).stats
        assertEquals(1, s.currentStreak)
    }

    @Test fun `missed day consumes a streak freeze`() {
        val s = UserStats(currentStreak = 5, bestStreak = 5, lastGoalEpochDay = today - 2, streakFreezes = 1)
        val out = engine.refresh(s, today, now)
        assertEquals(5, out.stats.currentStreak)
        assertEquals(0, out.stats.streakFreezes)
        assertEquals(today - 1, out.stats.lastGoalEpochDay)
        assertTrue(out.events.contains(GamificationEvent.StreakFreezeUsed(1)))
    }

    @Test fun `missed days without enough freezes lose the streak`() {
        val s = UserStats(currentStreak = 5, lastGoalEpochDay = today - 3, streakFreezes = 1)
        val out = engine.refresh(s, today, now)
        assertEquals(0, out.stats.currentStreak)
        assertEquals(1, out.stats.streakFreezes)
    }

    @Test fun `hearts are lost and regenerate over time`() {
        var s = engine.loseHeart(UserStats(), now)
        assertEquals(4, s.hearts)
        s = engine.loseHeart(s, now + 1000)
        assertEquals(3, s.hearts)
        val later = engine.regenerateHearts(s, now + GamificationEngine.HEART_REGEN_MILLIS * 2 + 5)
        assertEquals(5, later.stats.hearts)
        assertEquals(0L, later.stats.heartRegenStartMillis)
    }

    @Test fun `practice session restores one heart`() {
        val s = UserStats(hearts = 0, heartRegenStartMillis = now)
        val out = engine.completeLesson(s, result(practice = true), today, now, emptySet())
        assertEquals(1, out.stats.hearts)
    }

    @Test fun `shop purchases deduct gems and respect limits`() {
        val s = UserStats(gems = 100, hearts = 2, heartRegenStartMillis = now)
        val refill = engine.purchase(s, ShopItem.HEART_REFILL, now) as PurchaseResult.Success
        assertEquals(5, refill.outcome.stats.hearts)
        assertEquals(60, refill.outcome.stats.gems)

        val full = engine.purchase(refill.outcome.stats, ShopItem.HEART_REFILL, now)
        assertTrue(full is PurchaseResult.NotNeeded)

        val poor = engine.purchase(UserStats(gems = 10), ShopItem.DOUBLE_XP_BOOST, now)
        assertEquals(50, (poor as PurchaseResult.NotEnoughGems).missing)

        val boost = engine.purchase(UserStats(gems = 60), ShopItem.DOUBLE_XP_BOOST, now) as PurchaseResult.Success
        assertTrue(boost.outcome.stats.isDoubleXpActive(now + 14 * 60 * 1000))
        assertFalse(boost.outcome.stats.isDoubleXpActive(now + 16 * 60 * 1000))

        val maxed = engine.purchase(UserStats(gems = 500, streakFreezes = 2), ShopItem.STREAK_FREEZE, now)
        assertTrue(maxed is PurchaseResult.NotNeeded)
    }

    @Test fun `first lesson and perfect lesson unlock badges with gem rewards`() {
        val out = engine.completeLesson(UserStats(gems = 0), result(), today, now, emptySet())
        assertTrue(Badge.FIRST_LESSON.name in out.stats.unlockedBadges)
        assertTrue(Badge.PERFECT_LESSON.name in out.stats.unlockedBadges)
        // 5 lesson gems + 5 perfect gems + 2 badges x 20
        assertEquals(50, out.stats.gems)
    }

    @Test fun `chest rewards are deterministic per chest`() {
        val a = engine.openChest(UserStats(gems = 0), "u1_c1").stats
        val b = engine.openChest(UserStats(gems = 0), "u1_c1").stats
        assertEquals(a.gems, b.gems)
        assertTrue(a.gems in GamificationEngine.CHEST_MIN_GEMS..GamificationEngine.CHEST_MAX_GEMS)
        assertEquals(1, a.chestsOpened)
    }

    @Test fun `level thresholds grow`() {
        assertEquals(1, engine.levelForXp(99))
        assertEquals(2, engine.levelForXp(100))
        assertEquals(3, engine.levelForXp(250))
    }

    @Test fun `league week rollover resets weekly xp`() {
        val s = UserStats(weekStartEpochDay = today - 14, weeklyXp = 2000)
        val out = engine.resolveLeagueWeek(s, today)
        assertEquals(0, out.stats.weeklyXp)
        assertTrue(out.events.single() is GamificationEvent.LeagueResolved)
        assertEquals(com.rihla.english.core.model.LeagueTier.SILVER, out.stats.league)
    }
}
