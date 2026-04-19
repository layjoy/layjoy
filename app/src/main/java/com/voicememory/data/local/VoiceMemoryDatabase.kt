package com.voicememory.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.voicememory.data.model.AIAnalysis
import com.voicememory.data.model.VoiceEntry

@Database(
    entities = [VoiceEntry::class, AIAnalysis::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class VoiceMemoryDatabase : RoomDatabase() {
    abstract fun voiceEntryDao(): VoiceEntryDao
    abstract fun aiAnalysisDao(): AIAnalysisDao
    
    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 添加新字段到 voice_entries 表
                database.execSQL(
                    "ALTER TABLE voice_entries ADD COLUMN isFavorite INTEGER NOT NULL DEFAULT 0"
                )
                database.execSQL(
                    "ALTER TABLE voice_entries ADD COLUMN summary TEXT NOT NULL DEFAULT ''"
                )
                database.execSQL(
                    "ALTER TABLE voice_entries ADD COLUMN tags TEXT NOT NULL DEFAULT ''"
                )
                
                // 创建 ai_analysis 表
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS ai_analysis (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        entryId INTEGER NOT NULL,
                        summary TEXT NOT NULL,
                        emotionAnalysis TEXT NOT NULL,
                        emotionIntensity REAL NOT NULL,
                        tags TEXT NOT NULL,
                        suggestions TEXT NOT NULL,
                        timestamp INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }
    }
}
