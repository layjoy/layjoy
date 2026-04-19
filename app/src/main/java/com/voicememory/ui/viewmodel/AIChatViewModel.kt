package com.voicememory.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voicememory.data.model.ChatMessage
import com.voicememory.domain.ai.SparkAIService
import com.voicememory.domain.repository.VoiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AIChatState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AIChatViewModel @Inject constructor(
    private val aiService: SparkAIService,
    private val repository: VoiceRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AIChatState())
    val uiState: StateFlow<AIChatState> = _uiState.asStateFlow()
    
    init {
        // 添加欢迎消息
        _uiState.value = _uiState.value.copy(
            messages = listOf(
                ChatMessage(
                    role = "assistant",
                    content = "你好！我是你的 AI 日记助手。你可以问我关于你的录音、情绪趋势，或者只是想聊聊天 😊"
                )
            )
        )
    }
    
    /**
     * 发送消息
     */
    fun sendMessage(message: String) {
        if (message.isBlank()) return
        
        viewModelScope.launch {
            // 添加用户消息
            val userMessage = ChatMessage(role = "user", content = message)
            _uiState.value = _uiState.value.copy(
                messages = _uiState.value.messages + userMessage,
                isLoading = true,
                error = null
            )
            
            try {
                // 获取历史录音作为上下文
                val context = getRecentEntriesContext()
                
                // 调用 AI
                val result = aiService.chat(message, context)
                
                result.onSuccess { response ->
                    val aiMessage = ChatMessage(role = "assistant", content = response)
                    _uiState.value = _uiState.value.copy(
                        messages = _uiState.value.messages + aiMessage,
                        isLoading = false
                    )
                }.onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "AI 回复失败: ${error.message}"
                    )
                }
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "发送失败: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 清空对话
     */
    fun clearChat() {
        _uiState.value = AIChatState(
            messages = listOf(
                ChatMessage(
                    role = "assistant",
                    content = "对话已清空。有什么我可以帮你的吗？"
                )
            )
        )
    }
    
    /**
     * 获取最近录音作为上下文
     */
    private suspend fun getRecentEntriesContext(): List<String> {
        return try {
            // TODO: 从数据库获取最近 5 条录音的摘要
            // repository.getRecentEntries(5).map { it.transcription.take(200) }
            emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
