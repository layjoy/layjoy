package com.voicememory.data.repository

import com.voicememory.data.local.AIAnalysisDao
import com.voicememory.data.local.VoiceEntryDao
import com.voicememory.data.model.AIAnalysis
import com.voicememory.data.model.VoiceEntry
import com.voicememory.domain.repository.VoiceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class VoiceRepositoryImpl @Inject constructor(
    private val dao: VoiceEntryDao,
    private val aiAnalysisDao: AIAnalysisDao
) : VoiceRepository {
    
    override fun getAllEntries(): Flow<List<VoiceEntry>> = dao.getAllEntries()
    
    override suspend fun getEntryById(id: Long): VoiceEntry? = dao.getEntryById(id)
    
    override fun getEntriesByDateRange(startTime: Long, endTime: Long): Flow<List<VoiceEntry>> =
        dao.getEntriesByDateRange(startTime, endTime)
    
    override fun searchEntries(query: String): Flow<List<VoiceEntry>> =
        dao.searchEntries(query)
    
    override suspend fun insertEntry(entry: VoiceEntry): Long = dao.insertEntry(entry)
    
    override suspend fun updateEntry(entry: VoiceEntry) = dao.updateEntry(entry)
    
    override suspend fun deleteEntry(entry: VoiceEntry) = dao.deleteEntry(entry)
    
    override suspend fun saveAIAnalysis(analysis: AIAnalysis) {
        aiAnalysisDao.insertAnalysis(analysis)
    }
    
    override suspend fun getAIAnalysis(entryId: Long): AIAnalysis? = aiAnalysisDao.getAnalysisByEntryId(entryId)
}
