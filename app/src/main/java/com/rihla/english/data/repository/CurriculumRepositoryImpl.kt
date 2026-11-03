package com.rihla.english.data.repository

import com.rihla.english.core.curriculum.SeedCurriculumData
import com.rihla.english.core.engine.Sm2Scheduler
import com.rihla.english.core.engine.Sm2State
import com.rihla.english.core.model.CourseUnit
import com.rihla.english.core.model.ErrorPattern
import com.rihla.english.core.model.Exercise
import com.rihla.english.core.model.GrammarBridge
import com.rihla.english.core.model.NodeType
import com.rihla.english.core.model.PathNode
import com.rihla.english.core.model.VocabItem
import com.rihla.english.core.repository.CurriculumRepository
import com.rihla.english.core.repository.ExerciseOutcome
import com.rihla.english.data.local.CurriculumDao
import com.rihla.english.data.local.ExerciseEntity
import com.rihla.english.data.local.MistakeEntity
import com.rihla.english.data.local.NodeEntity
import com.rihla.english.data.local.NodeProgressEntity
import com.rihla.english.data.local.ProgressDao
import com.rihla.english.data.local.UnitEntity
import com.rihla.english.data.local.VocabEntity
import com.rihla.english.data.local.VocabMasteryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** Room-backed curriculum + progress repository (offline-first: all content is local). */
class CurriculumRepositoryImpl(
    private val curriculumDao: CurriculumDao,
    private val progressDao: ProgressDao,
    private val json: Json = ContentJson,
) : CurriculumRepository {

    private val seedMutex = Mutex()

    override suspend fun ensureSeeded() = seedMutex.withLock {
        val stored = curriculumDao.seedVersion()
        if (stored != null && stored >= SeedCurriculumData.VERSION) return@withLock

        val units = SeedCurriculumData.units
        val unitEntities = units.map {
            UnitEntity(it.id, it.orderIndex, it.titleAr, it.titleEn, it.descriptionAr, it.colorHex)
        }
        val nodes = units.flatMap { it.nodes }
        val nodeEntities = nodes.map { n ->
            NodeEntity(
                id = n.id,
                unitId = n.unitId,
                orderIndex = n.orderIndex,
                type = n.type.name,
                titleAr = n.titleAr,
                titleEn = n.titleEn,
                iconEmoji = n.iconEmoji,
                introTipAr = n.introTipAr,
                grammarBridgeJson = n.grammarBridge?.let { json.encodeToString(it) },
            )
        }
        val exerciseEntities = nodes.flatMap { n ->
            n.exercises.mapIndexed { i, e ->
                ExerciseEntity(e.id, n.id, i, e.type.name, json.encodeToString<Exercise>(e))
            }
        }
        val vocabEntities = nodes.flatMap { n ->
            n.vocabulary.map { VocabEntity(it.id, n.id, it.english, it.arabic, it.emoji) }
        }.distinctBy { it.id }

        curriculumDao.replaceContent(unitEntities, nodeEntities, exerciseEntities, vocabEntities, SeedCurriculumData.VERSION)
    }

    override fun observeUnits(): Flow<List<CourseUnit>> =
        combine(curriculumDao.observeUnits(), curriculumDao.observeNodes()) { units, nodes ->
            val byUnit = nodes.groupBy { it.unitId }
            units.map { u ->
                CourseUnit(
                    id = u.id,
                    orderIndex = u.orderIndex,
                    titleAr = u.titleAr,
                    titleEn = u.titleEn,
                    descriptionAr = u.descriptionAr,
                    colorHex = u.colorHex,
                    nodes = byUnit[u.id].orEmpty().sortedBy { it.orderIndex }.map { it.toDomain(emptyList(), emptyList()) },
                )
            }
        }

    override fun observeCompletedNodeIds(): Flow<Set<String>> =
        progressDao.observeCompletedNodeIds().map { it.toSet() }.distinctUntilChanged()

    override fun observeWordsLearned(): Flow<Int> = progressDao.observeMasteryCount()

    override fun observeMistakeCount(): Flow<Int> = progressDao.observeMistakeCount()

    override suspend fun getNode(nodeId: String): PathNode? {
        val node = curriculumDao.getNode(nodeId) ?: return null
        val exercises = curriculumDao.getExercises(nodeId).mapNotNull { it.decode() }
        val vocab = curriculumDao.getVocabulary(nodeId).map { VocabItem(it.id, it.english, it.arabic, it.emoji) }
        return node.toDomain(exercises, vocab)
    }

    override suspend fun markNodeCompleted(nodeId: String, accuracyPercent: Int, nowMillis: Long) {
        val existing = progressDao.getNodeProgress(nodeId)
        progressDao.upsertNodeProgress(
            NodeProgressEntity(
                nodeId = nodeId,
                firstCompletedAtMillis = existing?.firstCompletedAtMillis ?: nowMillis,
                lastCompletedAtMillis = nowMillis,
                timesCompleted = (existing?.timesCompleted ?: 0) + 1,
                bestAccuracy = maxOf(existing?.bestAccuracy ?: 0, accuracyPercent),
            ),
        )
    }

    override suspend fun buildPracticeSession(todayEpochDay: Long, maxExercises: Int): List<Exercise> {
        val picked = LinkedHashMap<String, Exercise>()

        // 1) Past mistakes, most recent first (up to 60% of the session).
        val mistakeIds = progressDao.recentMistakes(limit = (maxExercises * 0.6).toInt().coerceAtLeast(1)).map { it.exerciseId }
        if (mistakeIds.isNotEmpty()) {
            curriculumDao.getExercisesByIds(mistakeIds).mapNotNull { it.decode() }
                .sortedBy { mistakeIds.indexOf(it.id) }
                .forEach { picked[it.id] = it }
        }

        // 2) Exercises that practise SM-2 due vocabulary, from completed lessons.
        val completed = progressDao.observeCompletedNodeIdsOnce()
        if (picked.size < maxExercises && completed.isNotEmpty()) {
            val due = progressDao.dueVocabIds(todayEpochDay, limit = 30).toSet()
            val pool = curriculumDao.getGradedExercisesForNodes(completed).mapNotNull { it.decode() }
            pool.filter { e -> e.vocabIds.any { it in due } }
                .shuffled()
                .forEach { if (picked.size < maxExercises) picked.putIfAbsent(it.id, it) }
            // 3) Top up with random review from completed lessons.
            pool.shuffled().forEach { if (picked.size < maxExercises) picked.putIfAbsent(it.id, it) }
        }

        // 4) Brand-new learner: review the first lesson.
        if (picked.isEmpty()) {
            val firstLesson = SeedCurriculumData.units.first().nodes.first { it.type == NodeType.LESSON }
            curriculumDao.getGradedExercisesForNodes(listOf(firstLesson.id)).mapNotNull { it.decode() }
                .take(maxExercises).forEach { picked[it.id] = it }
        }
        return picked.values.take(maxExercises).shuffled()
    }

    override suspend fun buildPatternSession(pattern: ErrorPattern, maxExercises: Int): List<Exercise> {
        val completed = progressDao.observeCompletedNodeIdsOnce().toSet()
        val allNodeIds = SeedCurriculumData.units.flatMap { it.nodes }.map { it.id }
        val tagged = curriculumDao.getGradedExercisesForNodes(allNodeIds)
            .mapNotNull { entity -> entity.decode()?.let { entity.nodeId to it } }
            .filter { (_, e) -> e.pattern == pattern }
        // Prefer material from lessons already completed, then top up from the rest.
        val (seen, unseen) = tagged.partition { (nodeId, _) -> nodeId in completed }
        return (seen.shuffled() + unseen.shuffled()).map { it.second }.take(maxExercises).shuffled()
    }

    override suspend fun recordOutcomes(
        outcomes: List<ExerciseOutcome>,
        isPractice: Boolean,
        todayEpochDay: Long,
        nowMillis: Long,
    ) {
        if (outcomes.isEmpty()) return

        // --- Mistakes pool
        val missed = outcomes.filter { !it.firstTryCorrect && it.exercise.isGraded }
        if (missed.isNotEmpty()) {
            val existing = progressDao.getMistakes(missed.map { it.exercise.id }).associateBy { it.exerciseId }
            progressDao.upsertMistakes(
                missed.map { o ->
                    MistakeEntity(
                        exerciseId = o.exercise.id,
                        nodeId = o.nodeId ?: existing[o.exercise.id]?.nodeId,
                        missCount = (existing[o.exercise.id]?.missCount ?: 0) + 1,
                        lastMissedAtMillis = nowMillis,
                    )
                },
            )
        }
        if (isPractice) {
            val resolved = outcomes.filter { it.firstTryCorrect }.map { it.exercise.id }
            if (resolved.isNotEmpty()) progressDao.deleteMistakes(resolved)
        }

        // --- SM-2: aggregate per vocabulary item so a word is reviewed once per session.
        data class Agg(var graded: Boolean = false, var allFirstTry: Boolean = true, var allEventually: Boolean = true)
        val perVocab = LinkedHashMap<String, Agg>()
        outcomes.forEach { o ->
            o.exercise.vocabIds.forEach { id ->
                val agg = perVocab.getOrPut(id) { Agg() }
                if (o.exercise.isGraded) {
                    agg.graded = true
                    agg.allFirstTry = agg.allFirstTry && o.firstTryCorrect
                    agg.allEventually = agg.allEventually && o.eventuallyCorrect
                }
            }
        }
        if (perVocab.isEmpty()) return
        val current = progressDao.getMastery(perVocab.keys.toList()).associateBy { it.vocabId }
        val updated = perVocab.mapNotNull { (id, agg) ->
            val state = current[id]?.toState()
            when {
                agg.graded -> Sm2Scheduler.review(
                    state ?: Sm2State(),
                    Sm2Scheduler.qualityFor(agg.allFirstTry, agg.allEventually),
                    todayEpochDay,
                )
                // Only seen on a flashcard: introduce it, first review tomorrow.
                state == null -> Sm2State(dueEpochDay = todayEpochDay + 1, lastReviewedEpochDay = todayEpochDay)
                else -> null
            }?.toEntity(id)
        }
        if (updated.isNotEmpty()) progressDao.upsertMastery(updated)
    }

    // ------------------------------------------------------------------ mapping helpers

    private suspend fun ProgressDao.observeCompletedNodeIdsOnce(): List<String> =
        observeCompletedNodeIds().first()

    private fun ExerciseEntity.decode(): Exercise? =
        runCatching { json.decodeFromString<Exercise>(payloadJson) }.getOrNull()

    private fun NodeEntity.toDomain(exercises: List<Exercise>, vocab: List<VocabItem>) = PathNode(
        id = id,
        unitId = unitId,
        orderIndex = orderIndex,
        type = runCatching { NodeType.valueOf(type) }.getOrDefault(NodeType.LESSON),
        titleAr = titleAr,
        titleEn = titleEn,
        iconEmoji = iconEmoji,
        introTipAr = introTipAr,
        grammarBridge = grammarBridgeJson?.let { runCatching { json.decodeFromString<GrammarBridge>(it) }.getOrNull() },
        exercises = exercises,
        vocabulary = vocab,
    )

    private fun VocabMasteryEntity.toState() = Sm2State(
        easiness, intervalDays, repetitions, dueEpochDay, lastReviewedEpochDay, timesCorrect, timesWrong,
    )

    private fun Sm2State.toEntity(id: String) = VocabMasteryEntity(
        id, easiness, intervalDays, repetitions, dueEpochDay, lastReviewedEpochDay, timesCorrect, timesWrong,
    )

    companion object {
        val ContentJson = Json {
            ignoreUnknownKeys = true
            classDiscriminator = "kind"
            encodeDefaults = true
        }
    }
}
