package com.voicememory.data.local

import androidx.room.*
import com.voicememory.data.model.AIAnalysis
import kotlinx.coroutines.flow.Flow

@Dao
interface AIAnalysisDao {
    @Query("SELECT * FROM ai_analysis WHERE entryId = :entryId LIMIT 1")
    suspend fun getAnalysisByEntryId(entryId: Long): AIAnalysis?
    
    @Query("SELECT * FROM ai_analysis WHERE entryId = :entryId")
    fun getAnalysisByEntryIdFlow(entryId: Long): Flow<AIAnalysis?>
    
    @Query("SELECT * FROM ai_analysis ORDER BY timestamp DESC")
    fun getAllAnalysis(): Flow<List<AIAnalysis>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnalysis(analysis: AIAnalysis): Long
    
    @Update
    suspend fun updateAnalysis(analysis: AIAnalysis)
    
    @Delete
    suspend fun deleteAnalysis(analysis: AIAnalysis)
    
    @Query("DELETE FROM ai_analysis WHERE entryId = :entryId")
    suspend fun deleteAnalysisByEntryId(entryId: Long)
}
