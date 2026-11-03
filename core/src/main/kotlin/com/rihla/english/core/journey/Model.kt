package com.rihla.english.core.journey

import com.rihla.english.core.model.ErrorPattern

/** Hand-drawn (code-drawn) backdrops for scenes. */
enum class SceneArt { DEPARTURES, PLANE, ARRIVALS, TAXI, HOTEL, CAFE, TUBE, SHOP, CLINIC, CAMPUS, OFFICE, DINNER }

/** A ready-made phrase ("lexical chunk") highlighted inside a line and collectable in the notebook. */
data class Chunk(val id: String, val english: String, val arabic: String)

enum class Naturalness { NATURAL, OK, UNNATURAL }

data class ReplyOption(
    val english: String,
    val arabic: String,
    val naturalness: Naturalness,
    /** Arabic coaching: why this sounds natural or not. */
    val noteAr: String,
    /** Typical Arabic-speaker trap behind an unnatural reply. */
    val pattern: ErrorPattern? = null,
)

/** One step of a scene. */
sealed interface Beat {
    /** Arabic narration that sets the scene, shown like a film caption. */
    data class Narration(val arabic: String) : Beat

    /** A character speaks (comprehensible input). [chunk] is highlighted and collectable. */
    data class Line(val speaker: String, val english: String, val arabic: String, val chunk: Chunk? = null) : Beat

    /** The learner chooses what to say; unnatural replies get coaching and a retry. */
    data class Reply(val promptAr: String, val options: List<ReplyOption>) : Beat

    /** Shadowing: hear a phrase, then repeat it with the same rhythm. */
    data class Shadow(val english: String, val arabic: String, val phonetic: String, val tipAr: String) : Beat
}

data class Scene(
    val id: String,
    val chapterId: String,
    val index: Int,
    val titleAr: String,
    val art: SceneArt,
    val beats: List<Beat>,
) {
    val chunks: List<Chunk> get() = beats.filterIsInstance<Beat.Line>().mapNotNull { it.chunk }
}

/** A real-life task to do outside the app after a chapter. */
data class Mission(
    val id: String,
    val titleAr: String,
    val descriptionAr: String,
    val phrases: List<String>,
    val stampCode: String,
    val stampEn: String,
)

data class Chapter(
    val id: String,
    val number: Int,
    /** Passport stamp code, e.g. "LHR". */
    val code: String,
    val stampEn: String,
    val titleAr: String,
    val titleEn: String,
    val subtitleAr: String,
    val art: SceneArt,
    /** Character id → Arabic display name. */
    val cast: Map<String, String>,
    val scenes: List<Scene>,
    val mission: Mission,
)
