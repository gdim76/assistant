package com.example.hebrewassistant.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [LessonProgress::class, StudentProfile::class], version = 2, exportSchema = false)
abstract class ProgressDatabase : RoomDatabase() {
    abstract fun lessonProgressDao(): ProgressDao
    abstract fun studentProfileDao(): StudentProfileDao

    companion object {
        @Volatile
        private var INSTANCE: ProgressDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `student_profile` (
                        `id` INTEGER NOT NULL,
                        `name` TEXT,
                        `nativeLanguage` TEXT,
                        `interests` TEXT,
                        `workArea` TEXT,
                        `learningGoal` TEXT,
                        `currentLevel` TEXT,
                        `placementSummary` TEXT,
                        `onboardingCompleted` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
            }
        }

        fun getDatabase(context: Context): ProgressDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ProgressDatabase::class.java,
                    "hebrew_assistant_progress_db"
                ).addMigrations(MIGRATION_1_2).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
