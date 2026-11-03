package com.rihla.english.core

import com.rihla.english.core.curriculum.SeedCurriculumData
import com.rihla.english.core.model.Exercise
import com.rihla.english.core.model.ExerciseType
import com.rihla.english.core.model.ListeningChallenge
import com.rihla.english.core.model.ListeningMode
import com.rihla.english.core.model.MatchPairs
import com.rihla.english.core.model.MultipleChoice
import com.rihla.english.core.model.NodeType
import com.rihla.english.core.model.WordBankSentence
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SeedCurriculumDataTest {
    private val units = SeedCurriculumData.units
    private val lessons = units.flatMap { it.nodes }.filter { it.type == NodeType.LESSON }
    private val exercises = lessons.flatMap { it.exercises }

    @Test fun `curriculum has four units with lessons and chests`() {
        assertEquals(4, units.size)
        units.forEach { unit ->
            assertTrue(unit.nodes.any { it.type == NodeType.LESSON })
            assertTrue(unit.nodes.any { it.type == NodeType.CHEST })
        }
        assertTrue("expected a substantial course", exercises.size > 150)
    }

    @Test fun `all ids are unique`() {
        val nodeIds = units.flatMap { it.nodes }.map { it.id }
        assertEquals(nodeIds.size, nodeIds.toSet().size)
        assertEquals(exercises.size, exercises.map { it.id }.toSet().size)
    }

    @Test fun `every lesson uses all six exercise types except where noted`() {
        lessons.forEach { lesson ->
            val types = lesson.exercises.map { it.type }.toSet()
            assertTrue("${lesson.id} lacks flashcards", ExerciseType.FLASHCARD_AUDIO in types)
            assertTrue("${lesson.id} lacks speaking", ExerciseType.SPEAKING_CHALLENGE in types)
            assertTrue("${lesson.id} lacks listening", ExerciseType.LISTENING_CHALLENGE in types)
            assertTrue("${lesson.id} lacks matching", ExerciseType.MATCH_PAIRS in types)
            assertTrue("${lesson.id} lacks multiple choice", ExerciseType.MULTIPLE_CHOICE in types)
        }
        ExerciseType.entries.forEach { type -> assertTrue(exercises.any { it.type == type }) }
    }

    @Test fun `exercise payloads are internally consistent`() {
        exercises.forEach { e ->
            when (e) {
                is MultipleChoice -> {
                    assertTrue(e.id, e.options.size in 2..4)
                    assertTrue(e.id, e.correctIndex in e.options.indices)
                }
                is WordBankSentence -> {
                    assertTrue(e.id, e.distractors.none { d -> e.answerTokens.any { it.equals(d, ignoreCase = true) } })
                }
                is ListeningChallenge -> if (e.mode == ListeningMode.SELECT) {
                    assertEquals(e.id, e.audioText, e.options[e.correctIndex])
                } else {
                    assertTrue(e.id, e.distractors.none { d -> e.answerTokens.any { it.equals(d, ignoreCase = true) } })
                }
                is MatchPairs -> {
                    assertEquals(e.id, 4, e.pairs.size)
                    assertEquals(e.id, 4, e.pairs.map { it.other }.toSet().size)
                }
                else -> Unit
            }
        }
    }

    @Test fun `vocabulary is tagged on exercises`() {
        lessons.forEach { lesson ->
            assertTrue(lesson.id, lesson.vocabulary.isNotEmpty())
            assertTrue(lesson.id, lesson.exercises.count { it.vocabIds.isNotEmpty() } >= lesson.exercises.size / 2)
        }
    }

    @Test fun `exercises round-trip through json`() {
        val json = Json { ignoreUnknownKeys = true; classDiscriminator = "kind" }
        exercises.forEach { e ->
            val text = json.encodeToString<Exercise>(e)
            assertEquals(e, json.decodeFromString<Exercise>(text))
        }
    }
}
