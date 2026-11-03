package com.rihla.english.core

import com.rihla.english.core.curriculum.SeedCurriculumData
import com.rihla.english.core.engine.ExerciseGrader
import com.rihla.english.core.engine.GamificationEngine
import com.rihla.english.core.engine.GamificationEvent
import com.rihla.english.core.engine.UserAnswer
import com.rihla.english.core.model.Badge
import com.rihla.english.core.model.DailyPlan
import com.rihla.english.core.model.DayActivity
import com.rihla.english.core.model.ErrorCorrection
import com.rihla.english.core.model.ErrorPattern
import com.rihla.english.core.model.LearningInsights
import com.rihla.english.core.model.SessionId
import com.rihla.english.core.model.SpeakingChallenge
import com.rihla.english.core.model.UserStats
import com.rihla.english.core.season.Season
import com.rihla.english.core.season.SeasonCalendar
import com.rihla.english.core.sound.SoundLab
import com.rihla.english.core.story.ReplyQuality
import com.rihla.english.core.story.SeedStories
import com.rihla.english.core.story.StoryEngine
import com.rihla.english.core.story.StoryStep
import java.time.LocalDate
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NewFeaturesTest {

    // ------------------------------------------------------------------ stories

    @Test fun `every story can be finished with three stars by choosing good replies`() {
        assertEquals(6, SeedStories.all.size)
        SeedStories.all.forEach { story ->
            var state = StoryEngine.start(story)
            var guard = 0
            while (!state.isFinished && guard++ < 200) {
                state = when (val step = state.currentStep) {
                    is StoryStep.Line -> StoryEngine.advance(state)
                    is StoryStep.Choice -> StoryEngine.choose(state, step.options.indexOfFirst { it.quality == ReplyQuality.GOOD })
                    null -> state
                }
            }
            assertTrue(story.id, state.isFinished)
            assertEquals(story.id, 3, state.stars)
            assertTrue(story.id, story.choiceCount >= 4)
        }
    }

    @Test fun `bad reply shows a reaction, keeps the choice, and costs stars`() {
        val story = SeedStories.byId("airport")!!
        var state = StoryEngine.advance(StoryEngine.start(story))
        val choice = state.currentStep as StoryStep.Choice
        val badIndex = choice.options.indexOfFirst { it.quality == ReplyQuality.BAD }
        val before = state.stepIndex
        state = StoryEngine.choose(state, badIndex)
        assertEquals(before, state.stepIndex)
        assertNotNull(state.lastFeedback)
        // Then pick the good reply and finish with good answers.
        state = StoryEngine.choose(state, choice.options.indexOfFirst { it.quality == ReplyQuality.GOOD })
        assertEquals(before + 1, state.stepIndex)
        while (!state.isFinished) {
            state = when (val step = state.currentStep) {
                is StoryStep.Line -> StoryEngine.advance(state)
                is StoryStep.Choice -> StoryEngine.choose(state, step.options.indexOfFirst { it.quality == ReplyQuality.GOOD })
                null -> state
            }
        }
        assertTrue(state.stars < 3)
    }

    @Test fun `story requirements point at real lessons`() {
        val nodeIds = SeedCurriculumData.units.flatMap { it.nodes }.map { it.id }.toSet()
        SeedStories.all.mapNotNull { it.requiredNodeId }.forEach { assertTrue(it, it in nodeIds) }
    }

    // ------------------------------------------------------------------ sound lab

    @Test fun `sound lab builds tagged sessions with confusable speaking items`() {
        ErrorPattern.sounds.forEach { pattern ->
            val session = SoundLab.buildSession(pattern, Random(1))
            assertTrue(pattern.name, session.size >= 8)
            assertTrue(session.all { it.pattern == pattern })
            assertEquals(session.size, session.map { it.id }.toSet().size)
            session.filterIsInstance<SpeakingChallenge>().forEach { assertTrue(it.confusables.isNotEmpty()) }
        }
    }

    @Test fun `speaking grader detects the minimal-pair slip`() {
        val e = SoundLab.buildSession(ErrorPattern.SOUND_P_B, Random(3)).filterIsInstance<SpeakingChallenge>().first()
        val wrong = ExerciseGrader.grade(e, UserAnswer.Speech(listOf(e.confusables.first())))
        assertFalse(wrong.correct)
        assertEquals(e.confusables.first(), wrong.confusedWith)
        val right = ExerciseGrader.grade(e, UserAnswer.Speech(listOf(e.targetText)))
        assertTrue(right.correct)
        assertNull(right.confusedWith)
    }

    // ------------------------------------------------------------------ error correction

    @Test fun `error correction grading and corrected sentence`() {
        val all = SeedCurriculumData.units.flatMap { it.nodes }.flatMap { it.exercises }.filterIsInstance<ErrorCorrection>()
        assertTrue(all.size >= 20)
        val agree = all.first { it.tokens.joinToString(" ") == "I am agree with you" }
        assertEquals("I agree with you", agree.correctedSentence)
        assertTrue(ExerciseGrader.grade(agree, UserAnswer.Correction(agree.wrongIndex, agree.correctOption)).correct)
        assertFalse(ExerciseGrader.grade(agree, UserAnswer.Correction(0, agree.correctOption)).correct)
        assertTrue(all.all { it.pattern != null })
    }

    @Test fun `every grammar trap has practice material`() {
        val tagged = SeedCurriculumData.units.flatMap { it.nodes }.flatMap { it.exercises }.mapNotNull { it.pattern }.toSet()
        ErrorPattern.grammar.forEach { assertTrue(it.name, it in tagged) }
    }

    // ------------------------------------------------------------------ seasons

    @Test fun `season calendar recognises Ramadan, Eid and National Day`() {
        // 1 Ramadan 1447 AH ≈ 18 Feb 2026 (Umm al-Qura).
        assertEquals(Season.RAMADAN, SeasonCalendar.seasonOn(LocalDate.of(2026, 3, 5)))
        assertNotNull(SeasonCalendar.ramadanYearOf(LocalDate.of(2026, 3, 5).toEpochDay()))
        assertEquals(Season.EID_AL_FITR, SeasonCalendar.seasonOn(LocalDate.of(2026, 3, 21)))
        assertEquals(Season.NATIONAL_DAY, SeasonCalendar.seasonOn(LocalDate.of(2026, 9, 23)))
        assertNull(SeasonCalendar.seasonOn(LocalDate.of(2026, 7, 1)))
    }

    @Test fun `missed days during Eid keep the streak without spending freezes`() {
        val today = 1000L
        val engine = GamificationEngine(
            seasonOf = { if (it in 997L..999L) Season.EID_AL_FITR else null },
            ramadanYearOf = { null },
        )
        val s = UserStats(currentStreak = 9, lastGoalEpochDay = today - 4, streakFreezes = 0)
        val out = engine.refresh(s, today, 0L)
        assertEquals(9, out.stats.currentStreak)
        assertEquals(today - 1, out.stats.lastGoalEpochDay)
        assertTrue(out.events.any { it is GamificationEvent.StreakProtectedBySeason })
    }

    @Test fun `ramadan challenge lights crescents and unlocks badge`() {
        val engine = GamificationEngine(seasonOf = { Season.RAMADAN }, ramadanYearOf = { 1447 })
        var s = UserStats(gems = 0)
        repeat(10) { day -> s = engine.addXp(s, 40, 500L + day).stats }
        assertEquals(10, s.ramadanGoalDays)
        assertTrue(Badge.RAMADAN_CRESCENT in engine.earnedBadges(s, emptySet()))
        // A new Ramadan year resets the counter.
        val next = GamificationEngine(seasonOf = { Season.RAMADAN }, ramadanYearOf = { 1448 }).addXp(s, 40, 900L).stats
        assertEquals(1, next.ramadanGoalDays)
    }

    // ------------------------------------------------------------------ stories reward, daily plan, sessions

    @Test fun `story completion grants xp, gems and storyteller badge`() {
        val engine = GamificationEngine(seasonOf = { null }, ramadanYearOf = { null })
        var s = UserStats(gems = 0)
        repeat(3) { s = engine.completeStory(s, stars = 3, todayEpochDay = 2000L, nowMillis = 0L, firstTime = true).stats }
        assertEquals(3, s.storiesCompleted)
        assertTrue(Badge.STORYTELLER.name in s.unlockedBadges)
        assertTrue(Badge.SMOOTH_TALKER.name in s.unlockedBadges)
        assertEquals(3 * 45, s.totalXp)
    }

    @Test fun `daily plan bonus only once and only when all tasks are done`() {
        val engine = GamificationEngine()
        val insights = LearningInsights(days = mapOf(10L to DayActivity(lessons = 1, practices = 1, stories = 1)))
        val tasks = DailyPlan.tasksFor(insights, 10L)
        assertTrue(tasks.all { it.done })
        val claimed = engine.claimDailyBonus(UserStats(gems = 0), 10L, allTasksDone = true).stats
        assertEquals(DailyPlan.BONUS_GEMS, claimed.gems)
        assertEquals(claimed, engine.claimDailyBonus(claimed, 10L, allTasksDone = true).stats)
        assertEquals(0, engine.claimDailyBonus(UserStats(gems = 0), 10L, allTasksDone = false).stats.gems)
    }

    @Test fun `session ids round-trip`() {
        listOf(SessionId.Lesson("u1_l1"), SessionId.Practice, SessionId.Pattern(ErrorPattern.PREPOSITIONS), SessionId.Sound(ErrorPattern.SOUND_TH))
            .forEach { assertEquals(it, SessionId.parse(it.raw)) }
    }
}
