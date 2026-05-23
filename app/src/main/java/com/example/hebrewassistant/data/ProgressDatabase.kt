package com.example.hebrewassistant.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        LessonProgress::class,
        StudentProfile::class,
        LessonSession::class,
        ChatMessageEntity::class,
        LessonSummary::class
    ],
    version = 3,
    exportSchema = false
)
abstract class ProgressDatabase : RoomDatabase() {
    abstract fun lessonProgressDao(): ProgressDao
    abstract fun studentProfileDao(): StudentProfileDao
    abstract fun learningMemoryDao(): LearningMemoryDao

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

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `lesson_sessions` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `title` TEXT,
                        `status` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `completedAt` INTEGER
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_lesson_sessions_status` ON `lesson_sessions` (`status`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_lesson_sessions_createdAt` ON `lesson_sessions` (`createdAt`)")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `chat_messages` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `sessionId` INTEGER NOT NULL,
                        `text` TEXT NOT NULL,
                        `isUser` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_chat_messages_sessionId` ON `chat_messages` (`sessionId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_chat_messages_createdAt` ON `chat_messages` (`createdAt`)")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `lesson_summaries` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `sessionId` INTEGER NOT NULL,
                        `topic` TEXT NOT NULL,
                        `summaryJson` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_lesson_summaries_sessionId` ON `lesson_summaries` (`sessionId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_lesson_summaries_createdAt` ON `lesson_summaries` (`createdAt`)")
            }
        }

        fun getDatabase(context: Context): ProgressDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ProgressDatabase::class.java,
                    "hebrew_assistant_progress_db"
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
