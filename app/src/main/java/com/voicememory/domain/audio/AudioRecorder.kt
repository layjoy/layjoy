package com.voicememory.domain.audio

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioRecorder @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var mediaRecorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var isRecording = false
    
    suspend fun startRecording(): Result<File> = withContext(Dispatchers.IO) {
        try {
            if (isRecording) {
                return@withContext Result.failure(Exception("已在录音中"))
            }
            
            // 创建输出文件
            val audioDir = File(context.filesDir, "audio")
            if (!audioDir.exists()) {
                audioDir.mkdirs()
            }
            
            outputFile = File(audioDir, "voice_${System.currentTimeMillis()}.m4a")
            
            // 初始化 MediaRecorder
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioSamplingRate(16000) // 讯飞推荐 16kHz
                setAudioEncodingBitRate(128000)
                setOutputFile(outputFile!!.absolutePath)
                prepare()
                start()
            }
            
            isRecording = true
            Result.success(outputFile!!)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun stopRecording(): Result<File> = withContext(Dispatchers.IO) {
        try {
            if (!isRecording || mediaRecorder == null) {
                return@withContext Result.failure(Exception("未在录音"))
            }
            
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            isRecording = false
            
            outputFile?.let {
                Result.success(it)
            } ?: Result.failure(Exception("录音文件不存在"))
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun pauseRecording() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mediaRecorder?.pause()
        }
    }
    
    fun resumeRecording() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mediaRecorder?.resume()
        }
    }
    
    fun getMaxAmplitude(): Int {
        return try {
            mediaRecorder?.maxAmplitude ?: 0
        } catch (e: Exception) {
            0
        }
    }
    
    fun getAmplitudeFlow(): Flow<Float> = flow {
        while (isRecording) {
            val amplitude = getMaxAmplitude()
            // 归一化到 0-1 范围
            val normalized = (amplitude / 32767f).coerceIn(0f, 1f)
            emit(normalized)
            kotlinx.coroutines.delay(100) // 每100ms更新一次
        }
    }
    
    fun isRecording(): Boolean = isRecording
}
