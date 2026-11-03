package com.rihla.english.core.engine

import com.rihla.english.core.model.LeagueEntry
import com.rihla.english.core.model.LeagueOutcome
import com.rihla.english.core.model.LeagueTier
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Offline weekly leagues. Rivals are simulated deterministically from (tier, week) so the
 * leaderboard is stable across launches without any server. Their XP grows over the week.
 */
object LeagueEngine {
    const val GROUP_SIZE = 10
    const val PROMOTION_ZONE = 3
    const val DEMOTION_ZONE = 3

    private val rivalNames = listOf(
        "أحمد", "فاطمة", "يوسف", "مريم", "عمر", "ليلى", "خالد", "نور", "سارة",
        "علي", "هدى", "كريم", "زينب", "حسن", "ريم", "سلمى", "طارق", "جنى",
    )
    private val avatars = listOf("🦊", "🐼", "🦁", "🐯", "🐨", "🐸", "🐵", "🦉", "🐧", "🐙", "🦄", "🐳")

    /** Monday of the week containing [epochDay] (epoch day 0 = Thursday 1970-01-01). */
    fun weekStart(epochDay: Long): Long = epochDay - Math.floorMod(epochDay + 3, 7L)

    /** Fraction (0..1) of the week elapsed, given the day and the fraction of that day elapsed. */
    fun weekProgress(todayEpochDay: Long, fractionOfDay: Double): Double {
        val days = (todayEpochDay - weekStart(todayEpochDay)) + fractionOfDay.coerceIn(0.0, 1.0)
        return (days / 7.0).coerceIn(0.0, 1.0)
    }

    private fun targetRange(tier: LeagueTier): IntRange = when (tier) {
        LeagueTier.BRONZE -> 40..320
        LeagueTier.SILVER -> 90..480
        LeagueTier.GOLD -> 180..680
        LeagueTier.DIAMOND -> 300..950
    }

    /** Leaderboard sorted by weekly XP, highest first. */
    fun standings(
        tier: LeagueTier,
        weekStartEpochDay: Long,
        userWeeklyXp: Int,
        weekProgress: Double,
        userName: String = "أنت",
    ): List<LeagueEntry> {
        val random = Random(weekStartEpochDay * 31 + tier.ordinal * 7919)
        val names = rivalNames.shuffled(random)
        val faces = avatars.shuffled(random)
        val range = targetRange(tier)
        val rivals = (0 until GROUP_SIZE - 1).map { i ->
            val target = range.first + random.nextInt(range.last - range.first + 1)
            val pace = 0.7 + random.nextDouble() * 0.7
            val xp = (target * weekProgress.coerceIn(0.0, 1.0).pow(pace)).roundToInt()
            LeagueEntry(names[i], faces[i % faces.size], xp, isCurrentUser = false)
        }
        val me = LeagueEntry(userName, "🧑‍🎓", userWeeklyXp, isCurrentUser = true)
        // Ties are resolved in the learner's favour.
        return (rivals + me).sortedWith(compareByDescending<LeagueEntry> { it.weeklyXp }.thenByDescending { it.isCurrentUser })
    }

    /** 1-based rank of the learner. */
    fun rankOf(entries: List<LeagueEntry>): Int = entries.indexOfFirst { it.isCurrentUser } + 1

    /** Final standings at the end of the week: promote top 3, demote bottom 3. */
    fun resolve(tier: LeagueTier, weekStartEpochDay: Long, userWeeklyXp: Int): Pair<LeagueOutcome, LeagueTier> {
        val rank = rankOf(standings(tier, weekStartEpochDay, userWeeklyXp, weekProgress = 1.0))
        return when {
            rank <= PROMOTION_ZONE && tier != LeagueTier.DIAMOND -> LeagueOutcome.PROMOTED to tier.next
            rank > GROUP_SIZE - DEMOTION_ZONE && tier != LeagueTier.BRONZE -> LeagueOutcome.DEMOTED to tier.previous
            else -> LeagueOutcome.STAYED to tier
        }
    }
}
