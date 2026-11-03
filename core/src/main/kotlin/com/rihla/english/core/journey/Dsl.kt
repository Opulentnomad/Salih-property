package com.rihla.english.core.journey

import com.rihla.english.core.model.ErrorPattern

/*
 * Small builder DSL so the story script reads like a screenplay. Ids are stable
 * ("ch2", "ch2_s1", "ch2_s1_purpose-of-your-visit") so saved progress survives content edits.
 */

@DslMarker
annotation class ScriptDsl

@ScriptDsl
class ChapterBuilder(private val id: String) {
    val cast = LinkedHashMap<String, String>()
    private val scenes = mutableListOf<Scene>()
    private var mission: Mission? = null

    fun cast(vararg people: Pair<String, String>) { cast.putAll(people) }

    fun scene(titleAr: String, art: SceneArt, content: SceneBuilder.() -> Unit) {
        val sceneId = "${id}_s${scenes.size + 1}"
        scenes += Scene(sceneId, id, scenes.size, titleAr, art, SceneBuilder(sceneId, cast).apply(content).beats.toList())
    }

    fun mission(titleAr: String, descriptionAr: String, vararg phrases: String, stampCode: String, stampEn: String) {
        mission = Mission("${id}_mission", titleAr, descriptionAr, phrases.toList(), stampCode, stampEn)
    }

    fun build(number: Int, code: String, stampEn: String, titleAr: String, titleEn: String, subtitleAr: String, art: SceneArt) =
        Chapter(id, number, code, stampEn, titleAr, titleEn, subtitleAr, art, cast.toMap(), scenes.toList(),
            checkNotNull(mission) { "$id needs a mission" })
}

@ScriptDsl
class SceneBuilder(private val sceneId: String, private val cast: Map<String, String>) {
    val beats = mutableListOf<Beat>()

    fun narrate(arabic: String) { beats += Beat.Narration(arabic) }

    /** A spoken line. [chunk] (english to arabic) must appear verbatim inside [english]. */
    fun say(speaker: String, english: String, arabic: String, chunk: Pair<String, String>? = null) {
        require(speaker in cast) { "$sceneId: unknown speaker $speaker" }
        val c = chunk?.let { (en, ar) ->
            require(english.contains(en)) { "$sceneId: chunk '$en' not in '$english'" }
            Chunk("${sceneId}_" + en.lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-'), en, ar)
        }
        beats += Beat.Line(speaker, english, arabic, c)
    }

    fun reply(promptAr: String, vararg options: ReplyOption) {
        require(options.size in 2..3) { "$sceneId: replies need 2–3 options" }
        require(options.any { it.naturalness == Naturalness.NATURAL }) { "$sceneId: a reply needs a natural option" }
        beats += Beat.Reply(promptAr, options.toList())
    }

    fun shadow(english: String, arabic: String, phonetic: String, tipAr: String) {
        beats += Beat.Shadow(english, arabic, phonetic, tipAr)
    }

    fun natural(english: String, arabic: String, noteAr: String) = ReplyOption(english, arabic, Naturalness.NATURAL, noteAr)
    fun ok(english: String, arabic: String, noteAr: String) = ReplyOption(english, arabic, Naturalness.OK, noteAr)
    fun wrong(english: String, arabic: String, noteAr: String, pattern: ErrorPattern? = null) =
        ReplyOption(english, arabic, Naturalness.UNNATURAL, noteAr, pattern)
}

fun chapter(
    number: Int,
    code: String,
    stampEn: String,
    titleAr: String,
    titleEn: String,
    subtitleAr: String,
    art: SceneArt,
    content: ChapterBuilder.() -> Unit,
): Chapter = ChapterBuilder("ch$number").apply(content).build(number, code, stampEn, titleAr, titleEn, subtitleAr, art)
