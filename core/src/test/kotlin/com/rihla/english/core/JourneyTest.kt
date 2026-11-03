package com.rihla.english.core

import com.rihla.english.core.engine.AnswerMatcher
import com.rihla.english.core.journey.Beat
import com.rihla.english.core.journey.ChapterStatus
import com.rihla.english.core.journey.Journey
import com.rihla.english.core.journey.JourneyProgress
import com.rihla.english.core.journey.Naturalness
import com.rihla.english.core.journey.ScenePlayer
import com.rihla.english.core.journey.SceneState
import com.rihla.english.core.journey.Script
import com.rihla.english.core.season.Season
import com.rihla.english.core.season.SeasonCalendar
import java.time.LocalDate
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class JourneyTest {
    private val chapters = Script.chapters

    /** Plays a scene choosing the first natural reply each time. */
    private fun playThrough(state0: SceneState, firstWrong: Boolean = false): SceneState {
        var s = state0
        var guard = 0
        var triedWrong = false
        while (!s.isFinished && guard++ < 100) {
            val b = s.beat
            if (b is Beat.Reply && s.picked == null) {
                val wrongIndex = b.options.indexOfFirst { it.naturalness == Naturalness.UNNATURAL }
                s = if (firstWrong && !triedWrong && wrongIndex >= 0) {
                    triedWrong = true
                    ScenePlayer.choose(s, wrongIndex).also { assertFalse(it.canContinue) }
                } else ScenePlayer.choose(s, b.options.indexOfFirst { it.naturalness == Naturalness.NATURAL })
            }
            if (b is Beat.Reply && s.picked?.naturalness == Naturalness.UNNATURAL) {
                s = ScenePlayer.choose(s, b.options.indexOfFirst { it.naturalness == Naturalness.NATURAL })
            }
            s = ScenePlayer.next(s)
        }
        return s
    }

    @Test fun `script has ten chapters of three scenes with missions and unique ids`() {
        assertEquals(10, chapters.size)
        chapters.forEach { ch ->
            assertEquals(ch.id, 3, ch.scenes.size)
            ch.scenes.forEach { sc ->
                assertTrue(sc.id, sc.beats.any { it is Beat.Reply })
                assertTrue(sc.id, sc.beats.any { it is Beat.Shadow })
                assertTrue(sc.id, sc.chunks.isNotEmpty())
                sc.beats.filterIsInstance<Beat.Line>().forEach { assertTrue(sc.id, it.speaker in ch.cast) }
            }
        }
        val sceneIds = chapters.flatMap { it.scenes }.map { it.id }
        assertEquals(sceneIds.size, sceneIds.toSet().size)
        val chunkIds = chapters.flatMap { it.scenes }.flatMap { it.chunks }.map { it.id }
        assertEquals(chunkIds.size, chunkIds.toSet().size)
        val stampCodes = chapters.flatMap { listOf(it.code, it.mission.stampCode) }
        assertEquals(stampCodes.size, stampCodes.toSet().size)
    }

    @Test fun `every scene can be played to the end and unnatural replies block progress`() {
        chapters.flatMap { it.scenes }.forEach { scene ->
            val end = playThrough(ScenePlayer.start(scene), firstWrong = true)
            assertTrue(scene.id, end.isFinished)
        }
    }

    @Test fun `first attempt naturalness is what counts`() {
        val scene = chapters[1].scenes[0]
        val end = playThrough(ScenePlayer.start(scene), firstWrong = true)
        assertTrue(end.firstReplies.values.any { it == Naturalness.UNNATURAL })
        assertTrue(end.slips.isNotEmpty())
        assertEquals(1, end.shadowsDone)
    }

    @Test fun `chapters unlock in order and next scene follows progress`() {
        var p = JourneyProgress()
        assertEquals(chapters[0].scenes[0], Journey.nextScene(chapters, p))
        val views = Journey.chapterViews(chapters, p)
        assertEquals(ChapterStatus.CURRENT, views[0].status)
        assertEquals(ChapterStatus.LOCKED, views[1].status)

        chapters[0].scenes.forEach { sc ->
            p = Journey.completeScene(p, chapters[0], playThrough(ScenePlayer.start(sc)), today = 100, seconds = 120).first
        }
        assertEquals(ChapterStatus.DONE, Journey.chapterViews(chapters, p)[0].status)
        assertEquals(chapters[1].scenes[0], Journey.nextScene(chapters, p))
        assertEquals(chapters[0].mission, Journey.activeMission(chapters, p))
        assertTrue(Journey.stamps(chapters, p).first { it.code == "RUH" }.earned)
    }

    @Test fun `finishing a scene fills the notebook and logs the day`() {
        val sc = chapters[0].scenes[0]
        val (p, summary) = Journey.completeScene(JourneyProgress(), chapters[0], playThrough(ScenePlayer.start(sc)), 100, 300)
        assertEquals(sc.chunks.size, summary.newPhrases)
        assertEquals(sc.chunks.size, p.notebook.size)
        assertEquals(5, Journey.minutesToday(p, 100))
        assertEquals(1, Journey.commitmentDays(p, 100))
        assertEquals(1, Journey.commitmentDays(p, 101))
        assertEquals(0, Journey.commitmentDays(p, 102))
    }

    @Test fun `notebook reviews follow SM-2`() {
        val sc = chapters[0].scenes[0]
        var p = Journey.completeScene(JourneyProgress(), chapters[0], playThrough(ScenePlayer.start(sc)), 100, 60).first
        assertTrue(Journey.dueEntries(p, 100).isEmpty())
        val due = Journey.dueEntries(p, 101)
        assertEquals(p.notebook.size, due.size)
        val id = due.first().chunkId
        p = Journey.review(p, id, 5, 101)
        p = Journey.review(p, id, 5, 102)
        assertEquals(108L, p.notebook.getValue(id).dueDay)
        p = Journey.review(p, id, 1, 108)
        assertEquals(0, p.notebook.getValue(id).repetitions)
    }

    @Test fun `missions can be accepted and completed for a passport stamp`() {
        val m = chapters[0].mission
        var p = Journey.acceptMission(JourneyProgress(), m, 5)
        assertNull(p.missions.getValue(m.id).completedDay)
        p = Journey.completeMission(p, m, 6, "  طلبت قهوتي!  ")
        assertEquals("طلبت قهوتي!", p.missions.getValue(m.id).note)
        assertTrue(Journey.stamps(chapters, p).first { it.code == m.stampCode }.earned)
    }

    @Test fun `progress round-trips through json`() {
        val sc = chapters[0].scenes[0]
        val p = Journey.completeScene(JourneyProgress(), chapters[0], playThrough(ScenePlayer.start(sc)), 100, 60).first
        val json = Json { ignoreUnknownKeys = true }
        assertEquals(p, json.decodeFromString(JourneyProgress.serializer(), json.encodeToString(JourneyProgress.serializer(), p)))
    }

    @Test fun `answer matcher and season calendar still behave`() {
        assertTrue(AnswerMatcher.pronunciationAccepted(listOf("i'm here on holiday"), listOf("I'm here on holiday.")))
        assertEquals(Season.RAMADAN, SeasonCalendar.seasonOn(LocalDate.of(2026, 3, 5)))
    }
}
