package com.rihla.english.core.story

/**
 * Interactive dialogue stories set in real Gulf-life situations (airport, driver, clinic, ...).
 * A story is a script of lines spoken by characters and choice points where the learner picks
 * their English reply. A weak reply triggers a realistic reaction and the learner tries again.
 */
data class Story(
    val id: String,
    val titleAr: String,
    val titleEn: String,
    val emoji: String,
    val scenarioAr: String,
    val colorHex: Long,
    /** A lesson node that must be completed first, or null if open from the start. */
    val requiredNodeId: String?,
    val characters: Map<String, StoryCharacter>,
    val steps: List<StoryStep>,
    val endingAr: String,
) {
    val choiceCount: Int get() = steps.count { it is StoryStep.Choice }
}

data class StoryCharacter(val id: String, val nameAr: String, val emoji: String)

enum class ReplyQuality { GOOD, OK, BAD }

data class StoryLine(val speakerId: String, val english: String, val arabic: String)

data class StoryOption(
    val english: String,
    val arabic: String,
    val quality: ReplyQuality,
    /** Arabic coaching shown after picking this option. */
    val feedbackAr: String,
    /** For BAD replies: how the other person reacts before the learner tries again. */
    val reaction: StoryLine? = null,
)

sealed interface StoryStep {
    data class Line(val line: StoryLine) : StoryStep
    data class Choice(val promptAr: String, val options: List<StoryOption>) : StoryStep
}

/** One bubble in the chat transcript. */
sealed interface TranscriptItem {
    data class Spoken(val line: StoryLine) : TranscriptItem
    data class MyReply(val option: StoryOption) : TranscriptItem
}

/** Immutable playback state of a story. */
data class StoryState(
    val story: Story,
    val stepIndex: Int = 0,
    val transcript: List<TranscriptItem> = emptyList(),
    /** Points per choice point, from the first attempt only: GOOD = 2, OK = 1, BAD = 0. */
    val firstAttemptPoints: Map<Int, Int> = emptyMap(),
    val lastFeedback: StoryOption? = null,
) {
    val currentStep: StoryStep? get() = story.steps.getOrNull(stepIndex)
    val isFinished: Boolean get() = stepIndex >= story.steps.size
    val progress: Float get() = if (story.steps.isEmpty()) 1f else stepIndex.toFloat() / story.steps.size

    /** 1–3 stars from first-attempt reply quality. */
    val stars: Int
        get() {
            val max = story.choiceCount * 2
            if (max == 0) return 3
            val ratio = firstAttemptPoints.values.sum().toDouble() / max
            return when {
                ratio >= 0.9 -> 3
                ratio >= 0.6 -> 2
                else -> 1
            }
        }
}

/** Pure state machine for story playback (UI drives it, tests verify it). */
object StoryEngine {

    fun start(story: Story): StoryState = StoryState(story)

    /** Reveals the current spoken line and moves on. No-op on a choice point. */
    fun advance(state: StoryState): StoryState {
        val step = state.currentStep as? StoryStep.Line ?: return state
        return state.copy(
            stepIndex = state.stepIndex + 1,
            transcript = state.transcript + TranscriptItem.Spoken(step.line),
            lastFeedback = null,
        )
    }

    /**
     * Picks a reply at the current choice point. GOOD/OK replies move the story on; a BAD reply
     * shows the other person's reaction and keeps the learner on the same choice to retry.
     */
    fun choose(state: StoryState, optionIndex: Int): StoryState {
        val step = state.currentStep as? StoryStep.Choice ?: return state
        val option = step.options.getOrNull(optionIndex) ?: return state
        val points = when (option.quality) {
            ReplyQuality.GOOD -> 2
            ReplyQuality.OK -> 1
            ReplyQuality.BAD -> 0
        }
        val scored = if (state.stepIndex in state.firstAttemptPoints) state.firstAttemptPoints
        else state.firstAttemptPoints + (state.stepIndex to points)
        val transcript = state.transcript + TranscriptItem.MyReply(option)
        return if (option.quality == ReplyQuality.BAD) {
            state.copy(
                transcript = transcript + listOfNotNull(option.reaction?.let { TranscriptItem.Spoken(it) }),
                firstAttemptPoints = scored,
                lastFeedback = option,
            )
        } else {
            state.copy(
                stepIndex = state.stepIndex + 1,
                transcript = transcript,
                firstAttemptPoints = scored,
                lastFeedback = option,
            )
        }
    }

    /** XP for finishing a story with [stars] (before any Double XP boost). */
    fun xpFor(stars: Int): Int = 15 + 10 * stars
}

// ------------------------------------------------------------------------------- DSL

@DslMarker
annotation class StoryDsl

@StoryDsl
class StoryBuilder {
    private val characters = LinkedHashMap<String, StoryCharacter>()
    private val steps = mutableListOf<StoryStep>()
    var ending: String = ""

    fun character(id: String, nameAr: String, emoji: String) {
        characters[id] = StoryCharacter(id, nameAr, emoji)
    }

    fun say(speaker: String, english: String, arabic: String) {
        require(speaker in characters) { "unknown speaker $speaker" }
        steps += StoryStep.Line(StoryLine(speaker, english, arabic))
    }

    fun choice(promptAr: String, vararg options: StoryOption) {
        require(options.size in 2..3) { "choices need 2–3 options" }
        require(options.any { it.quality == ReplyQuality.GOOD }) { "each choice needs a GOOD option" }
        require(options.filter { it.quality == ReplyQuality.BAD }.all { it.reaction != null }) { "BAD options need a reaction" }
        steps += StoryStep.Choice(promptAr, options.toList())
    }

    fun good(english: String, arabic: String, feedbackAr: String) =
        StoryOption(english, arabic, ReplyQuality.GOOD, feedbackAr)

    fun ok(english: String, arabic: String, feedbackAr: String) =
        StoryOption(english, arabic, ReplyQuality.OK, feedbackAr)

    fun bad(english: String, arabic: String, feedbackAr: String, by: String, reaction: String, reactionAr: String) =
        StoryOption(english, arabic, ReplyQuality.BAD, feedbackAr, StoryLine(by, reaction, reactionAr))

    fun build(id: String, titleAr: String, titleEn: String, emoji: String, scenarioAr: String, color: Long, requires: String?) =
        Story(id, titleAr, titleEn, emoji, scenarioAr, color, requires, characters.toMap(), steps.toList(), ending)
}

fun story(
    id: String,
    titleAr: String,
    titleEn: String,
    emoji: String,
    scenarioAr: String,
    color: Long,
    requires: String? = null,
    content: StoryBuilder.() -> Unit,
): Story = StoryBuilder().apply(content).build(id, titleAr, titleEn, emoji, scenarioAr, color, requires)
