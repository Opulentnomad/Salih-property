package com.rihla.english.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UnitEntity::class,
        NodeEntity::class,
        ExerciseEntity::class,
        VocabEntity::class,
        NodeProgressEntity::class,
        VocabMasteryEntity::class,
        MistakeEntity::class,
        SeedMetaEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class RihlaDatabase : RoomDatabase() {
    abstract fun curriculumDao(): CurriculumDao
    abstract fun progressDao(): ProgressDao

    companion object {
        fun create(context: Context): RihlaDatabase =
            Room.databaseBuilder(context.applicationContext, RihlaDatabase::class.java, "rihla.db")
                .build()
    }
}
