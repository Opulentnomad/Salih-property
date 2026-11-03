package com.rihla.english.core.engine

import com.rihla.english.core.model.Exercise
import com.rihla.english.core.model.FlashcardAudio
import com.rihla.english.core.model.ListeningChallenge
import com.rihla.english.core.model.ListeningMode
import com.rihla.english.core.model.MatchPairs
import com.rihla.english.core.model.MultipleChoice
import com.rihla.english.core.model.SpeakingChallenge
import com.rihla.english.core.model.WordBankSentence

/** What the learner submitted for the current exercise. */
sealed interface UserAnswer {
    /** Flashcard acknowledged ("Continue"). */
    data object Acknowledged : UserAnswer
    data class Choice(val index: Int) : UserAnswer
    data class Tokens(val words: List<String>) : UserAnswer
    data class MatchCompleted(val mistakes: Int) : UserAnswer
    data class Speech(val candidates: List<String>) : UserAnswer
}

data class GradeResult(
    val correct: Boolean,
    /** The correct answer to display (English, LTR) when wrong. */
    val correctAnswer: String?,
    /** Arabic explanation shown on wrong answers. */
    val explanationAr: String?,
    /** Pronunciation similarity 0..1 for speaking exercises. */
    val score: Double? = null,
)

/** Grades a [UserAnswer] against an [Exercise]. Pure function — shared by UI and tests. */
object ExerciseGrader {

    fun grade(exercise: Exercise, answer: UserAnswer): GradeResult = when (exercise) {
        is FlashcardAudio -> GradeResult(true, null, null)

        is MultipleChoice -> {
            val ok = (answer as? UserAnswer.Choice)?.index == exercise.correctIndex
            GradeResult(ok, exercise.options[exercise.correctIndex], exercise.explanationAr)
        }

        is WordBankSentence -> {
            val given = (answer as? UserAnswer.Tokens)?.words.orEmpty().joinToString(" ")
            val ok = AnswerMatcher.sentenceMatches(given, listOf(exercise.answerSentence) + exercise.alternativeAnswers)
            GradeResult(ok, exercise.answerSentence, exercise.explanationAr)
        }

        is MatchPairs -> {
            val mistakes = (answer as? UserAnswer.MatchCompleted)?.mistakes ?: Int.MAX_VALUE
            GradeResult(
                correct = mistakes == 0,
                correctAnswer = exercise.pairs.joinToString("  •  ") { "${it.english} = ${it.other}" },
                explanationAr = "حاول مطابقة كل الأزواج دون أخطاء. ستظهر هذه المجموعة مرة أخرى في آخر الدرس.",
            )
        }

        is ListeningChallenge -> {
            val ok = when (exercise.mode) {
                ListeningMode.SELECT -> (answer as? UserAnswer.Choice)?.index == exercise.correctIndex
                ListeningMode.ASSEMBLE -> AnswerMatcher.sentenceMatches(
                    (answer as? UserAnswer.Tokens)?.words.orEmpty().joinToString(" "),
                    listOf(exercise.audioText),
                )
            }
            GradeResult(ok, exercise.audioText, exercise.explanationAr)
        }

        is SpeakingChallenge -> {
            val candidates = (answer as? UserAnswer.Speech)?.candidates.orEmpty()
            val targets = listOf(exercise.targetText) + exercise.acceptedVariants
            val score = AnswerMatcher.pronunciationScore(candidates, targets)
            GradeResult(
                correct = score >= AnswerMatcher.PRONUNCIATION_THRESHOLD,
                correctAnswer = exercise.targetText,
                explanationAr = exercise.tipAr,
                score = score,
            )
        }
    }
}
