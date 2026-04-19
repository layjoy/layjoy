package com.voicememory.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voicememory.data.model.Emotion
import com.voicememory.data.model.VoiceEntry
import com.voicememory.domain.audio.AudioRecorder
import com.voicememory.domain.audio.IFlyTekSpeechRecognizer
import com.voicememory.domain.repository.VoiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class RecordUiState(
    val isRecording: Boolean = false,
    val isPaused: Boolean = false,
    val recordingDuration: Long = 0,
    val currentAmplitude: Float = 0f,
    val transcription: String = "",
    val detectedEmotion: Emotion? = null
)

@HiltViewModel
class RecordViewModel @Inject constructor(
    private val repository: VoiceRepository,
    private val audioRecorder: AudioRecorder,
    private val speechRecognizer: IFlyTekSpeechRecognizer
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(RecordUiState())
    val uiState: StateFlow<RecordUiState> = _uiState.asStateFlow()
    
    private var amplitudeJob: Job? = null
    private var durationJob: Job? = null
    private var currentAudioFile: File? = null
    
    fun toggleRecording() {
        viewModelScope.launch {
            if (_uiState.value.isRecording) {
                stopRecording()
            } else {
                startRecording()
            }
        }
    }
    
    fun togglePause() {
        if (_uiState.value.isPaused) {
            audioRecorder.resumeRecording()
        } else {
            audioRecorder.pauseRecording()
        }
        
        _uiState.value = _uiState.value.copy(
            isPaused = !_uiState.value.isPaused
        )
    }
    
    fun stopRecording() {
        viewModelScope.launch {
            amplitudeJob?.cancel()
            durationJob?.cancel()
            
            val result = audioRecorder.stopRecording()
            result.onSuccess { audioFile ->
                currentAudioFile = audioFile
                
                // 开始语音识别
                _uiState.value = _uiState.value.copy(
                    isRecording = false,
                    isPaused = false,
                    transcription = "识别中..."
                )
                
                recognizeAudio(audioFile)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isRecording = false,
                    isPaused = false,
                    transcription = "录音失败: ${error.message}"
                )
            }
        }
    }
    
    private fun startRecording() {
        viewModelScope.launch {
            val result = audioRecorder.startRecording()
            result.onSuccess { audioFile ->
                currentAudioFile = audioFile
                
                _uiState.value = _uiState.value.copy(
                    isRecording = true,
                    isPaused = false,
                    recordingDuration = 0,
                    transcription = "",
                    detectedEmotion = null
                )
                
                // 启动振幅监听
                startAmplitudeMonitoring()
                
                // 启动时长计时
                startDurationTimer()
                
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    transcription = "启动录音失败: ${error.message}"
                )
            }
        }
    }
    
    private fun startAmplitudeMonitoring() {
        amplitudeJob = viewModelScope.launch {
            audioRecorder.getAmplitudeFlow().collect { amplitude ->
                _uiState.value = _uiState.value.copy(
                    currentAmplitude = amplitude
                )
            }
        }
    }
    
    private fun startDurationTimer() {
        durationJob = viewModelScope.launch {
            var duration = 0L
            while (_uiState.value.isRecording && duration < 300000) { // 最多5分钟
                delay(100)
                if (!_uiState.value.isPaused) {
                    duration += 100
                    _uiState.value = _uiState.value.copy(
                        recordingDuration = duration
                    )
                }
            }
            
            // 超过5分钟自动停止
            if (duration >= 300000) {
                stopRecording()
            }
        }
    }
    
    private fun recognizeAudio(audioFile: File) {
        viewModelScope.launch {
            val result = speechRecognizer.recognizeAudio(audioFile)
            result.onSuccess { text ->
                val emotion = analyzeEmotion(text)
                
                _uiState.value = _uiState.value.copy(
                    transcription = text,
                    detectedEmotion = emotion
                )
                
                // 保存到数据库
                saveVoiceEntry(audioFile, text, emotion)
                
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    transcription = "识别失败: ${error.message}"
                )
            }
        }
    }
    
    private fun saveVoiceEntry(audioFile: File, transcription: String, emotion: Emotion) {
        viewModelScope.launch {
            val entry = VoiceEntry(
                audioFilePath = audioFile.absolutePath,
                transcription = transcription,
                emotion = emotion,
                duration = _uiState.value.recordingDuration
            )
            
            repository.insertEntry(entry)
        }
    }
    
    private fun analyzeEmotion(text: String): Emotion {
        return when {
            text.contains(Regex("开心|高兴|快乐|哈哈|嘿嘿|棒|太好了")) -> Emotion.HAPPY
            text.contains(Regex("难过|悲伤|伤心|哭|痛苦")) -> Emotion.SAD
            text.contains(Regex("焦虑|紧张|担心|害怕|不安")) -> Emotion.ANXIOUS
            text.contains(Regex("平静|安静|放松|舒服|宁静")) -> Emotion.CALM
            text.contains(Regex("兴奋|激动|刺激|疯狂")) -> Emotion.EXCITED
            else -> Emotion.NEUTRAL
        }
    }
}
