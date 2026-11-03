package com.rihla.english.core

import com.rihla.english.core.engine.AnswerMatcher
import com.rihla.english.core.engine.LeagueEngine
import com.rihla.english.core.engine.LessonQueue
import com.rihla.english.core.engine.NodeState
import com.rihla.english.core.engine.SkillPath
import com.rihla.english.core.engine.Sm2Scheduler
import com.rihla.english.core.engine.Sm2State
import com.rihla.english.core.model.FlashcardAudio
import com.rihla.english.core.curriculum.SeedCurriculumData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CoreLogicTest {

    @Test fun `sm2 grows interval on success and resets on failure`() {
        var s = Sm2State()
        s = Sm2Scheduler.review(s, 5, 100)
        assertEquals(1, s.intervalDays)
        s = Sm2Scheduler.review(s, 5, 101)
        assertEquals(6, s.intervalDays)
        s = Sm2Scheduler.review(s, 5, 107)
        assertTrue(s.intervalDays > 6)
        assertEquals(107 + s.intervalDays.toLong(), s.dueEpochDay)
        s = Sm2Scheduler.review(s, 1, 120)
        assertEquals(0, s.repetitions)
        assertEquals(1, s.intervalDays)
        assertTrue(s.easiness >= Sm2Scheduler.MIN_EASINESS)
    }

    @Test fun `answer matcher normalises contractions punctuation and digits`() {
        assertTrue(AnswerMatcher.sentenceMatches("I'm a student.", listOf("I am a student")))
        assertTrue(AnswerMatcher.sentenceMatches("  hello,   GOOD morning! ", listOf("Hello good morning")))
        assertFalse(AnswerMatcher.sentenceMatches("I a student", listOf("I am a student")))
        assertTrue(AnswerMatcher.pronunciationAccepted(listOf("8"), listOf("Eight")))
        assertTrue(AnswerMatcher.pronunciationAccepted(listOf("thank you very match"), listOf("Thank you very much")))
        assertFalse(AnswerMatcher.pronunciationAccepted(listOf("bear"), listOf("pear tree")))
    }

    @Test fun `mistake recovery queue re-appends missed items`() {
        val cards = (1..3).map { FlashcardAudio("e$it", "w$it", "ك", "", "", "") }
        var q = LessonQueue(cards)
        q = q.answer(correct = false)
        assertEquals(listOf("e2", "e3", "e1"), q.pending.map { it.id })
        assertEquals(0f, q.progress)
        q = q.answer(true).answer(true)
        assertTrue(q.currentIsRetry)
        q = q.answer(true)
        assertTrue(q.isFinished)
        assertEquals(1f, q.progress)
    }

    @Test fun `skill path unlocks lessons sequentially and chests after lessons`() {
        val units = SeedCurriculumData.units
        val states = SkillPath.nodeStates(units, emptySet())
        assertEquals(NodeState.AVAILABLE, states["u1_l1"])
        assertEquals(NodeState.LOCKED, states["u1_l2"])
        assertEquals("u1_l1", SkillPath.currentLessonId(units, states))

        val done = setOf("u1_l1", "u1_l2", "u1_l3", "u1_l4")
        val s2 = SkillPath.nodeStates(units, done)
        assertEquals(NodeState.AVAILABLE, s2["u1_c1"])
        assertEquals(NodeState.AVAILABLE, s2["u1_l5"])
        assertEquals(NodeState.LOCKED, s2["u1_l6"])
    }

    @Test fun `league standings are deterministic and contain the user`() {
        val a = LeagueEngine.standings(com.rihla.english.core.model.LeagueTier.BRONZE, 19_998, 120, 0.5)
        val b = LeagueEngine.standings(com.rihla.english.core.model.LeagueTier.BRONZE, 19_998, 120, 0.5)
        assertEquals(a, b)
        assertEquals(LeagueEngine.GROUP_SIZE, a.size)
        assertEquals(1, a.count { it.isCurrentUser })
        assertEquals(4L, LeagueEngine.weekStart(4)) // epoch day 4 = Monday 1970-01-05
        assertEquals(4L, LeagueEngine.weekStart(10))
        assertEquals(-3L, LeagueEngine.weekStart(3)) // Sunday belongs to the previous week
    }
}

class ExerciseGraderTest {
    private val all = com.rihla.english.core.curriculum.SeedCurriculumData.units
        .flatMap { it.nodes }.flatMap { it.exercises }

    @Test fun `canonical answers grade as correct`() {
        all.forEach { e ->
            val answer = when (e) {
                is com.rihla.english.core.model.FlashcardAudio -> com.rihla.english.core.engine.UserAnswer.Acknowledged
                is com.rihla.english.core.model.MultipleChoice -> com.rihla.english.core.engine.UserAnswer.Choice(e.correctIndex)
                is com.rihla.english.core.model.WordBankSentence -> com.rihla.english.core.engine.UserAnswer.Tokens(e.answerTokens)
                is com.rihla.english.core.model.MatchPairs -> com.rihla.english.core.engine.UserAnswer.MatchCompleted(0)
                is com.rihla.english.core.model.ListeningChallenge ->
                    if (e.mode == com.rihla.english.core.model.ListeningMode.SELECT) com.rihla.english.core.engine.UserAnswer.Choice(e.correctIndex)
                    else com.rihla.english.core.engine.UserAnswer.Tokens(e.answerTokens)
                is com.rihla.english.core.model.SpeakingChallenge -> com.rihla.english.core.engine.UserAnswer.Speech(listOf(e.targetText.lowercase()))
                is com.rihla.english.core.model.ErrorCorrection -> com.rihla.english.core.engine.UserAnswer.Correction(e.wrongIndex, e.correctOption)
            }
            assertTrue(e.id, com.rihla.english.core.engine.ExerciseGrader.grade(e, answer).correct)
        }
    }

    @Test fun `wrong answers grade as incorrect`() {
        val wb = all.filterIsInstance<com.rihla.english.core.model.WordBankSentence>().first()
        val wrong = com.rihla.english.core.engine.UserAnswer.Tokens(wb.answerTokens.reversed())
        assertFalse(com.rihla.english.core.engine.ExerciseGrader.grade(wb, wrong).correct)
        val match = all.filterIsInstance<com.rihla.english.core.model.MatchPairs>().first()
        assertFalse(com.rihla.english.core.engine.ExerciseGrader.grade(match, com.rihla.english.core.engine.UserAnswer.MatchCompleted(1)).correct)
    }
}
