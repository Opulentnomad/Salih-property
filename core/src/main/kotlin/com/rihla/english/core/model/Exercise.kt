package com.rihla.english.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** The six exercise types supported by the polymorphic exercise engine. */
enum class ExerciseType {
    FLASHCARD_AUDIO,
    MULTIPLE_CHOICE,
    WORD_BANK_SENTENCE,
    MATCH_PAIRS,
    LISTENING_CHALLENGE,
    SPEAKING_CHALLENGE,

    /** "Spot the mistake": tap the wrong word in a sentence, then pick its correction. */
    ERROR_CORRECTION,
}

/**
 * Base type of every exercise. Exercises are stored in Room as JSON
 * (kotlinx.serialization polymorphism, discriminator `kind`).
 */
@Serializable
sealed interface Exercise {
    val id: String
    val type: ExerciseType

    /** Vocabulary ids practised by this exercise (feeds the SM-2 scheduler). */
    val vocabIds: List<String>

    /** The Arabic-speaker mistake pattern this exercise trains, if any (feeds the mistake profile). */
    val pattern: ErrorPattern?

    /** All English text in the exercise; used for vocabulary tagging. */
    fun englishContent(): String

    /** Flashcards are teaching cards: they are never "wrong" and never cost hearts. */
    val isGraded: Boolean get() = true
}

/** Teaching card: picture + English word + Arabic meaning + normal/slow TTS + phonetic tip. */
@Serializable
@SerialName("flashcard")
data class FlashcardAudio(
    override val id: String,
    val english: String,
    val arabic: String,
    val emoji: String,
    /** Pronunciation spelled with Arabic letters, e.g. "هَلُو". */
    val arabicPhonetic: String,
    val ipa: String,
    val tipAr: String? = null,
    /** For alphabet cards: the lowercase/uppercase pair, e.g. "A a". */
    val letterPair: String? = null,
    override val vocabIds: List<String> = emptyList(),
    override val pattern: ErrorPattern? = null,
) : Exercise {
    override val type get() = ExerciseType.FLASHCARD_AUDIO
    override val isGraded get() = false
    override fun englishContent() = english
}

/** 4-option quiz. [explanationAr] is shown when the learner answers wrongly. */
@Serializable
@SerialName("multiple_choice")
data class MultipleChoice(
    override val id: String,
    val questionAr: String,
    /** Optional English stem shown LTR in big type (e.g. a word to translate). */
    val englishPrompt: String? = null,
    val options: List<String>,
    val correctIndex: Int,
    /** True when the options are English (rendered LTR), false when Arabic. */
    val optionsAreEnglish: Boolean,
    val explanationAr: String,
    /** If set, the stem can be played aloud. */
    val audioText: String? = null,
    override val vocabIds: List<String> = emptyList(),
    override val pattern: ErrorPattern? = null,
) : Exercise {
    override val type get() = ExerciseType.MULTIPLE_CHOICE
    override fun englishContent() = buildString {
        englishPrompt?.let { append(it).append(' ') }
        if (optionsAreEnglish) append(options[correctIndex])
    }
}

/** Tap word chips in order to translate an Arabic sentence into English. */
@Serializable
@SerialName("word_bank")
data class WordBankSentence(
    override val id: String,
    val arabicSentence: String,
    /** The canonical answer, one chip per token, in order. */
    val answerTokens: List<String>,
    /** Extra chips that do not belong to the answer. */
    val distractors: List<String>,
    /** Other accepted full-sentence answers (compared after normalisation). */
    val alternativeAnswers: List<String> = emptyList(),
    val explanationAr: String,
    val grammarBridge: GrammarBridge? = null,
    override val vocabIds: List<String> = emptyList(),
    override val pattern: ErrorPattern? = null,
) : Exercise {
    override val type get() = ExerciseType.WORD_BANK_SENTENCE
    val answerSentence: String get() = answerTokens.joinToString(" ")
    override fun englishContent() = answerSentence
}

enum class MatchMode {
    /** English word ↔ Arabic translation. */
    ENGLISH_TO_ARABIC,

    /** Audio clip (TTS of the English word) ↔ Arabic translation. */
    AUDIO_TO_ARABIC,

    /** Uppercase letter ↔ lowercase letter (both columns LTR). */
    UPPER_TO_LOWER,
}

@Serializable
data class MatchPair(val english: String, val other: String)

/** 2-column tap-to-match grid. */
@Serializable
@SerialName("match_pairs")
data class MatchPairs(
    override val id: String,
    val pairs: List<MatchPair>,
    val mode: MatchMode = MatchMode.ENGLISH_TO_ARABIC,
    override val vocabIds: List<String> = emptyList(),
    override val pattern: ErrorPattern? = null,
) : Exercise {
    override val type get() = ExerciseType.MATCH_PAIRS
    override fun englishContent() = pairs.joinToString(" ") { it.english }
}

enum class ListeningMode {
    /** Choose the option that matches the audio (also used for minimal-pair phonics drills). */
    SELECT,

    /** Assemble what was heard from word chips. */
    ASSEMBLE,
}

/** Plays English TTS and asks the learner to pick or assemble what they heard. */
@Serializable
@SerialName("listening")
data class ListeningChallenge(
    override val id: String,
    val audioText: String,
    val mode: ListeningMode,
    /** SELECT: the options (English, LTR). */
    val options: List<String> = emptyList(),
    val correctIndex: Int = 0,
    /** ASSEMBLE: extra chips that are not part of [audioText]. */
    val distractors: List<String> = emptyList(),
    val translationAr: String,
    /** Arabic explanation focused on the sound contrast (e.g. P vs B). */
    val explanationAr: String,
    override val vocabIds: List<String> = emptyList(),
    override val pattern: ErrorPattern? = null,
) : Exercise {
    override val type get() = ExerciseType.LISTENING_CHALLENGE
    val answerTokens: List<String> get() = audioText.split(' ').filter { it.isNotBlank() }
    override fun englishContent() = audioText
}

/** Speak the phrase; validated by on-device speech recognition with fuzzy matching. */
@Serializable
@SerialName("speaking")
data class SpeakingChallenge(
    override val id: String,
    val targetText: String,
    val arabic: String,
    val arabicPhonetic: String,
    val tipAr: String,
    val acceptedVariants: List<String> = emptyList(),
    /** Minimal-pair words a learner may produce instead (e.g. "bear" for "pear"). */
    val confusables: List<String> = emptyList(),
    override val vocabIds: List<String> = emptyList(),
    override val pattern: ErrorPattern? = null,
) : Exercise {
    override val type get() = ExerciseType.SPEAKING_CHALLENGE
    override fun englishContent() = targetText
}

/**
 * ERROR_CORRECTION — "Spot the mistake". A sentence containing one typical Arabic-speaker error
 * is shown as word chips; the learner taps the wrong word, then chooses the correct replacement.
 */
@Serializable
@SerialName("error_correction")
data class ErrorCorrection(
    override val id: String,
    /** The faulty sentence, one chip per token. */
    val tokens: List<String>,
    val wrongIndex: Int,
    /** Replacement candidates for the wrong token ("" means "delete this word"). */
    val options: List<String>,
    val correctOption: Int,
    val translationAr: String,
    val explanationAr: String,
    override val vocabIds: List<String> = emptyList(),
    override val pattern: ErrorPattern? = null,
) : Exercise {
    override val type get() = ExerciseType.ERROR_CORRECTION

    /** The corrected sentence. */
    val correctedSentence: String
        get() = tokens.mapIndexedNotNull { i, t -> if (i == wrongIndex) options[correctOption].ifEmpty { null } else t }
            .joinToString(" ")

    override fun englishContent() = correctedSentence
}

/** Copies an exercise with new vocab ids / pattern, regardless of concrete type. */
fun Exercise.withTags(ids: List<String>, pattern: ErrorPattern?): Exercise = when (this) {
    is FlashcardAudio -> copy(vocabIds = ids, pattern = pattern)
    is MultipleChoice -> copy(vocabIds = ids, pattern = pattern)
    is WordBankSentence -> copy(vocabIds = ids, pattern = pattern)
    is MatchPairs -> copy(vocabIds = ids, pattern = pattern)
    is ListeningChallenge -> copy(vocabIds = ids, pattern = pattern)
    is SpeakingChallenge -> copy(vocabIds = ids, pattern = pattern)
    is ErrorCorrection -> copy(vocabIds = ids, pattern = pattern)
}
