package com.rihla.english.core.journey

import com.rihla.english.core.engine.Sm2Scheduler
import com.rihla.english.core.engine.Sm2State
import kotlinx.serialization.Serializable

/** A phrase collected in "دفتري" with its SM-2 review schedule. */
@Serializable
data class NotebookEntry(
    val chunkId: String,
    val english: String,
    val arabic: String,
    val context: String,
    val sourceAr: String,
    val savedDay: Long,
    val easiness: Double = 2.5,
    val intervalDays: Int = 0,
    val repetitions: Int = 0,
    val dueDay: Long = savedDay + 1,
    val reviews: Int = 0,
) {
    fun isDue(today: Long) = dueDay <= today
    /** Considered "mastered" after three successful spaced reviews. */
    val mastered: Boolean get() = repetitions >= 3
}

@Serializable
data class MissionState(val acceptedDay: Long? = null, val completedDay: Long? = null, val note: String = "")

@Serializable
data class DayLog(val seconds: Int = 0, val scenes: Int = 0, val reviews: Int = 0)

/** Everything the learner has done, persisted as JSON. */
@Serializable
data class JourneyProgress(
    val completedScenes: Set<String> = emptySet(),
    val notebook: Map<String, NotebookEntry> = emptyMap(),
    val missions: Map<String, MissionState> = emptyMap(),
    val days: Map<Long, DayLog> = emptyMap(),
    val naturalReplies: Int = 0,
    val totalReplies: Int = 0,
    val shadows: Int = 0,
    /** Arabic-speaker trap → how often it showed up in replies. */
    val slips: Map<String, Int> = emptyMap(),
) {
    fun day(epochDay: Long) = days[epochDay] ?: DayLog()
}

enum class ChapterStatus { DONE, CURRENT, LOCKED }

data class ChapterView(val chapter: Chapter, val status: ChapterStatus, val scenesDone: Int)

enum class StampKind { CHAPTER, MISSION }

data class Stamp(val code: String, val en: String, val labelAr: String, val kind: StampKind, val earned: Boolean)

/** What a finished scene contributed (for the scene-end summary). */
data class SceneSummary(val newPhrases: Int, val naturalReplies: Int, val replies: Int, val shadows: Int)

/** Pure rules for journey progress, the notebook and the passport. */
object Journey {

    const val MAX_SECONDS_PER_SESSION = 20 * 60

    fun chapterViews(chapters: List<Chapter>, p: JourneyProgress): List<ChapterView> {
        var previousDone = true
        return chapters.sortedBy { it.number }.map { ch ->
            val done = ch.scenes.count { it.id in p.completedScenes }
            val complete = done == ch.scenes.size
            val status = when {
                complete -> ChapterStatus.DONE
                previousDone -> ChapterStatus.CURRENT
                else -> ChapterStatus.LOCKED
            }
            previousDone = complete
            ChapterView(ch, status, done)
        }
    }

    /** The next unfinished scene of the current chapter, or null when the journey is complete. */
    fun nextScene(chapters: List<Chapter>, p: JourneyProgress): Scene? =
        chapterViews(chapters, p).firstOrNull { it.status == ChapterStatus.CURRENT }
            ?.chapter?.scenes?.firstOrNull { it.id !in p.completedScenes }

    fun isChapterComplete(chapter: Chapter, p: JourneyProgress) = chapter.scenes.all { it.id in p.completedScenes }

    fun completeScene(
        p: JourneyProgress,
        chapter: Chapter,
        state: SceneState,
        today: Long,
        seconds: Int,
    ): Pair<JourneyProgress, SceneSummary> {
        val scene = state.scene
        val lines = scene.beats.filterIsInstance<Beat.Line>()
        // Every phrase met in a finished scene goes into the notebook (sentence mining).
        var notebook = p.notebook
        var added = 0
        lines.forEach { line ->
            val c = line.chunk ?: return@forEach
            if (c.id !in notebook) {
                notebook = notebook + (c.id to NotebookEntry(c.id, c.english, c.arabic, line.english, "${chapter.titleAr} · ${scene.titleAr}", today))
                added++
            }
        }
        val replies = state.firstReplies.size
        val natural = state.naturalFirstReplies
        val slips = p.slips.toMutableMap()
        state.slips.forEach { slips[it.name] = (slips[it.name] ?: 0) + 1 }
        val day = p.day(today)
        val updated = p.copy(
            completedScenes = p.completedScenes + scene.id,
            notebook = notebook,
            days = p.days + (today to day.copy(seconds = day.seconds + seconds.coerceIn(0, MAX_SECONDS_PER_SESSION), scenes = day.scenes + 1)),
            naturalReplies = p.naturalReplies + natural,
            totalReplies = p.totalReplies + replies,
            shadows = p.shadows + state.shadowsDone,
            slips = slips,
        )
        return updated to SceneSummary(added, natural, replies, state.shadowsDone)
    }

    /** Saving a phrase mid-scene (before the scene is finished). */
    fun saveChunk(p: JourneyProgress, chunk: Chunk, context: String, sourceAr: String, today: Long): JourneyProgress =
        if (chunk.id in p.notebook) p
        else p.copy(notebook = p.notebook + (chunk.id to NotebookEntry(chunk.id, chunk.english, chunk.arabic, context, sourceAr, today)))

    fun dueEntries(p: JourneyProgress, today: Long): List<NotebookEntry> =
        p.notebook.values.filter { it.isDue(today) }.sortedBy { it.dueDay }

    /** Self-graded review: 5 = knew it, 3 = almost, 1 = didn't. */
    fun review(p: JourneyProgress, chunkId: String, quality: Int, today: Long): JourneyProgress {
        val e = p.notebook[chunkId] ?: return p
        val s = Sm2Scheduler.review(Sm2State(e.easiness, e.intervalDays, e.repetitions, e.dueDay, today), quality, today)
        val day = p.day(today)
        return p.copy(
            notebook = p.notebook + (chunkId to e.copy(easiness = s.easiness, intervalDays = s.intervalDays, repetitions = s.repetitions, dueDay = s.dueEpochDay, reviews = e.reviews + 1)),
            days = p.days + (today to day.copy(reviews = day.reviews + 1)),
        )
    }

    fun addTime(p: JourneyProgress, today: Long, seconds: Int): JourneyProgress {
        val day = p.day(today)
        return p.copy(days = p.days + (today to day.copy(seconds = day.seconds + seconds.coerceIn(0, MAX_SECONDS_PER_SESSION))))
    }

    fun acceptMission(p: JourneyProgress, mission: Mission, today: Long): JourneyProgress =
        p.copy(missions = p.missions + (mission.id to (p.missions[mission.id] ?: MissionState()).copy(acceptedDay = today)))

    fun completeMission(p: JourneyProgress, mission: Mission, today: Long, note: String): JourneyProgress {
        val m = p.missions[mission.id] ?: MissionState(acceptedDay = today)
        return p.copy(missions = p.missions + (mission.id to m.copy(completedDay = today, note = note.trim().take(280))))
    }

    /** The mission to show on the home screen: accepted-but-not-done first, else the latest unlocked one. */
    fun activeMission(chapters: List<Chapter>, p: JourneyProgress): Mission? {
        val unlocked = chapters.filter { isChapterComplete(it, p) }.map { it.mission }
        return unlocked.firstOrNull { p.missions[it.id]?.acceptedDay != null && p.missions[it.id]?.completedDay == null }
            ?: unlocked.lastOrNull { p.missions[it.id]?.completedDay == null }
    }

    fun stamps(chapters: List<Chapter>, p: JourneyProgress): List<Stamp> = chapters.sortedBy { it.number }.flatMap { ch ->
        listOf(
            Stamp(ch.code, ch.stampEn, "الفصل ${ch.number} · ${ch.titleAr}", StampKind.CHAPTER, isChapterComplete(ch, p)),
            Stamp(ch.mission.stampCode, ch.mission.stampEn, "مهمة · ${ch.mission.titleAr}", StampKind.MISSION, p.missions[ch.mission.id]?.completedDay != null),
        )
    }

    /** Consecutive active days ending today (or yesterday, so the day isn't "lost" until it ends). */
    fun commitmentDays(p: JourneyProgress, today: Long): Int {
        fun active(d: Long) = p.day(d).let { it.scenes > 0 || it.reviews > 0 }
        var day = if (active(today)) today else today - 1
        var count = 0
        while (active(day)) { count++; day-- }
        return count
    }

    fun minutesToday(p: JourneyProgress, today: Long): Int = p.day(today).seconds / 60
}

/** Repository for journey progress (DataStore in the app). */
interface JourneyRepository {
    val progress: kotlinx.coroutines.flow.Flow<JourneyProgress>
    suspend fun update(transform: (JourneyProgress) -> JourneyProgress)
}

interface ProfileRepository {
    val profile: kotlinx.coroutines.flow.Flow<com.rihla.english.core.model.UserProfile>
    suspend fun updateProfile(transform: (com.rihla.english.core.model.UserProfile) -> com.rihla.english.core.model.UserProfile)
}
