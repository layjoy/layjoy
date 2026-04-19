package com.voicememory.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voicememory.data.model.Emotion
import com.voicememory.data.model.VoiceEntry
import com.voicememory.domain.repository.VoiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class DayData(
    val date: Date,
    val entries: List<VoiceEntry>,
    val dominantEmotion: Emotion?,
    val intensity: Float // 0-1, 录音数量归一化
)

data class CalendarState(
    val currentMonth: Date = Date(),
    val daysData: Map<String, DayData> = emptyMap(),
    val selectedDay: DayData? = null,
    val isLoading: Boolean = true,
    val totalEntries: Int = 0,
    val emotionStats: Map<Emotion, Int> = emptyMap()
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val repository: VoiceRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(CalendarState())
    val uiState: StateFlow<CalendarState> = _uiState.asStateFlow()
    
    init {
        loadMonthData()
    }
    
    fun loadMonthData(month: Date = Date()) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                // 计算当月的开始和结束时间
                val calendar = Calendar.getInstance().apply {
                    time = month
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val startTime = calendar.timeInMillis
                
                calendar.add(Calendar.MONTH, 1)
                val endTime = calendar.timeInMillis
                
                // 从数据库加载当月数据
                repository.getEntriesByDateRange(startTime, endTime).collect { entries ->
                    val daysData = processDaysData(entries)
                    val emotionStats = calculateEmotionStats(entries)
                    
                    _uiState.value = _uiState.value.copy(
                        currentMonth = month,
                        daysData = daysData,
                        isLoading = false,
                        totalEntries = entries.size,
                        emotionStats = emotionStats
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
    
    fun selectDay(dayData: DayData?) {
        _uiState.value = _uiState.value.copy(selectedDay = dayData)
    }
    
    fun previousMonth() {
        val calendar = Calendar.getInstance().apply {
            time = _uiState.value.currentMonth
            add(Calendar.MONTH, -1)
        }
        loadMonthData(calendar.time)
    }
    
    fun nextMonth() {
        val calendar = Calendar.getInstance().apply {
            time = _uiState.value.currentMonth
            add(Calendar.MONTH, 1)
        }
        loadMonthData(calendar.time)
    }
    
    private fun processDaysData(entries: List<VoiceEntry>): Map<String, DayData> {
        val calendar = Calendar.getInstance()
        val grouped = entries.groupBy { entry ->
            calendar.time = Date(entry.timestamp)
            "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH)}-${calendar.get(Calendar.DAY_OF_MONTH)}"
        }
        
        val maxEntriesPerDay = grouped.values.maxOfOrNull { it.size } ?: 1
        
        return grouped.mapValues { (_, dayEntries) ->
            val emotionCounts = dayEntries.groupingBy { it.emotion }.eachCount()
            val dominantEmotion = emotionCounts.maxByOrNull { it.value }?.key
            
            DayData(
                date = Date(dayEntries.first().timestamp),
                entries = dayEntries,
                dominantEmotion = dominantEmotion,
                intensity = dayEntries.size.toFloat() / maxEntriesPerDay
            )
        }
    }
    
    private fun calculateEmotionStats(entries: List<VoiceEntry>): Map<Emotion, Int> {
        return entries.groupingBy { it.emotion }.eachCount()
    }
    
    private fun generateMockEntries(): List<VoiceEntry> {
        val entries = mutableListOf<VoiceEntry>()
        val calendar = Calendar.getInstance()
        val emotions = Emotion.values()
        
        // 生成过去 30 天的随机数据
        repeat(30) { dayOffset ->
            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_MONTH, -dayOffset)
            
            // 每天 0-3 条录音
            val entriesPerDay = (0..3).random()
            repeat(entriesPerDay) {
                entries.add(
                    VoiceEntry(
                        id = entries.size.toLong(),
                        audioFilePath = "/mock/audio_${entries.size}.m4a",
                        transcription = "模拟录音 ${entries.size}",
                        emotion = emotions.random(),
                        timestamp = calendar.timeInMillis,
                        duration = (30000..300000).random().toLong(),
                        isFavorite = false
                    )
                )
            }
        }
        
        return entries
    }
}
