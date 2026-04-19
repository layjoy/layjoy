package com.voicememory.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.voicememory.data.model.VoiceEntry

@Database(
    entities = [VoiceEntry::class],
    version = 2,
    exportSchema = false
)
abstract class VoiceMemoryDatabase : RoomDatabase() {
    abstract fun voiceEntryDao(): VoiceEntryDao
}
