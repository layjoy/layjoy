package com.voicememory.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.voicememory.data.model.VoiceEntry
import com.voicememory.domain.ai.SparkAIService
import com.voicememory.domain.repository.VoiceRepository
import com.voicememory.ui.viewmodel.AIAnalysisState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlayerState(
    val entry: VoiceEntry? = null,
    val isLoading: Boolean = true,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0,
    val duration: Long = 0,
    val playbackSpeed: Float = 1.0f,
    val isLooping: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class PlayerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: VoiceRepository,
    private val aiService: SparkAIService
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(PlayerState())
    val uiState: StateFlow<PlayerState> = _uiState.asStateFlow()
    
    private val _aiAnalysisState = MutableStateFlow(AIAnalysisState())
    val aiAnalysisState: StateFlow<AIAnalysisState> = _aiAnalysisState.asStateFlow()
    
    private var player: ExoPlayer? = null
    private var progressJob: Job? = null
    
    init {
        initializePlayer()
    }
    
    private fun initializePlayer() {
        player = ExoPlayer.Builder(context).build().apply {
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_READY -> {
                            _uiState.value = _uiState.value.copy(
                                duration = duration,
                                isLoading = false
                            )
                        }
                        Player.STATE_ENDED -> {
                            _uiState.value = _uiState.value.copy(isPlaying = false)
                            stopProgressTracking()
                        }
                    }
                }
                
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _uiState.value = _uiState.value.copy(isPlaying = isPlaying)
                    if (isPlaying) {
                        startProgressTracking()
                    } else {
                        stopProgressTracking()
                    }
                }
            })
        }
    }
    
    fun loadEntry(entryId: Long) {
        viewModelScope.launch {
            try {
                val entry = repository.getEntryById(entryId)
                if (entry == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "录音不存在"
                    )
                    return@launch
                }
                
                _uiState.value = _uiState.value.copy(entry = entry, isLoading = false)
                
                // 加载音频
                player?.setMediaItem(MediaItem.fromUri(entry.audioFilePath))
                player?.prepare()
                
                // 加载 AI 分析
                loadAIAnalysis(entry)
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "加载失败: ${e.message}"
                )
            }
        }
    }
    
    private fun loadAIAnalysis(entry: VoiceEntry) {
        viewModelScope.launch {
            _aiAnalysisState.value = _aiAnalysisState.value.copy(isLoading = true)
            
            try {
                val summaryResult = aiService.generateSummary(entry.transcription)
                val emotionResult = aiService.analyzeEmotion(entry.transcription)
                val tagsResult = aiService.extractTags(entry.transcription)
                
                _aiAnalysisState.value = _aiAnalysisState.value.copy(
                    isLoading = false,
                    summary = summaryResult.getOrNull() ?: "",
                    emotionAnalysis = emotionResult.getOrNull(),
                    tags = tagsResult.getOrNull() ?: emptyList()
                )
            } catch (e: Exception) {
                _aiAnalysisState.value = _aiAnalysisState.value.copy(
                    isLoading = false,
                    error = "AI 分析失败: ${e.message}"
                )
            }
        }
    }
    
    fun togglePlayPause() {
        player?.let {
            if (it.isPlaying) {
                it.pause()
            } else {
                it.play()
            }
        }
    }
    
    fun seekTo(position: Long) {
        player?.seekTo(position)
        _uiState.value = _uiState.value.copy(currentPosition = position)
    }
    
    fun setPlaybackSpeed(speed: Float) {
        player?.setPlaybackSpeed(speed)
        _uiState.value = _uiState.value.copy(playbackSpeed = speed)
    }
    
    fun rewind() {
        player?.let {
            val newPosition = (it.currentPosition - 15000).coerceAtLeast(0)
            it.seekTo(newPosition)
        }
    }
    
    fun forward() {
        player?.let {
            val newPosition = (it.currentPosition + 15000).coerceAtMost(it.duration)
            it.seekTo(newPosition)
        }
    }
    
    fun toggleFavorite() {
        viewModelScope.launch {
            _uiState.value.entry?.let { entry ->
                val updated = entry.copy(isFavorite = !entry.isFavorite)
                _uiState.value = _uiState.value.copy(entry = updated)
                repository.updateEntry(updated)
            }
        }
    }
    
    fun toggleLoop() {
        val newLoopState = !_uiState.value.isLooping
        player?.repeatMode = if (newLoopState) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
        _uiState.value = _uiState.value.copy(isLooping = newLoopState)
    }
    
    fun shareEntry(onShare: (String, String) -> Unit) {
        _uiState.value.entry?.let { entry ->
            val shareText = buildString {
                appendLine("【语音记录分享】")
                appendLine()
                appendLine("时间: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(java.util.Date(entry.timestamp))}")
                appendLine("时长: ${entry.duration / 1000}秒")
                appendLine("情绪: ${entry.emotion.name}")
                appendLine()
                appendLine("内容:")
                appendLine(entry.transcription)
                
                if (_aiAnalysisState.value.summary.isNotEmpty()) {
                    appendLine()
                    appendLine("摘要:")
                    appendLine(_aiAnalysisState.value.summary)
                }
                
                if (_aiAnalysisState.value.tags.isNotEmpty()) {
                    appendLine()
                    appendLine("标签: ${_aiAnalysisState.value.tags.joinToString(", ")}")
                }
            }
            
            onShare(shareText, entry.audioFilePath)
        }
    }
    
    fun deleteEntry() {
        viewModelScope.launch {
            _uiState.value.entry?.let { entry ->
                try {
                    repository.deleteEntry(entry)
                    // 停止播放
                    player?.stop()
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        error = "删除失败: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun regenerateAnalysis() {
        _uiState.value.entry?.let { entry ->
            loadAIAnalysis(entry)
        }
    }
    
    private fun startProgressTracking() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (isActive) {
                player?.let {
                    _uiState.value = _uiState.value.copy(
                        currentPosition = it.currentPosition
                    )
                }
                delay(100)
            }
        }
    }
    
    private fun stopProgressTracking() {
        progressJob?.cancel()
    }
    
    override fun onCleared() {
        super.onCleared()
        player?.release()
        player = null
        progressJob?.cancel()
    }
}
