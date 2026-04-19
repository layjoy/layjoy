package com.voicememory.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voicememory.data.model.AIAnalysis
import com.voicememory.data.model.VoiceEntry
import com.voicememory.domain.ai.EmotionAnalysisResult
import com.voicememory.domain.ai.SparkAIService
import com.voicememory.domain.repository.VoiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AIAnalysisState(
    val isLoading: Boolean = false,
    val summary: String = "",
    val emotionAnalysis: EmotionAnalysisResult? = null,
    val tags: List<String> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class AIAnalysisViewModel @Inject constructor(
    private val aiService: SparkAIService,
    private val repository: VoiceRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AIAnalysisState())
    val uiState: StateFlow<AIAnalysisState> = _uiState.asStateFlow()
    
    /**
     * 分析录音（摘要 + 情绪 + 标签）
     */
    fun analyzeEntry(entry: VoiceEntry) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                // 并行执行三个分析任务
                val summaryResult = aiService.generateSummary(entry.transcription)
                val emotionResult = aiService.analyzeEmotion(entry.transcription)
                val tagsResult = aiService.extractTags(entry.transcription)
                
                val summary = summaryResult.getOrNull() ?: ""
                val emotion = emotionResult.getOrNull()
                val tags = tagsResult.getOrNull() ?: emptyList()
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    summary = summary,
                    emotionAnalysis = emotion,
                    tags = tags
                )
                
                // 保存分析结果到数据库
                saveAnalysis(entry.id, summary, emotion, tags)
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "AI 分析失败: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 重新生成摘要
     */
    fun regenerateSummary(text: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val result = aiService.generateSummary(text)
            result.onSuccess { summary ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    summary = summary
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "生成摘要失败: ${error.message}"
                )
            }
        }
    }
    
    private fun saveAnalysis(
        entryId: Long,
        summary: String,
        emotion: EmotionAnalysisResult?,
        tags: List<String>
    ) {
        viewModelScope.launch {
            val analysis = AIAnalysis(
                entryId = entryId,
                summary = summary,
                emotionAnalysis = emotion?.let {
                    "${it.emotion.displayName} (${(it.intensity * 100).toInt()}%)\n原因: ${it.reason}"
                } ?: "",
                emotionIntensity = emotion?.intensity ?: 0f,
                tags = tags,
                suggestions = emotion?.suggestion ?: ""
            )
            
            // TODO: 保存到数据库
            // repository.saveAIAnalysis(analysis)
        }
    }
}
