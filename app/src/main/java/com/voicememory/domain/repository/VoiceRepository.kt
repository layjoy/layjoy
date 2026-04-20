package com.voicememory.domain.repository

import com.voicememory.data.model.AIAnalysis
import com.voicememory.data.model.VoiceEntry
import kotlinx.coroutines.flow.Flow

interface VoiceRepository {
    fun getAllEntries(): Flow<List<VoiceEntry>>
    suspend fun getEntryById(id: Long): VoiceEntry?
    suspend fun getRecentEntries(limit: Int): List<VoiceEntry>
    fun getEntriesByDateRange(startTime: Long, endTime: Long): Flow<List<VoiceEntry>>
    fun searchEntries(query: String): Flow<List<VoiceEntry>>
    suspend fun insertEntry(entry: VoiceEntry): Long
    suspend fun updateEntry(entry: VoiceEntry)
    suspend fun deleteEntry(entry: VoiceEntry)
    
    // AI 分析相关
    suspend fun saveAIAnalysis(analysis: AIAnalysis)
    suspend fun getAIAnalysis(entryId: Long): AIAnalysis?
}
