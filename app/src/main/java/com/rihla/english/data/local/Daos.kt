package com.rihla.english.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
abstract class CurriculumDao {
    @Query("SELECT * FROM units ORDER BY orderIndex")
    abstract fun observeUnits(): Flow<List<UnitEntity>>

    @Query("SELECT * FROM nodes ORDER BY orderIndex")
    abstract fun observeNodes(): Flow<List<NodeEntity>>

    @Query("SELECT * FROM nodes WHERE id = :nodeId")
    abstract suspend fun getNode(nodeId: String): NodeEntity?

    @Query("SELECT * FROM exercises WHERE nodeId = :nodeId ORDER BY orderIndex")
    abstract suspend fun getExercises(nodeId: String): List<ExerciseEntity>

    @Query("SELECT * FROM exercises WHERE id IN (:ids)")
    abstract suspend fun getExercisesByIds(ids: List<String>): List<ExerciseEntity>

    @Query("SELECT * FROM exercises WHERE nodeId IN (:nodeIds) AND type != 'FLASHCARD_AUDIO'")
    abstract suspend fun getGradedExercisesForNodes(nodeIds: List<String>): List<ExerciseEntity>

    @Query("SELECT * FROM vocabulary WHERE nodeId = :nodeId")
    abstract suspend fun getVocabulary(nodeId: String): List<VocabEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertUnits(units: List<UnitEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertNodes(nodes: List<NodeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertExercises(exercises: List<ExerciseEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertVocabulary(vocab: List<VocabEntity>)

    @Query("DELETE FROM units") abstract suspend fun clearUnits()
    @Query("DELETE FROM nodes") abstract suspend fun clearNodes()
    @Query("DELETE FROM exercises") abstract suspend fun clearExercises()
    @Query("DELETE FROM vocabulary") abstract suspend fun clearVocabulary()

    @Query("SELECT version FROM seed_meta WHERE id = 0")
    abstract suspend fun seedVersion(): Int?

    @Upsert
    abstract suspend fun setSeedMeta(meta: SeedMetaEntity)

    /** Replaces all content atomically. Progress tables are untouched. */
    @Transaction
    open suspend fun replaceContent(
        units: List<UnitEntity>,
        nodes: List<NodeEntity>,
        exercises: List<ExerciseEntity>,
        vocab: List<VocabEntity>,
        version: Int,
    ) {
        clearExercises()
        clearVocabulary()
        clearNodes()
        clearUnits()
        insertUnits(units)
        insertNodes(nodes)
        insertExercises(exercises)
        insertVocabulary(vocab)
        setSeedMeta(SeedMetaEntity(version = version))
    }
}

@Dao
interface ProgressDao {
    @Query("SELECT nodeId FROM node_progress")
    fun observeCompletedNodeIds(): Flow<List<String>>

    @Query("SELECT * FROM node_progress WHERE nodeId = :nodeId")
    suspend fun getNodeProgress(nodeId: String): NodeProgressEntity?

    @Upsert
    suspend fun upsertNodeProgress(progress: NodeProgressEntity)

    @Query("SELECT COUNT(*) FROM vocab_mastery")
    fun observeMasteryCount(): Flow<Int>

    @Query("SELECT * FROM vocab_mastery WHERE vocabId IN (:ids)")
    suspend fun getMastery(ids: List<String>): List<VocabMasteryEntity>

    @Query("SELECT vocabId FROM vocab_mastery WHERE dueEpochDay <= :today ORDER BY dueEpochDay LIMIT :limit")
    suspend fun dueVocabIds(today: Long, limit: Int): List<String>

    @Upsert
    suspend fun upsertMastery(items: List<VocabMasteryEntity>)

    @Query("SELECT COUNT(*) FROM mistakes")
    fun observeMistakeCount(): Flow<Int>

    @Query("SELECT * FROM mistakes ORDER BY lastMissedAtMillis DESC LIMIT :limit")
    suspend fun recentMistakes(limit: Int): List<MistakeEntity>

    @Query("SELECT * FROM mistakes WHERE exerciseId IN (:ids)")
    suspend fun getMistakes(ids: List<String>): List<MistakeEntity>

    @Upsert
    suspend fun upsertMistakes(items: List<MistakeEntity>)

    @Query("DELETE FROM mistakes WHERE exerciseId IN (:ids)")
    suspend fun deleteMistakes(ids: List<String>)
}
