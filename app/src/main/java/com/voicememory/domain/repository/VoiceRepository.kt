package com.voicememory.domain.repository

import com.voicememory.data.model.VoiceEntry
import kotlinx.coroutines.flow.Flow

interface VoiceRepository {
    fun getAllEntries(): Flow<List<VoiceEntry>>
    suspend fun getEntryById(id: Long): VoiceEntry?
    fun getEntriesByDateRange(startTime: Long, endTime: Long): Flow<List<VoiceEntry>>
    suspend fun insertEntry(entry: VoiceEntry): Long
    suspend fun updateEntry(entry: VoiceEntry)
    suspend fun deleteEntry(entry: VoiceEntry)
}
