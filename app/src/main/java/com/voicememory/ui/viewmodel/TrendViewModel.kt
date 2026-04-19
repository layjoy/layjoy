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

data class TrendState(
    val timeRangeDays: Int = 30,
    val trendData: Map<Long, Map<Emotion, Int>> = emptyMap(),
    val emotionStats: Map<Emotion, Int> = emptyMap(),
    val insights: List<String> = emptyList(),
    val totalEntries: Int = 0,
    val avgEntriesPerDay: Float = 0f,
    val mostActiveDay: String = "",
    val isLoading: Boolean = true
)

@HiltViewModel
class TrendViewModel @Inject constructor(
    private val repository: VoiceRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(TrendState())
    val uiState: StateFlow<TrendState> = _uiState.asStateFlow()
    
    init {
        loadTrendData()
    }
    
    fun setTimeRange(days: Int) {
        _uiState.value = _uiState.value.copy(timeRangeDays = days)
        loadTrendData()
    }
    
    private fun loadTrendData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                // TODO: 从数据库加载数据
                // val entries = repository.getEntriesForTimeRange(timeRangeDays)
                
                // 模拟数据
                val mockEntries = generateMockEntries(_uiState.value.timeRangeDays)
                
                val trendData = processTrendData(mockEntries, _uiState.value.timeRangeDays)
                val emotionStats = calculateEmotionStats(mockEntries)
                val insights = generateInsights(mockEntries, emotionStats)
                val (avgPerDay, mostActiveDay) = calculateActivityStats(mockEntries, _uiState.value.timeRangeDays)
                
                _uiState.value = _uiState.value.copy(
                    trendData = trendData,
                    emotionStats = emotionStats,
                    insights = insights,
                    totalEntries = mockEntries.size,
                    avgEntriesPerDay = avgPerDay,
                    mostActiveDay = mostActiveDay,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
    
    private fun processTrendData(entries: List<VoiceEntry>, days: Int): Map<Long, Map<Emotion, Int>> {
        val calendar = Calendar.getInstance()
        val result = mutableMapOf<Long, MutableMap<Emotion, Int>>()
        
        // 初始化所有日期
        repeat(days) { dayOffset ->
            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_MONTH, -dayOffset)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            
            result[calendar.timeInMillis] = Emotion.values().associateWith { 0 }.toMutableMap()
        }
        
        // 统计每天的情绪分布
        entries.forEach { entry ->
            calendar.time = Date(entry.timestamp)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            
            val dayKey = calendar.timeInMillis
            result[dayKey]?.let { emotionMap ->
                emotionMap[entry.emotion] = (emotionMap[entry.emotion] ?: 0) + 1
            }
        }
        
        return result
    }
    
    private fun calculateEmotionStats(entries: List<VoiceEntry>): Map<Emotion, Int> {
        return entries.groupingBy { it.emotion }.eachCount()
    }
    
    private fun generateInsights(entries: List<VoiceEntry>, emotionStats: Map<Emotion, Int>): List<String> {
        val insights = mutableListOf<String>()
        
        if (entries.isEmpty()) {
            insights.add("还没有录音数据，开始记录你的声音日记吧！")
            return insights
        }
        
        // 主导情绪
        val dominantEmotion = emotionStats.maxByOrNull { it.value }
        dominantEmotion?.let {
            val percentage = (it.value.toFloat() / entries.size * 100).toInt()
            insights.add("${it.key.displayName}是你最常见的情绪，占比 $percentage%")
        }
        
        // 情绪多样性
        val emotionCount = emotionStats.size
        when {
            emotionCount >= 5 -> insights.add("你的情绪状态很丰富，说明生活充满变化")
            emotionCount >= 3 -> insights.add("你的情绪状态比较稳定，有一定的波动")
            else -> insights.add("你的情绪状态相对单一，可以尝试更多样的活动")
        }
        
        // 积极情绪比例
        val positiveCount = (emotionStats[Emotion.HAPPY] ?: 0) + (emotionStats[Emotion.CALM] ?: 0) + (emotionStats[Emotion.EXCITED] ?: 0)
        val positiveRatio = positiveCount.toFloat() / entries.size
        when {
            positiveRatio >= 0.7 -> insights.add("积极情绪占比很高，保持这种状态！")
            positiveRatio >= 0.4 -> insights.add("积极情绪和消极情绪比较平衡")
            else -> insights.add("最近消极情绪较多，建议多做一些让自己开心的事")
        }
        
        return insights
    }
    
    private fun calculateActivityStats(entries: List<VoiceEntry>, days: Int): Pair<Float, String> {
        val avgPerDay = entries.size.toFloat() / days
        
        val calendar = Calendar.getInstance()
        val dayOfWeekCounts = entries.groupingBy { entry ->
            calendar.time = Date(entry.timestamp)
            calendar.get(Calendar.DAY_OF_WEEK)
        }.eachCount()
        
        val mostActiveDay = dayOfWeekCounts.maxByOrNull { it.value }?.key?.let { dayOfWeek ->
            when (dayOfWeek) {
                Calendar.SUNDAY -> "周日"
                Calendar.MONDAY -> "周一"
                Calendar.TUESDAY -> "周二"
                Calendar.WEDNESDAY -> "周三"
                Calendar.THURSDAY -> "周四"
                Calendar.FRIDAY -> "周五"
                Calendar.SATURDAY -> "周六"
                else -> "未知"
            }
        } ?: "无"
        
        return Pair(avgPerDay, mostActiveDay)
    }
    
    private fun generateMockEntries(days: Int): List<VoiceEntry> {
        val entries = mutableListOf<VoiceEntry>()
        val calendar = Calendar.getInstance()
        val emotions = Emotion.values()
        
        repeat(days) { dayOffset ->
            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_MONTH, -dayOffset)
            
            // 每天 0-5 条录音
            val entriesPerDay = (0..5).random()
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
