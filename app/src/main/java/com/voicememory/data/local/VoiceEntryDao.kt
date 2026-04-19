package com.voicememory.data.local

import androidx.room.*
import com.voicememory.data.model.VoiceEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface VoiceEntryDao {
    @Query("SELECT * FROM voice_entries ORDER BY timestamp DESC")
    fun getAllEntries(): Flow<List<VoiceEntry>>
    
    @Query("SELECT * FROM voice_entries WHERE id = :id")
    suspend fun getEntryById(id: Long): VoiceEntry?
    
    @Query("SELECT * FROM voice_entries WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    fun getEntriesByDateRange(startTime: Long, endTime: Long): Flow<List<VoiceEntry>>
    
    @Query("SELECT * FROM voice_entries WHERE transcription LIKE '%' || :query || '%' OR summary LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchEntries(query: String): Flow<List<VoiceEntry>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: VoiceEntry): Long
    
    @Update
    suspend fun updateEntry(entry: VoiceEntry)
    
    @Delete
    suspend fun deleteEntry(entry: VoiceEntry)
    
    @Query("DELETE FROM voice_entries WHERE id = :id")
    suspend fun deleteEntryById(id: Long)
}
