package com.rihla.english.core.curriculum

import com.rihla.english.core.engine.AnswerMatcher
import com.rihla.english.core.model.BridgeToken
import com.rihla.english.core.model.CourseUnit
import com.rihla.english.core.model.ErrorCorrection
import com.rihla.english.core.model.ErrorPattern
import com.rihla.english.core.model.Exercise
import com.rihla.english.core.model.FlashcardAudio
import com.rihla.english.core.model.GrammarBridge
import com.rihla.english.core.model.ListeningChallenge
import com.rihla.english.core.model.ListeningMode
import com.rihla.english.core.model.MatchMode
import com.rihla.english.core.model.MatchPair
import com.rihla.english.core.model.MatchPairs
import com.rihla.english.core.model.MultipleChoice
import com.rihla.english.core.model.NodeType
import com.rihla.english.core.model.PathNode
import com.rihla.english.core.model.SpeakingChallenge
import com.rihla.english.core.model.VocabItem
import com.rihla.english.core.model.WordBankSentence
import com.rihla.english.core.model.withTags
import kotlin.random.Random

/*
 * A tiny DSL used by SeedCurriculumData to keep hundreds of exercises readable.
 * Ids are deterministic (u1, u1_l1, u1_l1_e3, u1_c1) so progress survives re-seeding.
 */

fun vocabIdFor(english: String): String = "v_" + AnswerMatcher.normalize(english).replace(' ', '_')

fun bridge(arabic: List<String>, vararg english: Pair<String, Int?>, explanation: String) = GrammarBridge(
    arabicTokens = arabic,
    englishTokens = english.map { (text, index) -> BridgeToken(text, index) },
    explanationAr = explanation,
)

@DslMarker
annotation class CurriculumDslMarker

@CurriculumDslMarker
class UnitBuilder(private val unitId: String) {
    private val nodes = mutableListOf<PathNode>()
    private var lessonCount = 0
    private var chestCount = 0

    fun lesson(
        titleAr: String,
        titleEn: String,
        icon: String,
        intro: String? = null,
        grammarBridge: GrammarBridge? = null,
        pattern: ErrorPattern? = null,
        content: LessonBuilder.() -> Unit,
    ) {
        lessonCount++
        val id = "${unitId}_l$lessonCount"
        val builder = LessonBuilder(id, pattern).apply(content)
        nodes += PathNode(
            id = id,
            unitId = unitId,
            orderIndex = nodes.size,
            type = NodeType.LESSON,
            titleAr = titleAr,
            titleEn = titleEn,
            iconEmoji = icon,
            introTipAr = intro,
            grammarBridge = grammarBridge,
            exercises = builder.buildExercises(),
            vocabulary = builder.vocabulary,
        )
    }

    fun chest(titleAr: String = "صندوق كنز") {
        chestCount++
        nodes += PathNode(
            id = "${unitId}_c$chestCount",
            unitId = unitId,
            orderIndex = nodes.size,
            type = NodeType.CHEST,
            titleAr = titleAr,
            titleEn = "Treasure chest",
            iconEmoji = "🎁",
        )
    }

    fun build(order: Int, titleAr: String, titleEn: String, descriptionAr: String, color: Long) = CourseUnit(
        id = unitId,
        orderIndex = order,
        titleAr = titleAr,
        titleEn = titleEn,
        descriptionAr = descriptionAr,
        colorHex = color,
        nodes = nodes.toList(),
    )
}

@CurriculumDslMarker
class LessonBuilder(private val lessonId: String, private val lessonPattern: ErrorPattern? = null) {
    private val exercises = mutableListOf<Exercise>()
    val vocabulary = mutableListOf<VocabItem>()

    private fun nextId() = "${lessonId}_e${exercises.size + 1}"

    /** Shuffles options deterministically (so the right answer isn't always first). */
    private fun shuffled(id: String, options: List<String>, answer: String): Pair<List<String>, Int> {
        require(answer in options) { "$id: answer '$answer' missing from options $options" }
        val list = options.shuffled(Random(id.hashCode()))
        return list to list.indexOf(answer)
    }

    /** FLASHCARD_AUDIO teaching card; also registers the word as tracked vocabulary. */
    fun card(
        en: String,
        ar: String,
        emoji: String,
        phonetic: String,
        ipa: String,
        tip: String? = null,
        letters: String? = null,
    ) {
        val vocabId = vocabIdFor(en)
        if (vocabulary.none { it.id == vocabId }) vocabulary += VocabItem(vocabId, en, ar, emoji)
        exercises += FlashcardAudio(nextId(), en, ar, emoji, phonetic, ipa, tip, letters)
    }

    /** MULTIPLE_CHOICE with 2–4 options. */
    fun choice(
        question: String,
        prompt: String? = null,
        options: List<String>,
        answer: String,
        english: Boolean,
        why: String,
        audio: String? = null,
    ) {
        val id = nextId()
        val (opts, index) = shuffled(id, options, answer)
        exercises += MultipleChoice(id, question, prompt, opts, index, english, why, audio)
    }

    /** WORD_BANK_SENTENCE: translate the Arabic sentence by tapping English chips. */
    fun bank(
        ar: String,
        answer: String,
        distractors: List<String>,
        why: String,
        alternatives: List<String> = emptyList(),
        grammar: GrammarBridge? = null,
    ) {
        exercises += WordBankSentence(
            id = nextId(),
            arabicSentence = ar,
            answerTokens = answer.split(' '),
            distractors = distractors,
            alternativeAnswers = alternatives,
            explanationAr = why,
            grammarBridge = grammar,
        )
    }

    /** MATCH_PAIRS: exactly four pairs. */
    fun match(vararg pairs: Pair<String, String>, mode: MatchMode = MatchMode.ENGLISH_TO_ARABIC) {
        require(pairs.size == 4) { "$lessonId: match exercises need 4 pairs" }
        exercises += MatchPairs(nextId(), pairs.map { MatchPair(it.first, it.second) }, mode)
    }

    /** LISTENING_CHALLENGE (select): hear [audio] and pick it among [options]. Also used for minimal pairs. */
    fun hear(audio: String, options: List<String>, ar: String, why: String) {
        val id = nextId()
        val (opts, index) = shuffled(id, options, audio)
        exercises += ListeningChallenge(id, audio, ListeningMode.SELECT, opts, index, emptyList(), ar, why)
    }

    /** LISTENING_CHALLENGE (assemble): hear [audio] and build it from chips. */
    fun hearBuild(audio: String, distractors: List<String>, ar: String, why: String) {
        exercises += ListeningChallenge(nextId(), audio, ListeningMode.ASSEMBLE, distractors = distractors, translationAr = ar, explanationAr = why)
    }

    /** SPEAKING_CHALLENGE validated by speech recognition; [confusables] detect minimal-pair slips. */
    fun say(
        text: String,
        ar: String,
        phonetic: String,
        tip: String,
        variants: List<String> = emptyList(),
        confusables: List<String> = emptyList(),
        pattern: ErrorPattern? = null,
    ) {
        exercises += SpeakingChallenge(nextId(), text, ar, phonetic, tip, variants, confusables, pattern = pattern)
    }

    /**
     * ERROR_CORRECTION ("spot the mistake"): [wrong] contains one typical error at [wrongWord];
     * [answer] is the right replacement ("" = delete the word) among [options].
     */
    fun fix(
        wrong: String,
        wrongWord: String,
        options: List<String>,
        answer: String,
        ar: String,
        why: String,
        pattern: ErrorPattern? = null,
    ) {
        val id = nextId()
        val tokens = wrong.split(' ')
        val wrongIndex = tokens.indexOf(wrongWord)
        require(wrongIndex >= 0) { "$id: '$wrongWord' not found in '$wrong'" }
        val (opts, index) = shuffled(id, options, answer)
        exercises += ErrorCorrection(id, tokens, wrongIndex, opts, index, ar, why, pattern = pattern)
    }

    /**
     * Tags each exercise with the lesson vocabulary it contains (whole-word match) and with its
     * mistake pattern (its own, or the lesson's default for graded exercises).
     */
    fun buildExercises(): List<Exercise> = exercises.map { exercise ->
        val content = " " + AnswerMatcher.normalize(exercise.englishContent()) + " "
        val ids = vocabulary.filter { item ->
            content.contains(" " + AnswerMatcher.normalize(item.english) + " ")
        }.map { it.id }
        val pattern = exercise.pattern ?: lessonPattern.takeIf { exercise.isGraded }
        exercise.withTags(ids, pattern)
    }
}

fun courseUnit(
    id: String,
    order: Int,
    titleAr: String,
    titleEn: String,
    descriptionAr: String,
    color: Long,
    content: UnitBuilder.() -> Unit,
): CourseUnit = UnitBuilder(id).apply(content).build(order, titleAr, titleEn, descriptionAr, color)
