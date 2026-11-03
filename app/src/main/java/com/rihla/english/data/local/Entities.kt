package com.rihla.english.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/*
 * Content tables (units, nodes, exercises, vocabulary) are replaced when the seed version
 * changes. Progress tables (node_progress, vocab_mastery, mistakes) are never wiped, and are
 * keyed by stable ids so they survive re-seeding.
 */

@Entity(tableName = "units")
data class UnitEntity(
    @PrimaryKey val id: String,
    val orderIndex: Int,
    val titleAr: String,
    val titleEn: String,
    val descriptionAr: String,
    val colorHex: Long,
)

@Entity(tableName = "nodes", indices = [Index("unitId")])
data class NodeEntity(
    @PrimaryKey val id: String,
    val unitId: String,
    val orderIndex: Int,
    /** [com.rihla.english.core.model.NodeType] name. */
    val type: String,
    val titleAr: String,
    val titleEn: String,
    val iconEmoji: String,
    val introTipAr: String?,
    /** JSON of [com.rihla.english.core.model.GrammarBridge], if any. */
    val grammarBridgeJson: String?,
)

@Entity(tableName = "exercises", indices = [Index("nodeId")])
data class ExerciseEntity(
    @PrimaryKey val id: String,
    val nodeId: String,
    val orderIndex: Int,
    /** [com.rihla.english.core.model.ExerciseType] name (for querying/analytics). */
    val type: String,
    /** Polymorphic JSON of [com.rihla.english.core.model.Exercise]. */
    val payloadJson: String,
)

@Entity(tableName = "vocabulary", indices = [Index("nodeId")])
data class VocabEntity(
    @PrimaryKey val id: String,
    val nodeId: String,
    val english: String,
    val arabic: String,
    val emoji: String,
)

@Entity(tableName = "node_progress")
data class NodeProgressEntity(
    @PrimaryKey val nodeId: String,
    val firstCompletedAtMillis: Long,
    val lastCompletedAtMillis: Long,
    val timesCompleted: Int,
    val bestAccuracy: Int,
)

/** SM-2 state per vocabulary item. */
@Entity(tableName = "vocab_mastery", indices = [Index("dueEpochDay")])
data class VocabMasteryEntity(
    @PrimaryKey val vocabId: String,
    val easiness: Double,
    val intervalDays: Int,
    val repetitions: Int,
    val dueEpochDay: Long,
    val lastReviewedEpochDay: Long,
    val timesCorrect: Int,
    val timesWrong: Int,
)

/** Exercises the learner got wrong; the pool for "Practice Mistakes". */
@Entity(tableName = "mistakes")
data class MistakeEntity(
    @PrimaryKey val exerciseId: String,
    val nodeId: String?,
    val missCount: Int,
    val lastMissedAtMillis: Long,
)

@Entity(tableName = "seed_meta")
data class SeedMetaEntity(
    @PrimaryKey val id: Int = 0,
    val version: Int,
)
