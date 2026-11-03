package com.rihla.english.core.model

import kotlinx.serialization.Serializable

/**
 * A unit on the skill tree (e.g. "Alphabet & Essential Phonics").
 * Arabic fields are shown in the RTL chrome; English fields are always rendered LTR.
 */
data class CourseUnit(
    val id: String,
    val orderIndex: Int,
    val titleAr: String,
    val titleEn: String,
    val descriptionAr: String,
    val colorHex: Long,
    val nodes: List<PathNode>,
)

/** Kind of node on the winding skill path. */
enum class NodeType {
    /** A regular lesson made of exercises. */
    LESSON,

    /** A treasure chest that grants gems once the preceding lesson is complete. */
    CHEST,
}

/**
 * A node on the snake path. A [NodeType.LESSON] node owns exercises, a
 * [NodeType.CHEST] node owns a reward.
 */
data class PathNode(
    val id: String,
    val unitId: String,
    val orderIndex: Int,
    val type: NodeType,
    val titleAr: String,
    val titleEn: String,
    val iconEmoji: String,
    /** Short Arabic tip shown before the lesson starts. */
    val introTipAr: String? = null,
    /** Optional visual grammar bridge shown before the lesson starts. */
    val grammarBridge: GrammarBridge? = null,
    val exercises: List<Exercise> = emptyList(),
    val vocabulary: List<VocabItem> = emptyList(),
)

/** A single vocabulary item tracked by the SM-2 scheduler. */
@Serializable
data class VocabItem(
    val id: String,
    val english: String,
    val arabic: String,
    val emoji: String = "",
)

/**
 * A visual "bridge" between an Arabic nominal sentence and its English
 * equivalent. English tokens that have no Arabic counterpart (the copula
 * "am/is/are", articles "a/an") are flagged with [BridgeToken.arabicIndex] = null
 * so the UI can highlight them as "the hidden word Arabic doesn't write".
 */
@Serializable
data class GrammarBridge(
    val arabicTokens: List<String>,
    val englishTokens: List<BridgeToken>,
    val explanationAr: String,
)

@Serializable
data class BridgeToken(
    val text: String,
    /** Index into [GrammarBridge.arabicTokens] this English word translates, or null if Arabic omits it. */
    val arabicIndex: Int?,
)
